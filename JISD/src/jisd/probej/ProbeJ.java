/** */
package jisd.probej;

import com.sun.jdi.VMCannotBeModifiedException;
import jisd.debug.Location;
import jisd.debug.Utility;
import jisd.debug.value.ValueInfo;
import jisd.util.ClassName;
import jisd.util.Name;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

/**
 * Provides ProbeJ client.
 *
 * @author sugiyama
 */
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

  /** Run a targetVM. */
  public void runVM() {
    if (vm.isPresent()) {
      if (isVMStarted) {
        throw new VMCannotBeModifiedException("VM already started once.");
      }
      Thread tmp = new Thread(vm.get());
      tmp.start();
      vmThread = Optional.of(tmp);
      Utility.sleep(800); // wait for vm start.
      System.out.println("VM with ProbeJ started.");
      isVMStarted = true;
    }
  }

  /** Start connection with ProbeJ. */
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

  /**
   * Request to set an observation point.
   *
   * @param loc location
   */
  public void requestSetProbePoint(Location loc) {
    String cmd =
        "Set "
            + (new ClassName(loc.getClassName())).getClassName()
            + ".java "
            + loc.getVarName()
            + " "
            + loc.getLineNumber();
    connector.sendCommand(cmd);
  }

  /**
   * Fetch value info from ProbeJ.
   *
   * @return
   */
  public HashMap<Location, ArrayList<ValueInfo>> getResults() {
    Location loc = new Location("", "", 0, "");
    return getResults(loc);
  }

  /**
   * Fetch value info from ProbeJ.
   *
   * @return
   */
  public HashMap<Location, ArrayList<ValueInfo>> getResults(Location loc) {
    return connector.getResults(loc);
  }

  Connector getConnector() {
    return connector;
  }

  /**
   * Close connection with ProbeJ. If a target VM was managed by this class, this class shutdowns
   * it.
   */
  public void exit() {
    connector.close();
    if (vm.isPresent()) {
      vm.get().shutdown();
      isVMStarted = false;
    }
    vm = Optional.empty();
    vmThread = Optional.empty();
  }

  /**
   * Request to remove an observation point.
   *
   * @param loc location
   */
  public void requestRemoveProbePoint(Location loc) {
    String cmd =
        "Clear "
            + (new ClassName(loc.getClassName())).getClassName()
            + ".java "
            + loc.getVarName()
            + " "
            + loc.getLineNumber();
    connector.sendCommand(cmd);
  }
}
