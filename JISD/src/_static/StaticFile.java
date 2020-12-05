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
  @Getter @Setter static String srcDir = ".";
  @Getter @Setter static String binDir = ".";
  @Getter static Optional<JSONObject> cd = Optional.empty();
  @Getter static Optional<JSONObject> ps = Optional.empty();
  // private static StaticFile me = new StaticFile();

  private StaticFile() {}

  public static void load(String srcDir, String binDir) {
    if (!srcDir.isBlank()) {
      setSrcDir((srcDir.endsWith(File.separator)) ? srcDir : srcDir + File.separator);
    }
    if (!binDir.isBlank()) {
      setBinDir((binDir.endsWith(File.separator)) ? binDir : binDir + File.separator);
    }
    setStaticFilePath();
    createStaticData();
    readStaticData();
  }

  static void setStaticFilePath() {
    setRootDirPath(binDir + rootDirName + File.separator);
    setClassDataFilePath(rootDirPath + "class_data.json");
    setProgramStructureFilePath(rootDirPath + "program_structure.json");
  }

  static void createStaticData() {
    String[] args = new String[2];
    args[0] = rootDirPath; // out
    args[1] = binDir; // in
    LysMain.main(args);
  }

  static void readStaticData() {
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
