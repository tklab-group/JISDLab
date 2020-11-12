/**
 * 
 */
package debug;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnVMStart;

import com.sun.jdi.VMDisconnectedException;

/**
 * @author sugiyama
 *
 */
class JDIManager extends VMManager {
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
  JDIManager(JDIScript j, OnVMStart start) {
    this.j = j;
    this.start = start;
  }
  
  JDIManager(JDIScript j) {
    this(j, (s)->{});
  }
  
  void addStart(OnVMStart start) {
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
  
  JDIScript getJDI() {
    return j;
  }
  
  @Override
  void prepareStart(BreakPointManager bpm) {
    start = se -> {
      /* procedure when vm starts. */
      bpm.requestSetBreakPoints();
    };
    bpm.init(j);
  }
}
