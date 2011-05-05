/* ===========================================================================
 * $RCS$
 * Version: $Id: StringHelper.java,v 2.5 2007/07/11 13:53:48 shahzad Exp $
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
import java.util.*;
import java.util.regex.*;
import java.text.*;
import java.io.*;
import java.util.zip.*;


/**
 * This class is helper class for String manipulation
 *
 * @author shahzad bhatti
 *
 * modification history
 * date         who             what
 * 12/21/05     SB              created.
 */
public class StringHelper {
  private final static String VARIABLE_START = "${";
  private final static String VARIABLE_END   = "}";
  private final static int  MAX_DEPTH    = 10;
  /**
   * replace - modifies given string and replaces specified pattern
   * @param str - string to modify
   * @param frompat - pattern to find
   * @param topat - pattern to replace
   * @return modified string
   */
  public static String replace(String str, String frompat, String topat) {
    Pattern p = Pattern.compile(frompat);
    Matcher matcher = p.matcher(str);
    return matcher.replaceAll(topat);
  }

  /**
   * split - splits string using space delimited 
   * @param str - string to split
   * @return array of strings
   */
  public static String[] split(String str) {
    if (str == null) return null;
    return split(str, null);
  }



  /**
   * split - splits string using given delimiter
   * @param str - string to split
   * @param delim - string delimiter
   * @return array of strings
   */
  public static String[] split(String str, String delim) {
    if (str == null) return null;
    str = str.trim();
    if (str.length() == 0) return new String[0];
    List list = new ArrayList();
    StringTokenizer st = null;
    if (delim == null) st = new StringTokenizer(str);
    else st = new StringTokenizer(str, delim);
    while (st.hasMoreTokens()) {
      list.add(st.nextToken());
    }
    return (String[]) list.toArray(new String[list.size()]);
  }

