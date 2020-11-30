package _static;

import util.Name;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class StaticInfo {
  String className;
  String name;

  public StaticInfo(String className, String name) {
    this.className = className;
    this.name = name;
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
    return StaticFile.getSrcDir() + File.separator + Name.toSourcePathFromClassName(className);
  }

  public String absPath() {
    return System.getProperty("user.dir") + File.separator + path();
  }

  public String className() {
    return className;
  }

  public String name() {
    return name;
  }
}
