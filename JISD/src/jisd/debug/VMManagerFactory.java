/** */
package jisd.debug;

import com.sun.jdi.VirtualMachine;
import jisd.analysis.FaultFinder;
import jisd.probej.ProbeJ;
import org.jdiscript.JDIScript;
import org.jdiscript.util.VMLauncher;
import org.jdiscript.util.VMSocketAttacher;

import static jisd.debug.Utility.exec;
import static jisd.debug.Utility.sleep;

/**
 * Creates VMManager.
 *
 * @author sugiyama
 */
class VMManagerFactory {
  static VMManager create(
      Debugger debugger,
      String main,
      String options,
      String host,
      int port,
      boolean isRemoteDebug,
      boolean usesProbeJ,
      String projectName,
      String projectId) {
    // ProbeJ
    if (usesProbeJ) {
      ProbeJ probeJ;
      if (!isRemoteDebug) {
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
          jisd.probej.VirtualMachine vm = new jisd.probej.VirtualMachine(main, options, port);
          probeJ = new ProbeJ(host, port, vm);
        } else {
          throw new IllegalArgumentException(
              "ProbeJ Not Supported for " + System.getProperty("os.name") + ".");
        }
      } else {
        probeJ = new ProbeJ(host, port);
      }
      return new ProbeJManager(debugger, probeJ);
    }

    // JDI
    VirtualMachine vm;
    /** target VM Thread*/
    Thread targetVmThread = null;
    if (isRemoteDebug) {
      if (projectName.length() > 0 && projectId.length() > 0) {
        String cmd = FaultFinder.jisdCmdPath + " debug " + projectName + " " + projectId;
        targetVmThread = new Thread(()->{exec(cmd);});;
        targetVmThread.start();
        sleep(Debugger.defaultSleepTime);
      }
      DebuggerInfo.print("Try to connect to " + host + ":" + port);
      vm = new VMSocketAttacher(host, port).attach();
      DebuggerInfo.print("Successflly connected to " + host + ":" + port);
    } else {
      vm = new VMLauncher(options, main).start();
    }
    JDIScript j = new JDIScript(vm);
    return new JDIManager(debugger, j, isRemoteDebug, targetVmThread);
  }
}
