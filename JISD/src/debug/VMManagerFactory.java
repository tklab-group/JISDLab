/**
 * 
 */
package debug;

import org.jdiscript.JDIScript;
import org.jdiscript.util.VMLauncher;
import org.jdiscript.util.VMSocketAttacher;

import com.sun.jdi.VirtualMachine;

import probej.ProbeJ;

/**
 * @author sugiyama
 *
 */
class VMManagerFactory {
  static VMManager create(String main, String options, String host, int port, boolean isRemoteDebug, boolean usesProbeJ) {
    // ProbeJ
    if (usesProbeJ) {
      DebuggerInfo.print("Try to connect to "+host+":"+port);
      ProbeJ probeJ = new ProbeJ(host, port);
      DebuggerInfo.print("Successflly connected to "+host+":"+port);
      return new ProbeJManager(probeJ);
    }
    
    // JDI
    VirtualMachine vm;
    if (isRemoteDebug) {
      DebuggerInfo.print("Try to connect to "+host+":"+port);
      vm = new VMSocketAttacher(host, port).attach();
      DebuggerInfo.print("Successflly connected to "+host+":"+port);
    } else {
      vm = new VMLauncher(options, main).start();
    }
    JDIScript j = new JDIScript(vm);
    return new JDIManager(j);
  }
}
