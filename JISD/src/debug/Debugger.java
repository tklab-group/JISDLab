package debug;

import java.util.ArrayList;
import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnVMStart;
import org.jdiscript.util.VMLauncher;

import com.sun.jdi.AbsentInformationException;

/**
 * JISD's main debugger class.
 * @author sugiyama
 *
 */
public class Debugger {
	/** JDI */
	JDIScript j;
	/** Manage breakpoints */
	BreakPointManager bpm;
	/** Manage debug results */
	DebugResultManager drm;
	/** Target class setting items */
	String main, options;
	Thread jdiThread;
	JDIManager jdiManager;
	
	/**
	 * Constructor
	 * @param main A target class file name
	 * @param options A target class path setting
	 */
    public Debugger(String main, String options) {
		this.main = main;
		this.options = options;
		drm = new DebugResultManager();
		bpm = new BreakPointManager(drm);
	}
	
    /**
     * Set breakpoint with a line number.
     * @param lineNumber A line number in a target java file 
     */
	public void setBreakPoint(int lineNumber) {
		bpm.setBreakPoint(main, lineNumber);
	}
	
	public void setBreakPoint(String className, int lineNumber) {
		bpm.setBreakPoint(className, lineNumber);
	}
	
	/**
	 * Set breakpoint with a method name.
	 * @param methodName A method name in a target java file 
	 */
	public void setBreakPoint(String methodName) {
		bpm.setBreakPoint(main, methodName);
	}
	
	public void setBreakPoint(String className, String methodName) {
		bpm.setBreakPoint(className, methodName);
	}
	
	/**
	 * Remove breakpoint with a line number.
	 * @param lineNumber A line number in a target java file
	 */
	public void removeBreakPoint(int lineNumber) {
		bpm.removeBreakPoint(main, lineNumber);
	}
	
	public void removeBreakPoint(String className, int lineNumber) {
		bpm.removeBreakPoint(className, lineNumber);
	}
	
	/**
	 * Remove breakpoint with a method name.
	 * @param methodName A method name in a target java file
	 */
	public void removeBreakPoint(String methodName) {
		bpm.removeBreakPoint(main, methodName);
	}
	
	public void removeBreakPoint(String className, String methodName) {
		bpm.removeBreakPoint(className, methodName);
	}
	
	/**
	 * Set some settings(breakpoints) before the debugger runs.
	 * @return A procedure before the debugger runs 
	 */
	OnVMStart prepareStart() {
		OnVMStart start = se -> {
	        j.onClassPrep(p -> {
	        	if(p.referenceType().name().equals(main)) {
	        		bpm.requestSetBreakPoints();
	        	}
	        });
	    };
	    return start;
	}
	
	void vmInit() {
		j = new JDIScript(new VMLauncher(options, main).start());
		bpm.setJDI(j);
		OnVMStart start = prepareStart();
		jdiManager = new JDIManager(j, start);
		jdiThread = new Thread(jdiManager);
	}
	
	/**
	 * Start up the debugger.
	 * @param sleepTime Wait time after the debugger starts running 
	 */
	public void run(int sleepTime) {
		vmInit();
		jdiThread.start();
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Shutdown the debugger.
	 */
	public void exit() {
		jdiManager.shutdown();
	}
	
	/**
	 * Restart the debugger.
	 * @param sleepTime  Wait time after the debugger starts running
	 */
	public void restart(int sleepTime) {
		exit();
		drm.clearResults();
		run(sleepTime);
	}
	
	/**
	 * Get debug results. 
	 * @return Debug results 
	 */
	public ArrayList<DebugResult> getResults() {
		return drm.getResults();
	}
	
	/**
	 * Get line numbers a breakpoint sets.
	 * @return breakpoints
	 */
	public ArrayList<BreakPoint> getLineNumbers() {
		return new ArrayList<>(bpm.getLineNumbers());
	}
	
	/**
	 * Get method names a breakpoint sets.
	 * @return breakpoints
	 */
	public ArrayList<BreakPoint> getMethodNames() {
		return new ArrayList<>(bpm.getMethodNames());
	}
}
