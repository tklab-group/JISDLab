package jisd.probej;

import jisd.util.Print;

import java.io.IOException;
import java.util.Optional;

/**
 * Manages VM with ProbeJ.
 *
 * @author sugiyama
 */
public class VirtualMachine implements Runnable {
  String main;
  String options;
  int port;
  Optional<Process> p = Optional.empty();

  public VirtualMachine(String main, String options, int port) {
    this.main = main;
    this.options = options;
    this.port = port;
  }

  /** Run a target VM. */
  @Override
  public void run() {
    try {
      String JISD_HOME = System.getenv("JISDLAB_HOME").replace('\\', '/') + "/JISD/";
      String cmd =
          "java -agentpath:"
              + JISD_HOME
              + "lib/ProbeJ_ex.dll="
              + JISD_HOME
              + "lib/options_none:"
              + port
              + " "
              + options
              + " "
              + main;
      System.out.println(cmd);
      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    shutdown();
                  }));
      p = Optional.ofNullable(Runtime.getRuntime().exec(cmd));
    } catch (IOException e) {
      e.printStackTrace();
      shutdown();
    }
  }

  /** Shutdown a target VM. */
  public void shutdown() {
    if (p.isPresent()) {
      p.get().destroy();
      Print.out("VM with ProbeJ exited.");
      p = Optional.empty();
    }
  }
}
