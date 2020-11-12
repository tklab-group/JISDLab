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
abstract class VMManager implements Runnable {

  /**
   * Run the debugger.
   */
  @Override
  abstract public void run();

  /**
   * Shut down the debugger.
   */
  abstract void shutdown();
  
  abstract void prepareStart(BreakPointManager bpm);
}
