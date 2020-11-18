/**
 * 
 */
package util;

import java.io.File;

/**
 * @author sugiyama
 *
 */
public class SrcUtil {
  /**
   * generate a class path from a source path
   * 
   * @param sp source path
   * @return class name
   */
  public static String toClassNameFromSourcePath(String sp) {
    String className = sp.replace(File.separator.charAt(0), '.');
    int length = className.length();
    if (className.substring(length - 5, length).equals(".java")) {
      return className.substring(0, length - 5);
    }
    return className;
  }
}
