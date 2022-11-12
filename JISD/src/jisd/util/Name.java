package jisd.util;

import java.io.File;

/**
 * Provides utility methods related to a name, Ex. class name, path name
 *
 * @author sugiyama
 */
public final class Name {
  private Name() {}

  /**
   * generate a class path from a source path
   *
   * @param sp source path
   * @return class name
   */
  public static String toClassNameFromSourcePath(String sp) {
    if (sp == null) {
      return "";
    }
    String className = sp.replace('/', '.').replace(File.separatorChar, '.');
    int length = className.length();
    if (className.substring(length - 5, length).equals(".java")) {
      return className.substring(0, length - 5);
    }
    while (className.length() > 0 && className.charAt(className.length() - 1) == '.') {
      className = className.substring(0, className.length() - 1);
    }
    return className;
  }

  public static String toSourcePathFromClassName(String cn) {
    String srcPath = cn.replace('.', File.separatorChar);
    srcPath += ".java";
    return srcPath;
  }

  public static String getMethodNameFromFullMethodName(String fullName) {
    var names = fullName.split("\\.");
    boolean isStartMethodName = false;
    StringBuilder result= new StringBuilder();
    for (int i = 0; i < names.length; i++) {
      if (names[i].contains("(")) {
        isStartMethodName = true;
      }
      if (isStartMethodName) {
        result.append(names[i]);
      }
    }
    return result.toString();
  }
}
