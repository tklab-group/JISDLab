package probej;

import util.Print;

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
      System.out.println(
          "java -agentpath:lib/ProbeJ_ex.dll=options_none:" + port + " " + options + " " + main);
      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    shutdown();
                  }));
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
      shutdown();
    }
  }

  public void shutdown() {
    if (p.isPresent()) {
      p.get().destroy();
      Print.out("VM shutdown.");
      p = Optional.empty();
    }
  }
}
