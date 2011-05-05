/* ===========================================================================
 * $RCS$
 * Version: $Id: DefaultWritingStrategy.java,v 1.7 2007/07/11 13:53:47 shahzad Exp $
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

package com.plexobject.testplayer.plugin.test.strategy;
import com.plexobject.testplayer.*;
import com.plexobject.testplayer.plugin.*;
import com.plexobject.testplayer.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;
import java.lang.reflect.*;
import org.apache.log4j.*;

public class DefaultWritingStrategy implements WritingStrategy {
  private static int INDENT_WIDTH = 2;

  public void indent(StringBuilder sourceCode) {
    int indentLevel = 0;
    int beginIndex  = 0;
    int endIndex  = 0;
    int opening   = 0;
    int closing   = 0;
    int inserted  = 0;
    boolean openingFirst = false;
    boolean closingFirst = false;

    if (sourceCode != null) {
      while (endIndex<sourceCode.length()) {
        switch (sourceCode.charAt(endIndex)) {
          case '{':
            opening++;
            if (!closingFirst) {
              openingFirst = true;
            }
            break;
          case '}':
            closing++;
            if (!openingFirst) {
              closingFirst = true;
            }
            break;
          case '\n':
            if (closing>opening) {
              indentLevel = indentLevel-(closing-opening);
            }
            if (closing == opening) {
              if (closingFirst) indentLevel--;
            }

            inserted = 0;
            for (int i=0; (i<(indentLevel*INDENT_WIDTH)); i++) {
              if (sourceCode.charAt(beginIndex+i) != ' ') {
                sourceCode.insert(beginIndex+i, " ");
                inserted++;
              }
            }
            endIndex += inserted;

            if (closing == opening) {
              if (closingFirst) indentLevel++;
            }
            if (opening>closing) {
              indentLevel = indentLevel+(opening-closing);
            }
            beginIndex = endIndex+1;
            opening = 0;
            closing = 0;
            openingFirst = false;
            closingFirst = false;
            break;
          // no default
        }
        endIndex++;
      }
    }
  }

  /**
   * Merges generated source code with class file for given class name.
   * @return true if successfully merged or target file does not exist, 
   * false if class file contains no markers.
   */
  public StringBuilder loadClassSource(
        String root, 
        String fullClassName) throws IOException {
    String     line;

    StringBuilder returnValue = new StringBuilder();
    String name = translateClassNameToFileName(fullClassName);
    File file = new File(root, name);
    if (file.exists()) {
      BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
      while (bufferedReader.ready()) {
        line = bufferedReader.readLine();
        returnValue.append(line);
        returnValue.append("\n");
      }
      bufferedReader.close();
    } // no else
    return returnValue;
  }

  public void writeClassSource(
        String root, 
        String fullClassName, 
        StringBuilder sourceCode) throws IOException {
    String name = translateClassNameToFileName(fullClassName);

    File file = new File(root, name);
    file.getParentFile().mkdirs();

    FileWriter fileWriter   = new FileWriter(file);
    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
    bufferedWriter.write(sourceCode.toString());
    bufferedWriter.flush();
    bufferedWriter.close();
    fileWriter.close();
  }

  public String translateClassNameToFileName(String className) {
    String returnValue;
    returnValue = className.replace('.', File.separatorChar) + ".java";
    return returnValue;
  }

  public boolean isExistingAndNewer(
        String dirInQuestion, 
        String fullClassNameInQuestion,
        String dirReference, 
        String fullClassNameReference) {
    boolean returnValue = false;
    File inQuestion;
    File reference;

    if ((dirInQuestion != null) && (fullClassNameInQuestion != null) &&
      (dirReference != null) && (fullClassNameReference != null)) {
      inQuestion = new File(dirInQuestion, translateClassNameToFileName(fullClassNameInQuestion));
      reference  = new File(dirReference, translateClassNameToFileName(fullClassNameReference));
      returnValue = inQuestion.exists() && (inQuestion.lastModified()>reference.lastModified());
    }
    return returnValue;
  }
}
