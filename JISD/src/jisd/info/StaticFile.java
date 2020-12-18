package jisd.info;

import jisd.analysis.LysMain;
import jisd.util.Name;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Optional;

import static jisd.util.Json.readJsonFile;

public class StaticFile {
  @Getter private static final String rootDirName = ".jisd_static_data";
  @Getter static int count = 0;
  @Getter int number;
  @Setter String rootDirPath = rootDirName + File.separator;
  @Setter String classDataFilePath = rootDirPath + "class_data.json";
  @Setter String programStructureFilePath = rootDirPath + "program_structure.json";

  @Getter
  @Setter(AccessLevel.PACKAGE)
  String srcDir = ".";

  @Getter
  @Setter(AccessLevel.PACKAGE)
  String binDir = ".";

  @Getter Optional<JSONObject> cd = Optional.empty();
  @Getter Optional<JSONObject> ps = Optional.empty();
  // private static StaticFile me = new StaticFile();

  StaticFile(String srcDir, String binDir) {
    number = count++;
    load(srcDir, binDir);
  }

  void load(String srcDir, String binDir) {
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

  void setStaticFilePath() {
    setRootDirPath(binDir + rootDirName + number + File.separator);
    setClassDataFilePath(rootDirPath + "class_data.json");
    setProgramStructureFilePath(rootDirPath + "program_structure.json");
  }

  void createStaticData() {
    String[] args = new String[2];
    args[0] = rootDirPath; // out
    args[1] = binDir; // in
    LysMain.main(args);
  }

  void readStaticData() {
    readCd();
    readPs();
  }

  void readCd() {
    cd = readJsonFile(classDataFilePath);
  }

  void readPs() {
    ps = readJsonFile(programStructureFilePath);
  }

  JSONObject getClassObjFromCD(String className) {
    var packageAndClassName = Name.splitClassName(className);
    if (cd.isEmpty()) {
      throw new JSONException("");
    }
    var cdObj = cd.get();
    var packageObj = cdObj.getJSONObject(packageAndClassName.get("package"));
    var classObj = packageObj.getJSONObject(packageAndClassName.get("class"));
    return classObj;
  }

  JSONObject getClassObjFromPS(String className) {
    var packageAndClassName = Name.splitClassName(className);
    if (ps.isEmpty()) {
      throw new JSONException("");
    }
    var cdObj = ps.get();
    var packageObj = cdObj.getJSONObject(packageAndClassName.get("package"));
    var classObj = packageObj.getJSONObject(packageAndClassName.get("class"));
    return classObj;
  }
}
