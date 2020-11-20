package debug._static;

import java.io.File;
import java.util.ArrayList;

class StaticFile {
  static final String rootDir = "data" + File.separator;
  static final String methodDataFilePath = rootDir + "method_data.json";
  static final String programStructureFilePath = rootDir + "program_structure.json";

  static ArrayList<String> getMethodNames(String className) {
    return null;
  }

  static ArrayList<String> getFieldNames(String className) {
    return null;
  }

  static ArrayList<String> getLocalNames(String className) {
    return null;
  }
}
