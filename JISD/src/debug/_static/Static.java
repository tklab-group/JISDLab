package debug._static;

import util.Name;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public abstract class Static {
  String className;
  String name;
  String srcDir;

  public Static(String srcDir, String className, String name) {
    this.srcDir = srcDir;
    this.className = className;
    this.name = name;
  }

  public String getSrcDir() {
    return srcDir;
  }

  public void setSrcDir(String srcDir) {
    this.srcDir = srcDir;
  }

  public String src() {
    Path path = Paths.get(path());
    ArrayList<String> lines;
    try {
      lines = (ArrayList) Files.readAllLines(path);
    } catch (IOException e) {
      e.printStackTrace();
      return "";
    }
    StringBuilder sb = new StringBuilder();
    lines.forEach(
        (l) -> {
          sb.append(l);
          sb.append("\n");
        });
    return sb.toString();
  }

  public String path() {
    return srcDir + File.separator + Name.toSourcePathFromClassName(className);
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
