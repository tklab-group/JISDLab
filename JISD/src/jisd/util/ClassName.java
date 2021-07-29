package jisd.util;

import lombok.Getter;

/**
 * Provides a package name and a simple class name
 *
 * @author sugiyama
 */
public class ClassName {
  /** package name */
  @Getter
  private String packageName;
  /** simple class name */
  @Getter
  private String className;
  /** fully qualified class name */
  @Getter
  private String qualifiedClassName;

  public ClassName(String qualifiedClassName) {
    this.qualifiedClassName = qualifiedClassName;
    splitName(qualifiedClassName);
  }

  /**
   * split a fully qualified class name into a package name and a simple class name
   * @param qualifiedClassName
   */
  private void splitName(String qualifiedClassName) {
    var cns = qualifiedClassName.split("\\.");
    var cnsLen = cns.length;
    // empty name
    if (cnsLen == 0) {
      packageName = "";
      className = "";
      return;
    }
    // default package
    if (cnsLen == 1) {
      packageName = "default";
      className = cns[0];
      return;
    }
    // non-default package
    StringBuilder sb = new StringBuilder();
    sb.append(cns[0]);
    for (int i = 1; i < cnsLen - 1; i++) {
      sb.append(".");
      sb.append(cns[i]);
    }
    packageName = sb.toString();
    className = cns[cnsLen - 1];
    return;
  }
}
