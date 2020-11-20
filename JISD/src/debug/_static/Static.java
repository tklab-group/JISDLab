package debug._static;

import util.Name;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

public class Static {
  final String className;
  HashSet<String> methodNames;
  HashSet<String> fieldNames;
  String srcDir;
  final String separator = File.separator;

  public Static(String className) {
    this(".", className);
  }

  public Static(String srcDir, String className) {
    this.srcDir = srcDir;
    this.className = className;
  }

  String getSrcDir() {
    return srcDir;
  }

  void setSrcDir(String srcDir) {
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
    return srcDir + separator + Name.toSourcePathFromClassName(className);
  }

  public String absPath() {
    return System.getProperty("user.dir") + separator + path();
  }
}
