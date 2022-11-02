package jisd.debug;

import jisd.util.Name;
import jisd.util.Print;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides debug utility.
 *
 * @author sugiyama
 */
public class Utility {

  @Getter @Setter
  public static String vscodeWorkspaceDir = "";
  static final Comparator<? super DebugResult> compDR =
    Comparator.comparing(dr -> ((DebugResult) dr).getLocation().className)
      .thenComparing(dr -> ((DebugResult) dr).getLocation().lineNumber)
      .thenComparing(dr -> ((DebugResult) dr).getLocation().varName);

  /**
   * Execute external command.
   *
   * @param command command (wildcard * is unavailable)
   * @return [stdout, stderr, exit code] (optional)
   */
  public static Optional<String[]> exec(String command) {
    String lineSeparator = java.lang.System.getProperty("line.separator");
    String[] results = new String[3];
    Arrays.fill(results, "");
    try {
      Process p = Runtime.getRuntime().exec(command);
      InputStream in = null;
      BufferedReader br = null;
      try {
        in = p.getInputStream();
        StringBuffer out = new StringBuffer();
        br = new BufferedReader(new InputStreamReader(in));
        String line;
        boolean isFirst = true;
        while ((line = br.readLine()) != null) {
          if (isFirst) {
            out.append(line);
            isFirst=false;
            continue;
          }
          out.append(lineSeparator + line);
        }
        results[0] = out.toString();
        br.close();
        in.close();
        in = p.getErrorStream();
        StringBuffer err = new StringBuffer();
        br = new BufferedReader(new InputStreamReader(in));
        isFirst = true;
        while ((line = br.readLine()) != null) {
          if (isFirst) {
            err.append(line);
            isFirst=false;
            continue;
          }
          err.append(lineSeparator + line);
        }
        results[1] = err.toString();
        results[2] = Integer.toString(p.waitFor());
        if (! results[0].isEmpty()) {
          java.lang.System.out.println(results[0]);
        }
        if (! results[1].isEmpty()) {
          java.lang.System.err.println(results[1]);
        }
      } finally {
        if (br != null) {
          br.close();
        }
        if (in != null) {
          in.close();
        }
      }
      p.waitFor();
      p.destroy();
      return Optional.of(results);
    } catch (IOException e) {
      e.printStackTrace();
      return Optional.empty();
    } catch (InterruptedException e) {
      DebuggerInfo.print("Interrupted.");
      return Optional.empty();
    }
  }

  /** Alias of System.out.println(). */
  public static void print(Object o) {
    System.out.print(o);
  }

  /** Alias of System.out.println(). */
  public static void println(Object o) {
    System.out.println(o);
  }

  /**
   * Sleep main thread
   *
   * @param sleepTime sleep time (milliseconds)
   */
  public static void sleep(int sleepTime) {
    try {
      Thread.sleep(sleepTime);
    } catch (InterruptedException e) {
      DebuggerInfo.print("Interrupted.");
    }
  }

  public static void prints(HashMap<String, DebugResult> drs) {
    drs.entrySet().stream().map(strDrs -> strDrs.getValue()).sorted(compDR).peek(dr -> Print.out(dr.lv())).collect(Collectors.toList());
  }

  public static String createUri(String text, String path) {
    return "["+text+"]("+path+")";
  }

  public static String uri(Location loc, List<String> srcDirs) {
    var lineNumber = loc.lineNumber;
    return uri(loc, srcDirs, lineNumber);
  }

  public static String uri(Location loc, List<String> srcDirs, int lineNumber) {
    var srcRelPath = Name.toSourcePathFromClassName(loc.className);
    srcDirs.add(".");
    for (int i = 0; i < srcDirs.size(); i++) {
      var srcDir = srcDirs.get(i);
      var srcAbsPathStr = srcDir + File.separator.charAt(0) + srcRelPath;
      var srcAbsPath = Paths.get(srcAbsPathStr);
      if (Files.exists(srcAbsPath)) {
        String text = srcAbsPathStr+"#L"+lineNumber;
        String path;
        if (vscodeWorkspaceDir.length() > 0) {
          path = srcAbsPathStr.replaceFirst(vscodeWorkspaceDir, "/") + "#L" + lineNumber;
        } else {
          path = srcAbsPathStr + "#L" + lineNumber;
        }
        return createUri(text, path);
      }
    }
    DebuggerInfo.printError(srcRelPath+" not found. Set srcDir by Debugger.setSrcDir(String... srcDir))");
    return null;
  }
}
