/** */
package probej;

import debug.Location;
import debug.value.ValueInfo;
import util.Name;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

/** @author sugiyama */
public class ProbeJ {
  Connector connector;
  Optional<VirtualMachine> vm = Optional.empty();
  Optional<Thread> vmThread = Optional.empty();

  public ProbeJ(String host, int port) {
    connector = new Connector(host, port);
  }

  public ProbeJ(String host, int port, VirtualMachine vm) {
    connector = new Connector(host, port);
    this.vm = Optional.ofNullable(vm);
  }

  public void run() {
    try {
      if (vm.isPresent()) {
        Thread tmp = new Thread(vm.get());
        tmp.start();
        vmThread = Optional.of(tmp);
      }
      connector.openConnection();
      connector.sendCommand("PrintSocketOn");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void requestSetProbePoint(String className, String varName, int lineNumber) {
    String cmd =
        "Set "
            + Name.splitClassName(className).get("class")
            + ".java "
            + varName
            + " "
            + lineNumber;
    connector.sendCommand(cmd);
  }

  public HashMap<Location, ArrayList<ValueInfo>> getResults() {
    return getResults("", "", 0);
  }

  public HashMap<Location, ArrayList<ValueInfo>> getResults(
      String className, String varName, int lineNumber) {
    return connector.getResults(Name.splitClassName(className).get("class"), varName, lineNumber);
  }

  Connector getConnector() {
    return connector;
  }

  public void exit() {
    connector.close();
    if (vm.isPresent()) {
      vm.get().shutdown();
    }
    vm = Optional.empty();
    vmThread = Optional.empty();
  }
}
