package _static;

import analysis.LysMain;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.io.File;
import java.util.Optional;

import static util.Json.readJsonFile;

public class StaticFile {
  private static final String rootDirName = ".jisd_static_data";
  @Setter String rootDirPath = rootDirName + File.separator;
  @Setter String classDataFilePath = rootDirPath + "class_data.json";
  @Setter String programStructureFilePath = rootDirPath + "program_structure.json";
  @Getter @Setter String srcDir = ".", binDir = ".";

  @Getter Optional<JSONObject> cd = Optional.empty();
  @Getter Optional<JSONObject> ps = Optional.empty();

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
    setRootDirPath(binDir + File.separator + rootDirName + File.separator);
    setClassDataFilePath(rootDirPath + "class_data.json");
    setProgramStructureFilePath(rootDirPath + "program_structure.json");
  }

  public void createStaticData() {
    String[] args = new String[2];
    args[0] = rootDirPath;
    args[1] = binDir;
    LysMain.main(args);
  }

  public void readStaticData() {
    readCd();
    readPs();
  }

  void readCd() {
    cd = readJsonFile(classDataFilePath);
  }

  void readPs() {
    ps = readJsonFile(programStructureFilePath);
  }
}