package probej;

import java.io.IOException;
import java.util.Optional;

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

  @Override
  public void run() {
    try {
      p =
          Optional.ofNullable(
              Runtime.getRuntime()
                  .exec(
                      "java -agentpath:lib/ProbeJ_ex.dll=options_none:"
                          + port
                          + " "
                          + options
                          + " "
                          + main));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void shutdown() {
    if (p.isPresent()) {
      p.get().destroy();
      p = Optional.empty();
    }
  }
}
