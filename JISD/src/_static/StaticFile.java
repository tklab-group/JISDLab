package _static;

import lombok.Getter;
import org.json.JSONException;
import org.json.JSONObject;
import util.Name;
import util.Print;
import util.Stream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

class StaticFile {
  static final String rootDir = "data" + File.separator;
  static final String methodDataFilePath = rootDir + "method_data.json";
  static final String programStructureFilePath = rootDir + "program_structure.json";

  @Getter static Optional<JSONObject> md;
  @Getter static Optional<JSONObject> ps;

  public StaticFile() {}

  public void readData() {
    readMd();
    readPs();
  }

  void readMd() {
    md = readJsonFile(methodDataFilePath);
  }

  void readPs() {
    ps = readJsonFile(programStructureFilePath);
  }

  Optional<JSONObject> readJsonFile(String jsonFilePath) {
    Path path = Paths.get(jsonFilePath);
    String jsonStr;
    try {
      jsonStr = Files.readString(path);
    } catch (IOException e) {
      e.printStackTrace();
      return Optional.empty();
    }
    JSONObject jsonObj = new JSONObject(jsonStr);
    return Optional.ofNullable(jsonObj);
  }

  static ArrayList<String> getMethodNames(String className) {
    var packageAndClassName = Name.splitClassName(className);
    if (ps.isEmpty()) {
      return new ArrayList<>();
    }
    var psObj = ps.get();
    try {
      var packageObj = psObj.getJSONObject(packageAndClassName.get("package"));
      var classObj = packageObj.getJSONObject("class");
      var names = (ArrayList<String>) classObj.keySet().stream().collect(Stream.toArrayList());
      return names;
    } catch (JSONException e) {
      Print.out("Class not found");
      return new ArrayList<>();
    }
  }
}
