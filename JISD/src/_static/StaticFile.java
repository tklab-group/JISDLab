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
  @Setter static String rootDirPath = rootDirName + File.separator;
  @Setter static String classDataFilePath = rootDirPath + "class_data.json";
  @Setter static String programStructureFilePath = rootDirPath + "program_structure.json";
  @Getter @Setter static String srcDir = ".", binDir = ".";
  @Getter static Optional<JSONObject> cd = Optional.empty();
  @Getter static Optional<JSONObject> ps = Optional.empty();
  // private static StaticFile me = new StaticFile();

  private StaticFile() {}

  public static void init(String srcDir, String binDir) {
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

  static void setStaticFilePath(String binDir) {
    setRootDirPath(binDir + File.separator + rootDirName + File.separator);
    setClassDataFilePath(rootDirPath + "class_data.json");
    setProgramStructureFilePath(rootDirPath + "program_structure.json");
  }

  public static void createStaticData() {
    String[] args = new String[2];
    args[0] = rootDirPath;
    args[1] = binDir;
    LysMain.main(args);
  }

  public static void readStaticData() {
    readCd();
    readPs();
  }

  static void readCd() {
    cd = readJsonFile(classDataFilePath);
  }

  static void readPs() {
    ps = readJsonFile(programStructureFilePath);
  }
}
