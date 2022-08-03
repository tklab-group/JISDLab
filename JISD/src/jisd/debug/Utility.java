package jisd.debug;

import jisd.util.Print;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides debug utility.
 *
 * @author sugiyama
 */
public class Utility {

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

  public static void printDebugResults(HashMap<String, DebugResult> drs) {
    drs.entrySet().stream().map(strDrs -> strDrs.getValue()).sorted(compDR).peek(dr -> Print.out(dr.lv())).collect(Collectors.toList());
  }
}
