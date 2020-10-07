package debug;

import java.util.ArrayList;
import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnVMStart;
import org.jdiscript.util.VMLauncher;
import org.jdiscript.util.VMSocketAttacher;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.VirtualMachine;

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
	String main, options, srcDir;
	Thread jdiThread;
	VMManager vmManager;
	
	boolean isRemoteDebug = false;
	int port = 0; 
	
	/**
	 * Constructor
	 * @param main A target class file name
	 * @param options A target class path setting
	 */
    public Debugger(String main, String options, String srcDir) {
		this.main = main;
		this.options = options;
		this.srcDir = srcDir;
		drm = new DebugResultManager();
		bpm = new BreakPointManager(drm);
	}
    
    public Debugger(String main, String options) {
    	this(main, options, ".");
    }
    
    public Debugger(String main, String srcDir, int port) { 
    	this(main, "", srcDir);
    	this.isRemoteDebug = true;
    	if (port < 1024 || port > 65535) {
    		this.port = 8000;
    		DebuggerInfo.printError("This port is out of range. So, now port is set 8000. Please set the port bewtween 1024 ~ 65535.");
    	} else {
    	    this.port = port;
    	}
    }
    
    public Debugger(String main, int port) {
    	this(main, "", port);
    }
    
    public void setMaxRecordNumber(int number) {
    	drm.setMaxRecordNoOfValue(number);
    }
	
    /**
     * Set breakpoint with a line number.
     * @param lineNumber A line number in a target java file 
     */
	public void setBreakPoint(int lineNumber) {
		setBreakPoint(main, lineNumber);
	}
	
	public void setBreakPoint(int lineNumber, ArrayList<String> varName) {
		setBreakPoint(main, lineNumber, varName);
	}
	
	public void setBreakPoint(String className, int lineNumber) {
		setBreakPoint(className, lineNumber, new ArrayList<String>());
	}
	
	public void setBreakPoint(String className, int lineNumber, ArrayList<String> varNames) {
		bpm.setBreakPoint(className, lineNumber, varNames, true);
	}
	
	/**
	 * Set breakpoint with a method name.
	 * @param methodName A method name in a target java file 
	 */
	public void setBreakPoint(String methodName) {
		setBreakPoint(main, methodName);
	}
	
	public void setBreakPoint(String methodName, ArrayList<String> varNames) {
		setBreakPoint(main, methodName, varNames);
	}
	
	public void setBreakPoint(String className, String methodName) {
		setBreakPoint(className, methodName, new ArrayList<String>());
	}
	
	public void setBreakPoint(String className, String methodName, ArrayList<String> varName) {
		bpm.setBreakPoint(className, methodName, varName, true);
	}	
	
	/**
     * Set watchpoint with a line number.
     * @param lineNumber A line number in a target java file 
     */
	public void setWatchPoint(int lineNumber) {
		setWatchPoint(main, lineNumber);
	}
	
	public void setWatchPoint(int lineNumber, ArrayList<String> varName) {
		setWatchPoint(main, lineNumber, varName);
	}
	
	public void setWatchPoint(String className, int lineNumber) {
		setWatchPoint(className, lineNumber, new ArrayList<String>());
	}
	
	public void setWatchPoint(String className, int lineNumber, ArrayList<String> varNames) {
		bpm.setBreakPoint(className, lineNumber, varNames, false);
	}
	
	/**
	 * Set watchpoint with a method name.
	 * @param methodName A method name in a target java file 
	 */
	public void setWatchPoint(String methodName) {
		setWatchPoint(main, methodName);
	}
	
	public void setWatchPoint(String methodName, ArrayList<String> varNames) {
		setWatchPoint(main, methodName, varNames);
	}
	
	public void setWatchPoint(String className, String methodName) {
		setWatchPoint(className, methodName, new ArrayList<String>());
	}
	
	public void setWatchPoint(String className, String methodName, ArrayList<String> varName) {
		bpm.setBreakPoint(className, methodName, varName, false);
	}	
	
	public void cont() {
	    bpm.resumeThread();	
	}
	
	public void list() {
		bpm.printSrcAtCurrentLocation("Current location,", srcDir);
	}
	
	public void classes(String className) {
		j.vm().allClasses().stream().filter(cls -> cls.name().contains(className)).forEach(cls -> {
			System.out.println(cls.name());
		});
	}
	
	public void methods(String className) {
		j.vm().classesByName(className).forEach(cls -> {
			cls.allMethods().forEach(methods -> {
				System.out.println(methods.name());	
			});
		});
	}
	
	public void fields(String className) {
		j.vm().classesByName(className).forEach(cls -> {
			cls.allFields().forEach(fields -> {
				System.out.println(fields.name());	
			});
		});
	}
	
	public void locals() {
		bpm.printLocals();
	}
	
	public void stop(int lineNumber) {
		BreakPoint bp = bpm.setBreakPoint(main, lineNumber, new ArrayList<String>(), true);
		if (bp != null) { 
		    bpm.requestSetBreakPoint(bp);
		}
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
		VirtualMachine vm;
		if (isRemoteDebug) {
			vm = new VMSocketAttacher(port).attach();
		} else {
			vm = new VMLauncher(options, main).start();
		}
		j = new JDIScript(vm);
		bpm.setJDI(j);
		OnVMStart start = prepareStart();
        vmManager = new VMManager(j, start);
		jdiThread = new Thread(vmManager);
	}
	
	/**
	 * Start up the debugger.
	 * @param sleepTime Wait time after the debugger starts running 
	 */
	public void run(int sleepTime) {
		vmInit();
		jdiThread.start();
		sleep(sleepTime);
	}
	
	public void sleep(int sleepTime) {
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
		vmManager.shutdown();
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
	 * Get breakpoints.
	 * @return breakpoints
	 */
	public ArrayList<BreakPoint> getBreakPoints() {
		return new ArrayList<>(bpm.getBreakPoints());
	}
}
