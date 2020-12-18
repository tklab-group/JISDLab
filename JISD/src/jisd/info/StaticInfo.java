package jisd.info;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class StaticInfo {
  String className;
  String name;
  String path;
  JSONObject classObjFromCD;
  JSONObject classObjFromPS;

  StaticInfo(String className, String name, String path, JSONObject cd, JSONObject ps) {
    this.className = className;
    this.name = name;
    this.path = path;
    this.classObjFromCD = cd;
    this.classObjFromPS = ps;
  }

  public String src() {
    Path path = Paths.get(path());
    String str;
    try {
      str = Files.readString(path);
    } catch (IOException e) {
      e.printStackTrace();
      return "";
    }
    return str;
  }

  public String path() {
    return path;
  }

  public String absPath() {
    return System.getProperty("user.dir") + File.separator + path;
  }

  public String className() {
    return className;
  }

  public String name() {
    return name;
  }

  public abstract void clearCache();
}
