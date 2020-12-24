package jisd.info;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Static data information. */
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

  /** Get source contents. */
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

  /** Get the relative path this item is declared. */
  public String path() {
    return path;
  }

  /** Get the absolute path this item is declared. */
  public String absPath() {
    return System.getProperty("user.dir") + File.separator + path;
  }

  /** Get a class name. */
  public String className() {
    return className;
  }

  /** Get this item's name. */
  public String name() {
    return name;
  }

  /** Clear cache data. */
  public abstract void clearCache();
}
