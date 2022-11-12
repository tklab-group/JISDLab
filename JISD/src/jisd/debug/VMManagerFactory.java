/** */
package jisd.debug;

import com.sun.jdi.VirtualMachine;
import jisd.analysis.FaultFinder;
import jisd.probej.ProbeJ;
import org.jdiscript.JDIScript;
import org.jdiscript.util.VMLauncher;
import org.jdiscript.util.VMSocketAttacher;

import static jisd.debug.Utility.*;

/**
 * Creates VMManager.
 *
 * @author sugiyama
 */
class VMManagerFactory {
  public static int timeOut = 10000;
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
    VirtualMachine vm = null;
    /** target VM Thread*/
    Thread targetVmThread = null;
    if (isRemoteDebug) {
      if (projectName.length() > 0 && projectId.length() > 0) {
        String cmd = FaultFinder.jisdCmdPath + " debug " + projectName + " " + projectId;
        targetVmThread = new Thread(()->{exec(cmd, false);});;
        targetVmThread.start();
      }
      DebuggerInfo.print("Try to connect to " + host + ":" + port);
      boolean isConnecting = false;
      int times = 0;
      int sleepTime = 1000;
      while (!isConnecting) {
        try {
          vm = new VMSocketAttacher(host, port).attach();
          isConnecting = true;
        } catch (RuntimeException e) {
          sleep(sleepTime);
          times++;
          if (sleepTime*times >= timeOut) {
            throw e;
          }
        }
      }
      DebuggerInfo.print("Successflly connected to " + host + ":" + port);
    } else {
      vm = new VMLauncher(options, main).start();
    }
    JDIScript j = new JDIScript(vm);
    return new JDIManager(debugger, j, isRemoteDebug, targetVmThread);
  }
}
