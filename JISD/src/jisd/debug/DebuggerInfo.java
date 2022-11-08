package jisd.debug;

import lombok.Getter;
import lombok.Setter;

/**
 * Provides how to show debugger infomation.
 *
 * @author sugiyama
 */
public class DebuggerInfo {
  @Getter @Setter
  public static boolean isVerbose = true;

  /**
   * Prints debugger infomation to stdout.
   *
   * @param message debugger infomation
   */
  static void print(String message) {
    if (isVerbose) {
      System.out.println(">> Debugger Info: " + message);
    }
  }

  /**
   * Prints debugger information to stderr.
   *
   * @param message debugger infomation
   */
  static void printError(String message) {
    if (isVerbose) {
      System.err.println(">> Debugger Info: " + message);
    }
  }

}
