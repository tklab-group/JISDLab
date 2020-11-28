package _static;

import analysis.LysMain;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.io.File;
import java.util.Optional;

import static util.Json.readJsonFile;

public class StaticFile {
  @Setter static String rootDir = "data" + File.separator;
  @Setter static String methodDataFilePath = rootDir + "class_data.json";
  @Setter static String programStructureFilePath = rootDir + "program_structure.json";
  @Getter @Setter static String srcDir = ".", binDir = ".";

  @Getter static Optional<JSONObject> cd = Optional.empty();
  @Getter static Optional<JSONObject> ps = Optional.empty();

  public StaticFile(String srcDir, String binDir) {
    init(srcDir, binDir);
  }

  public void init(String srcDir, String binDir) {
    if (!srcDir.isBlank()) {
      setSrcDir(srcDir);
    }
    if (!binDir.isBlank()) {
      setBinDir(binDir);
    }
    setStaticFilePath(binDir);
    createStaticData();
    readStaticData();
  }

  void setStaticFilePath(String binDir) {
    setRootDir(binDir + File.separator + "data" + File.separator);
    setMethodDataFilePath(rootDir + "class_data.json");
    setProgramStructureFilePath(rootDir + "program_structure.json");
  }

  public void createStaticData() {
    String[] args = new String[2];
    args[0] = binDir + "/data/";
    args[1] = binDir;
    LysMain.main(args);
  }

  public void readStaticData() {
    readCd();
    readPs();
  }

  void readCd() {
    cd = readJsonFile(methodDataFilePath);
  }

  void readPs() {
    ps = readJsonFile(programStructureFilePath);
  }
}
