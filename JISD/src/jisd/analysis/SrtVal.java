package jisd.analysis;

import java.util.Set;

/**
 * This class corresponds to one variable. Also analyze variable and get the type and whether it can
 * be set as breakpoint.
 *
 * @author khiranouchi
 * @author sugiyama
 */
class SrtVal {

  /** name of the variable (eg. "x", "sum", "val1") */
  String name;

  /**
   * kind of the variable type <br>
   * PRIMITIVE : can be set as breakpoint <br>
   * USERDEFINED: user-defined class (whether its field is can be set or not) <br>
   * OTHER: cannot be set as breakpoint
   */
  VariableType typeKind;

  /** the variable type name (eg. "int", "java.lang.String", "Student", "int[]", "int[][]") */
  String typeName; // 'L' and ';' removed

  /**
   * Create instance by analyzing the variable
   *
   * @param name name of the variable
   * @param desc descriptor of the variable
   * @param cnKeySet set of ClassNode key (used to get whether the variable type is user-defined or
   *     not)
   */
  public SrtVal(String name, String desc, Set<String> cnKeySet) {
    this.name = name;

    // get array dimention
    int dimen = 0;
    if (desc.charAt(dimen) == '[') {
      dimen++;
    }

    VariableType typeKindTmp;
    String typeNameTmp;

    char c = desc.charAt(dimen);
    if (c == 'L') {
      typeNameTmp = desc.substring(dimen + 1, desc.length() - 1); // remove 'L' and ';'
      if (cnKeySet.contains(typeNameTmp)) {
        typeKindTmp = VariableType.USERDEFINED; // userclass
      } else {
        typeKindTmp = VariableType.USERDEFINED;
        // TODO: typeKindTmp = VariableType.OTHER;// cannot
        typeNameTmp = typeNameTmp.replace('/', '.');
      }
    } else {
      typeKindTmp = VariableType.PRIMITIVE; // can
      switch (c) {
        case 'B':
          typeNameTmp = "bool";
          break;
        case 'C':
          typeNameTmp = "char";
          break;
        case 'D':
          typeNameTmp = "double";
          break;
        case 'F':
          typeNameTmp = "float";
          break;
        case 'I':
          typeNameTmp = "int";
          break;
        case 'J':
          typeNameTmp = "long";
          break;
        case 'S':
          typeNameTmp = "short";
          break;
        case 'Z':
          typeNameTmp = "boolean";
          break;
        default: // 'V'
          typeNameTmp = "void";
      }
    }

    // take array
    if (dimen > 0) {
      typeKindTmp = VariableType.OTHER; // cannot
      for (int i = 0; i < dimen; i++) {
        typeNameTmp += "[]";
      }
    }

    this.typeKind = typeKindTmp;
    this.typeName = typeNameTmp;
  }
}
