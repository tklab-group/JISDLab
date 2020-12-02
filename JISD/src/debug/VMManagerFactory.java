/** */
package debug;

import com.sun.jdi.VirtualMachine;
import org.jdiscript.JDIScript;
import org.jdiscript.util.VMLauncher;
import org.jdiscript.util.VMSocketAttacher;
import probej.ProbeJ;

/** @author sugiyama */
class VMManagerFactory {
  static VMManager create(
      String main,
      String options,
      String host,
      int port,
      boolean isRemoteDebug,
      boolean usesProbeJ) {
    // ProbeJ
    if (usesProbeJ) {
      ProbeJ probeJ;
      if (!isRemoteDebug) {
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
          probej.VirtualMachine vm = new probej.VirtualMachine(main, options, port);
          probeJ = new ProbeJ(host, port, vm);
        } else {
          DebuggerInfo.printError(
              "ProbeJ Not Supported for " + System.getProperty("os.name") + ".");
          throw new IllegalArgumentException();
        }
      } else {
        probeJ = new ProbeJ(host, port);
      }
      return new ProbeJManager(probeJ);
    }

    // JDI
    VirtualMachine vm;
    if (isRemoteDebug) {
      DebuggerInfo.print("Try to connect to " + host + ":" + port);
      vm = new VMSocketAttacher(host, port).attach();
      DebuggerInfo.print("Successflly connected to " + host + ":" + port);
    } else {
      vm = new VMLauncher(options, main).start();
    }
    JDIScript j = new JDIScript(vm);
    return new JDIManager(j);
  }
}
