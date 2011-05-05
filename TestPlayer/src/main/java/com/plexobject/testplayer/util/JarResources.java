/* ===========================================================================
 * $RCS$
 * Version: $Id: JarResources.java,v 1.2 2007/07/11 13:53:48 shahzad Exp $
 * ===========================================================================
 *
 * TestPlayer - an automated test harness builder
 *
 * Copyright (c) 2005-2006 Shahzad Bhatti (bhatti@plexobject.com)
 *
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 2 of the License, or 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * The author may be contacted at bhatti@plexobject.com 
 * See http://testplayer.dev.java.net/ for more details.
 *
 */

package com.plexobject.testplayer.util;
import java.io.*;
import java.util.*;
import java.util.zip.*;


/**
 * <B>CLASS COMMENTS</B>
 * Class Name: JarResource
 * Class Description:
 *   JarResource fetches a resource from jar file or zip file
 *   Additionaly, it provides a method to extract one as a blob.
 *
 * @Author: SAB
 * $Author: shahzad $
 * Known Bugs:
 *   None
 * Concurrency Issues:
 *   None
 * Invariants:
 *   N/A
 * Modification History
 * Initial      Date            Changes
 * SAB          Feb 22, 2006    Created
*/


public final class JarResources {
  /**
   * creates a JarResources. It extracts all resources from a Jar
   * into an internal hashtable, keyed by resource names.
   * @param jarFileName a jar or zip file
   */
  public JarResources(String jarFileName) {
    this.jarFileName=jarFileName;
    init();
  }

  /**
   * Extracts a jar resource as a blob.
   * @param name a resource name.
   * @return byte-array of resource
   */
  public byte[] getResource(String name) {
    return (byte[]) htJarContents.get(name);
  }


  /**
   * @return names of all resources in the zip or jar file.
   */
  public String[] getResourceNames() {
    String[] names = new String[htJarContents.size()];
    int i=0;
    Iterator it = htJarContents.keySet().iterator();
    while (it.hasNext()) {
      names[i++] = (String) it.next();
    }
    return names;
  }

  /**
   * initializes internal hash tables with Jar file resources.
   */
  private void init() {
    try {
      // extracts just sizes only. 
      ZipFile zf=new ZipFile(jarFileName);
      Enumeration e=zf.entries();
      while (e.hasMoreElements()) {
        ZipEntry ze=(ZipEntry)e.nextElement();
        htSizes.put(ze.getName(),new Integer((int)ze.getSize()));
      }
      zf.close();
      // extract resources and put them into the hashtable.
      FileInputStream fis=new FileInputStream(jarFileName);
      BufferedInputStream bis=new BufferedInputStream(fis);
      ZipInputStream zis=new ZipInputStream(bis);
      ZipEntry ze=null;
      while ((ze=zis.getNextEntry())!=null) {
        if (ze.isDirectory()) continue;
        int size=(int)ze.getSize();
        // -1 means unknown size.
        if (size==-1) {
          size=((Integer)htSizes.get(ze.getName())).intValue();
        }

        byte[] b=new byte[(int)size];
        int rb=0;
        int chunk=0;
        while (((int)size - rb) > 0) {
          chunk=zis.read(b,rb,(int)size - rb);
          if (chunk==-1) break;
          rb+=chunk;
        }

        // add to internal resource hashtable
        htJarContents.put(ze.getName(),b);
      } // while
    } catch (NullPointerException e) { // done}
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Dumps a zip entry into a string.
   * @param ze a ZipEntry
   */
  private String dumpZipEntry(ZipEntry ze) {
    StringBuilder sb=new StringBuilder();
    if (ze.isDirectory()) sb.append("d ");
    else sb.append("f ");
    if (ze.getMethod()==ZipEntry.STORED) sb.append("stored   ");
    else sb.append("defalted ");
    sb.append(ze.getName());
    sb.append("\t");
    sb.append(""+ze.getSize());
    if (ze.getMethod()==ZipEntry.DEFLATED) sb.append("/"+ze.getCompressedSize());
    return (sb.toString());
  }

  /**
   * Is a test driver. Given a jar file and a resource name, it trys to
   * extract the resource and then tells us whether it could or not.
   *
   * <strong>Example</strong>
   * Let's say you have a JAR file which jarred up a bunch of gif image
   * files. Now, by using JarResources, you could extract, create, and
   * display those images on-the-fly.
   * <pre>
   *     ...
   *     JarResources JR=new JarResources("GifBundle.jar");
   *     Image image=Toolkit.createImage(JR.getResource("logo.gif");
   *     Image logo=Toolkit.getDefaultToolkit().createImage(
   *                   JR.getResources("logo.gif")
   *                   );
   *     ...
   * </pre>
   */
  public static void main(String[] args) throws IOException {
    if (args.length!=2) {
            System.err.println(
                "usage: java JarResources <jar file name> <resource name>"
                );
            System.exit(1);
    }

    JarResources jr=new JarResources(args[0]);
    byte[] buff=jr.getResource(args[1]);
    if (buff==null) {
      System.out.println("Could not find "+args[1]+".");
    } else {
      System.out.println("Found "+args[1]+
                               " (length="+buff.length+").");
    }
  }

  /////////////////////////////////////////////////////////////////////
  //
  private Map htSizes = new HashMap();  
  private Map htJarContents = new HashMap();
  private String jarFileName;
} 
