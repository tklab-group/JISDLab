package debug;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnVMStart;

import com.sun.jdi.VMDisconnectedException;

/**
 * A target JavaVM manager class.
 * @author sugiyama
 *
 */
class VMManager implements Runnable {
	/** JDI */
	JDIScript j;
	/** A procedure before the debugger runs  */
	OnVMStart start;
	
	/**
	 * Constructor
	 * @param j JDI
	 * @param start A procedure before the debugger runs
	 */
	VMManager(JDIScript j, OnVMStart start) {
		this.j = j;
		this.start = start;
	}
	
	/**
	 * Run the debugger.
	 */
	public void run() {
		DebuggerInfo.print("VM started.");
		try {
		  j.run(start);
		} catch (VMDisconnectedException e) {
			/* Do nothing */
		}
	}
	
	void shutdown() {
		j.vm().exit(0);
		DebuggerInfo.print("VM exited.");
	}
}