  /**
   * fsplit - splits string using given delimiter if even if token is empty.
   * @param str - string to split
   * @param delim - string delimiter
   * @return array of strings
   */
  public static String[] fsplit(String str, String delim) {
    if (str == null) return null;
    str = str.trim();
    if (str.length() == 0) return new String[0];
    List list = new ArrayList();
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<str.length(); i++) {
      boolean isDelim = false;
      for (int j=0; j<delim.length(); j++) {
        if (str.charAt(i) == delim.charAt(j)) {
           isDelim = true;
           break;
        }
      }
      if (isDelim) {
        list.add(sb.toString());
        sb.setLength(0);
      } else {
        sb.append(str.charAt(i));
      }
    }
    list.add(sb.toString());
    return (String[]) list.toArray(new String[list.size()]);
  }


  /**
   * join joins array of tokens into one string
   * @param tokens - array of tokens
   * @return string
   */
  public static String join(String[] tokens) {
    if (tokens == null) return "";
    return join(tokens, 0, tokens.length, null);
  }


  /**
   * join joins array of tokens into one string
   * @param tokens - array of tokens
   * @param offset - string offset
   * @return string
   */
  public static String join(String[] tokens, int offset) {
    if (tokens == null) return "";
    return join(tokens, offset, tokens.length, null);
  }


  /**
   * join joins array of tokens into one string
   * @param tokens - array of tokens
   * @param delim - delimiter
   * @return string
   */
  public static String join(String[] tokens, String delim) {
    if (tokens == null) return "";
    return join(tokens, 0, tokens.length, delim);
  }


  /**
   * join joins array of tokens into one string
   * @param tokens - array of tokens
   * @param offset - string offset
   * @param delim - delimiter
   * @return string
   */
  public static String join(String[] tokens, int offset, String delim) {
    return join(tokens, offset, tokens.length, delim);
  }



  /**
   * join joins array of tokens into one string
   * @param tokens - array of tokens
   * @param offset - string offset
   * @param length - length of string
   * @param delim - delimiter
   * @return string
   */
  public static String join(String[] tokens, int offset, int length, 
        String delim) {
    if (tokens == null) return "";
    if (tokens.length == 0) return "";
    if (tokens.length == 1 && offset == 0) return tokens[0].trim();
    if (length > tokens.length) {
       throw new IllegalArgumentException("StringHelper.join() length is beyond array's size");
    }
    
    if (delim == null) delim = " ";
    StringBuilder buffer = new StringBuilder();
    for (int i=offset; tokens != null && i<length; i++) {
      buffer.append(tokens[i]);
      if (i < (length-1)) {
        buffer.append(delim);
      }
    }
    return buffer.toString();
  }



  /**
   * camel - camelize string
   * @param - original string
   * @return camelized string
   */
  public static String camel(String name) {
    return camel(name, " ");
  }

  /**
   * camel - camelize string
   * @param - original string
   * @param - delim - delimiter
   * @return camelized string
   */
  public static String camel(String name, String delim) {
    StringBuilder b = new StringBuilder(name.length());
    StringTokenizer t = new StringTokenizer(name, delim);
    b.append(t.nextToken());
    while (t.hasMoreTokens()) {
      String token = t.nextToken();
      b.append(token.substring(0, 1).toUpperCase()); // replace spaces with
                                                    // camelCase
      b.append(token.substring(1));
    }
    return b.toString();
  }

  /**
   * uncamel - uncamelize string
   * @param - name - original string
   * @param - delim - delimiter
   * @return camelized string
   */
  public static String uncamel(String name) {
    StringBuilder sb = new StringBuilder(name.length());
    StringBuilder last = new StringBuilder();
    for (int i=0; i<name.length(); i++) {
      char ch = name.charAt(i);
      if (Character.isUpperCase(ch)) {
        if (sb.length() > 0) sb.append(' ');
        sb.append(last.toString());
        last.setLength(0);
      }
      last.append(Character.toLowerCase(ch));
    }
    if (sb.length() > 0) sb.append(' ');
    sb.append(last.toString());
    return sb.toString();
  }


  /**
   * countLetter - counts character in given text
   * @param - text - original string
   * @param - ch - character
   * @return count of characters
   */
  public static int countLetter(String text, char ch) {
    int count = 0;
    for (int i=0; text != null && i<text.length(); i++) {
      if (text.charAt(i) == ch) count++;
    }
    return count;
  }

  /**
   * Emulates the method indexOf in StringBuilder JDK 1.4 in JDKs prior to that. :-(
   */
  public static int indexOfStringInStringBuilder(StringBuilder sb, String str) {

    int returnValue;

    returnValue = -1;

    if ((sb != null) && (str != null)) {
      returnValue = sb.toString().indexOf(str);  // TODO: There have to be better ways to do that.
    }

    return returnValue;
  }

  public static int indexOfTwoPartString(String code, String partOne, String partTwo, int startIndex) {
    int returnValue = -1;
    int possibleOne;
    int possibleTwo;
    int possibleIndex;
    int codeLength;
    boolean endOfCode;

    if ((code != null) && (partOne != null) && (partTwo != null)) {
      possibleIndex = Math.max(startIndex, 0);
      codeLength = code.length();
      endOfCode = false;
      while ((returnValue == -1) && (!endOfCode)) {
        possibleOne = code.indexOf(partOne, possibleIndex);  // Where does the first part start.

        if (possibleOne>-1) {

          possibleIndex = possibleOne + partOne.length();  // go over whitespaces
          possibleTwo   = code.indexOf(partTwo, possibleIndex);

          while ((possibleIndex < codeLength) && (Character.isWhitespace(code.charAt(possibleIndex)))){
            possibleIndex++;
          }

          if (possibleIndex == possibleTwo) {
            returnValue = possibleOne;
          }

        } else {
          endOfCode = true;
        }
      }

    } else {
      // exception
    }

    return returnValue;
  }

  public static String firstToUpper(String s) {

    StringBuilder sb = new StringBuilder(s);

    if (sb.length() > 0) {
      sb.replace(0, 1, "" + Character.toUpperCase(sb.charAt(0)));
    }

    return sb.toString();
  }

  public static String replaceVariables(String template, Properties properties) {

    String     returnValue = null;
    StringBuilder sb;
    boolean    foundSomething;
    boolean    foundKey;
    Enumeration  e;
    String     key, variable, replacement;
    int      idx, idxOld;
    int depth;

    if ((template != null) && (properties != null)) {
      depth = 0;
      sb = new StringBuilder(template);

      do {
        foundSomething = false;
        e        = properties.propertyNames();

        while (e.hasMoreElements()) {
          key    = (String) e.nextElement();
          variable = VARIABLE_START + key + VARIABLE_END;
          idxOld = -1;
          do {
            foundKey = false;
            idx    = StringHelper.indexOfStringInStringBuilder(sb, variable);

            if (idx >= 0) {
              if (idx<=idxOld) {
                depth++;
              }
              foundKey = true;
              foundSomething = true;
              replacement = properties.getProperty(key);
              idxOld = idx+replacement.length();
              sb.replace(idx, idx + variable.length(), replacement);
            }
          } while ((foundKey) && (depth<MAX_DEPTH));
        }
        depth++;
      } while ((foundSomething) && (depth<MAX_DEPTH));
      if (depth >= MAX_DEPTH) {
        throw new RuntimeException("Error in templates: To many recursions.");
      }

      returnValue = sb.toString();

    } else {
      // failed
    }
    return returnValue;
  }

  /**
   * Compares to StringBuilders by their content (and nothing else).
   * This is helpful, since two StringBuilder's may have the same
   * content, but the method equals returns false. It seems, some
   * other attributes of StringBuilder are considered as well, unfortuantely.
   */
  public static boolean haveEqualContent(StringBuilder a, StringBuilder b) {
    boolean returnValue = false;

    if ((a != null) && (b != null)) {
      returnValue = a.toString().equals(b.toString());
    } // no else

    if ((a == null) && (b == null)) {
      returnValue = true;
    } // no else

    return returnValue;
  }
}

