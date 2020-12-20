package jisd.info;

import jisd.analysis.LysMain;
import jisd.util.Name;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
  ArrayList<String> paths = new ArrayList<>();

  @Getter Optional<JSONObject> cd = Optional.empty();
  @Getter Optional<JSONObject> ps = Optional.empty();
  // private static StaticFile me = new StaticFile();

  StaticFile(String srcDir, String... paths) {
    number = count++;
    load(srcDir, paths);
  }

  void load(String srcDir, String... bins) {
    setStaticFilePath();
    if (!srcDir.isBlank()) {
      setSrcDir(formatDirName(srcDir));
    }
    paths = new ArrayList<>();
    paths.add(rootDirPath); // in
    Arrays.stream(bins)
        .forEach(
            bin -> {
              if (!bin.isBlank()) {
                paths.add(formatDirName(bin));
              }
            });
    createStaticData();
    readStaticData();
  }

  void setStaticFilePath() {
    setRootDirPath(rootDirName + File.separator + number + File.separator);
    setClassDataFilePath(rootDirPath + "class_data.json");
    setProgramStructureFilePath(rootDirPath + "program_structure.json");
  }

  void createStaticData() {
    LysMain.main((paths.toArray(new String[0])));
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

  String formatDirName(String oldDir) {
    String newDir;
    if (oldDir.endsWith(File.separator) || oldDir.endsWith(".jar")) {
      newDir = oldDir;
    } else {
      newDir = oldDir + File.separator;
    }
    return newDir;
  }
}
