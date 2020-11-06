package analysis;

import java.util.Set;

/**
 * This class corresponds to one valuable. Also analyze valuable and get the
 * type and whether it can be set as breakpoint.
 */
public class SrtVal {

  /**
   * name of the valuable (eg. "x", "sum", "val1")
   */
  public String name;

  /**
   * kind of the valuable type <br>
   * 0: can be set as breakpoint <br>
   * 1: user-defined class (whether its field is can be set or not) <br>
   * 2: cannot be set as breakpoint
   */
  public int typekind;

  /**
   * the valuable type name (eg. "int", "java.lang.String", "Student", "int[]",
   * "int[][]")
   */
  public String typename;// 'L' and ';' removed

  /**
   * Create instance by analyzing the valuable
   * 
   * @param name     name of the valuable
   * @param desc     descriptor of the valuable
   * @param cnKeySet set of ClassNode key (used to get whether the valuable type
   *                 is user-defined or not)
   */
  public SrtVal(String name, String desc, Set<String> cnKeySet) {
    this.name = name;

    // get array dimention
    int dimen = 0;
    if (desc.charAt(dimen) == '[')
      dimen++;

    int typekind_tmp;
    String typename_tmp;

    char c = desc.charAt(dimen);
    if (c == 'L') {
      typename_tmp = desc.substring(dimen + 1, desc.length() - 1);// remove 'L' and ';'
      if (cnKeySet.contains(typename_tmp)) {
        typekind_tmp = 1;// userclass
      } else {
        typekind_tmp = 2;// cannot
        typename_tmp = typename_tmp.replace('/', '.');
      }
    } else {
      typekind_tmp = 0;// can
      switch (c) {
      case 'B':
        typename_tmp = "bool";
        break;
      case 'C':
        typename_tmp = "char";
        break;
      case 'D':
        typename_tmp = "double";
        break;
      case 'F':
        typename_tmp = "float";
        break;
      case 'I':
        typename_tmp = "int";
        break;
      case 'J':
        typename_tmp = "long";
        break;
      case 'S':
        typename_tmp = "short";
        break;
      case 'Z':
        typename_tmp = "boolean";
        break;
      default:// 'V'
        typename_tmp = "void";
      }
    }

    // take array
    if (dimen > 0) {
      typekind_tmp = 2;// cannot
      for (int i = 0; i < dimen; i++)
        typename_tmp += "[]";
    }

    this.typekind = typekind_tmp;
    this.typename = typename_tmp;
  }
}
