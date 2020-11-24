/** */
package util;

import java.io.File;
import java.util.HashMap;

/** @author sugiyama */
public class Name {
  /**
   * generate a class path from a source path
   *
   * @param sp source path
   * @return class name
   */
  public static String toClassNameFromSourcePath(String sp) {
    String className = sp.replace('/', '.').replace(File.separator.charAt(0), '.');
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
    String srcPath = cn.replace('.', File.separator.charAt(0));
    srcPath += ".java";
    return srcPath;
  }

  public static HashMap<String, String> splitClassName(String name) {
    var cns = name.split("\\.");
    var cnsLen = cns.length;
    HashMap<String, String> map = new HashMap<>();
    if (cnsLen == 0) {
      map.put("package", "");
      map.put("class", "");
      return map;
    }
    // default package
    if (cnsLen == 1) {
      map.put("package", "default");
      map.put("class", cns[0]);
      return map;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(cns[0]);
    for (int i = 1; i < cnsLen - 1; i++) {
      sb.append(".");
      sb.append(cns[i]);
    }
    String packageName = sb.toString();
    String className = cns[cnsLen - 1];
    map.put("package", packageName);
    map.put("class", className);
    return map;
  }
}
