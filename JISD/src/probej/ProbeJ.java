/** */
package probej;

import com.sun.jdi.VMCannotBeModifiedException;
import debug.Debugger;
import debug.Location;
import debug.value.ValueInfo;
import util.Name;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

/** @author sugiyama */
public class ProbeJ {
  Connector connector;
  Optional<VirtualMachine> vm = Optional.empty();
  Optional<Thread> vmThread = Optional.empty();
  private boolean isVMStarted = false;

  public ProbeJ(String host, int port) {
    connector = new Connector(host, port);
  }

  public ProbeJ(String host, int port, VirtualMachine vm) {
    connector = new Connector(host, port);
    this.vm = Optional.ofNullable(vm);
  }

  public void runVM() {
    if (vm.isPresent()) {
      if (isVMStarted) {
        throw new VMCannotBeModifiedException("VM already started once.");
      }
      Thread tmp = new Thread(vm.get());
      tmp.start();
      vmThread = Optional.of(tmp);
      Debugger.sleep(800); // wait for vm start.
      System.out.println("VM with ProbeJ started.");
      isVMStarted = true;
    }
  }

  public void run() {
    if (connector.client != null && connector.client.isOpen()) {
      throw new VMCannotBeModifiedException("Connection already established.");
    }
    try {
      connector.openConnection();
      connector.sendCommand("PrintSocketOn");
    } catch (TimeoutException e) {
      e.printStackTrace();
    }
  }

  public void requestSetProbePoint(Location loc) {
    String cmd =
        "Set "
            + Name.splitClassName(loc.getClassName()).get("class")
            + ".java "
            + loc.getVarName()
            + " "
            + loc.getLineNumber();
    connector.sendCommand(cmd);
  }

  public HashMap<Location, ArrayList<ValueInfo>> getResults() {
    Location loc = new Location("", "", 0, "");
    return getResults(loc);
  }

  public HashMap<Location, ArrayList<ValueInfo>> getResults(Location loc) {
    return connector.getResults(loc);
  }

  Connector getConnector() {
    return connector;
  }

  public void exit() {
    connector.close();
    if (vm.isPresent()) {
      vm.get().shutdown();
      isVMStarted = false;
    }
    vm = Optional.empty();
    vmThread = Optional.empty();
  }

  public void requestRemoveProbePoint(Location loc) {
    String cmd =
        "Clear "
            + Name.splitClassName(loc.getClassName()).get("class")
            + ".java "
            + loc.getVarName()
            + " "
            + loc.getLineNumber();
    connector.sendCommand(cmd);
  }
}
