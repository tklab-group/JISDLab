package _static;

import lombok.Getter;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

class StaticFile {
  static final String rootDir = "data" + File.separator;
  static final String methodDataFilePath = rootDir + "class_data.json";
  static final String programStructureFilePath = rootDir + "program_structure.json";

  @Getter static Optional<JSONObject> cd;
  @Getter static Optional<JSONObject> ps;

  public StaticFile() {
    readData();
  }

  public void readData() {
    readCd();
    readPs();
  }

  void readCd() {
    cd = readJsonFile(methodDataFilePath);
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
}
