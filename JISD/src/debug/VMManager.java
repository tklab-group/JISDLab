package debug;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnVMStart;

import com.sun.jdi.VMDisconnectedException;

/**
 * Target JavaVM manager
 * 
 * @author sugiyama
 *
 */
class VMManager implements Runnable {
  /** JDI */
  JDIScript j;
  /** A procedure before the debugger runs */
  OnVMStart start;

  /**
   * Constructor
   * 
   * @param j     JDI
   * @param start A procedure before the debugger runs
   */
  VMManager(JDIScript j, OnVMStart start) {
    this.j = j;
    this.start = start;
  }

  /**
   * Run the debugger.
   */
  @Override
  public void run() {
    DebuggerInfo.print("VM started.");
    try {
      j.run(start);
    } catch (VMDisconnectedException e) {
      /* Do nothing */
    }
  }

  /**
   * Shut down the debugger.
   */
  void shutdown() {
    try {
      j.vm().exit(0);
      DebuggerInfo.print("VM exited.");
    } catch (VMDisconnectedException e) {
      DebuggerInfo.print("VM already exited.");
    }
  }
}
