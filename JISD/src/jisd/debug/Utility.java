package jisd.debug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Optional;

/**
 * Provides debug utility.
 *
 * @author sugiyama
 */
public class Utility {
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
        while ((line = br.readLine()) != null) {
          out.append(line + lineSeparator);
        }
        results[0] = out.toString();
        br.close();
        in.close();
        in = p.getErrorStream();
        StringBuffer err = new StringBuffer();
        br = new BufferedReader(new InputStreamReader(in));
        while ((line = br.readLine()) != null) {
          err.append(line + lineSeparator);
        }
        results[1] = err.toString();
        results[2] = Integer.toString(p.waitFor());
        java.lang.System.out.print(results[0]);
        java.lang.System.err.print(results[1]);
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
}
