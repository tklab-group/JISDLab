package debug;

import java.util.ArrayList;
import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnVMStart;
import org.jdiscript.util.VMLauncher;
import org.jdiscript.util.VMSocketAttacher;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.VirtualMachine;

/**
 * JISD's main debugger
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
	/** JDI thread */
	Thread jdiThread;
	/** VM manager */
	VMManager vmManager;
	/** use attaching connector or not */
	boolean isRemoteDebug = false;
	/** attaching port */
	int port = 0; 
	
	/**
	 * Constructor
	 * @param main A target class file name
	 * @param options A target class path setting
	 * @param srcDir source directory
	 */
    public Debugger(String main, String options, String srcDir) {
		this.main = main;
		this.options = options;
		this.srcDir = srcDir;
		drm = new DebugResultManager();
		bpm = new BreakPointManager(drm);
	}
    
    /**
     * Constructor
     * @param main A target class file name
     * @param options A target class path setting
     */
    public Debugger(String main, String options) {
    	this(main, options, ".");
    }
    
    /**
     * Constructor
     * @param main A target class file name
     * @param srcDir source directory
     * @param port attaching port
     */
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
    
    /**
     * Constructor
     * @param main A target class file name
     * @param port attaching port
     */
    public Debugger(String main, int port) {
    	this(main, "", port);
    }
    
    /**
     * Set the max record number of values
     * @param number the max record number of values
     */
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
	
	/**
	 * Set breakpoint with a line number and variable names.
	 * @param lineNumber line number
	 * @param varNames variable names
	 */
	public void setBreakPoint(int lineNumber, ArrayList<String> varNames) {
		setBreakPoint(main, lineNumber, varNames);
	}
	
	/**
	 * Set breakpoint with a line number and variable names.
	 * @param className class name
	 * @param lineNumber line number
	 */
	public void setBreakPoint(String className, int lineNumber) {
		setBreakPoint(className, lineNumber, new ArrayList<String>());
	}
	
	/**
	 * Set breakpoint with a line number and variable names.
	 * @param className class name
	 * @param lineNumber line number
	 * @param varNames variable names
	 */
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
	
	/**
	 * Set breakpoint with a method name  and variable names.
	 * @param methodName method name
	 * @param varNames variable names
	 */
	public void setBreakPoint(String methodName, ArrayList<String> varNames) {
		setBreakPoint(main, methodName, varNames);
	}
	
	/**
	 * Set breakpoint with a method name.
	 * @param className class name
	 * @param methodName method name
	 */
	public void setBreakPoint(String className, String methodName) {
		setBreakPoint(className, methodName, new ArrayList<String>());
	}
	
	/**
	 * Set breakpoint with a method name  and variable names.
	 * @param className class name
	 * @param methodName method name
	 * @param varNames variable names
	 */
	public void setBreakPoint(String className, String methodName, ArrayList<String> varNames) {
		bpm.setBreakPoint(className, methodName, varNames, true);
	}	
	
	/**
     * Set watchpoint with a line number.
     * @param lineNumber A line number in a target java file 
     */
	public void setWatchPoint(int lineNumber) {
		setWatchPoint(main, lineNumber);
	}
	
	/**
	 * Set watchpoint with a line number and variable names.
	 * @param lineNumber line number
	 * @param varNames variable names
	 */
	public void setWatchPoint(int lineNumber, ArrayList<String> varNames) {
		setWatchPoint(main, lineNumber, varNames);
	}
	
	/**
	 * Set watchpoint with a line number.
	 * @param className class name
	 * @param lineNumber line number
	 */
	public void setWatchPoint(String className, int lineNumber) {
		setWatchPoint(className, lineNumber, new ArrayList<String>());
	}
	
	/**
	 * Set watchpoint with a line number and variable names.
	 * @param className class name
	 * @param lineNumber line number
	 * @param varNames variable names
	 */
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
	/**
	 * Set watchpoint with a method name and variable names.
	 * @param methodName method name
	 * @param varNames variable names
	 */
	public void setWatchPoint(String methodName, ArrayList<String> varNames) {
		setWatchPoint(main, methodName, varNames);
	}
	
	/**
	 * Set watchpoint with a method name.
	 * @param className class name
	 * @param methodName method name
	 */
	public void setWatchPoint(String className, String methodName) {
		setWatchPoint(className, methodName, new ArrayList<String>());
	}
	
	/**
	 * Set watchpoint with a method name and variable names.
	 * @param className class name
	 * @param methodName method name
	 * @param varNames variable names
	 */
	public void setWatchPoint(String className, String methodName, ArrayList<String> varNames) {
		bpm.setBreakPoint(className, methodName, varNames, false);
	}	
	
	/**  Continue execution from breakpoint */
	public void cont() {
	    bpm.resumeThread();	
	}
	
	/**
	 * Print source code
	 */
	public void list() {
		bpm.printSrcAtCurrentLocation("Current location,", srcDir);
	}
	
	/**
	 * List currently known classes
	 * @param className if className sets "", all classes are shown.
	 */
	public void classes(String className) {
		j.vm().allClasses().stream().filter(cls -> cls.name().contains(className)).forEach(cls -> {
			System.out.println(cls.name());
		});
	}
	
	/**
	 * List a class's methods
	 * @param className class name
	 */
	public void methods(String className) {
		j.vm().classesByName(className).forEach(cls -> {
			cls.allMethods().forEach(methods -> {
				System.out.println(methods.name());	
			});
		});
	}
	
	/**
	 * List a class's fields
	 * @param className class name
	 */
	public void fields(String className) {
		j.vm().classesByName(className).forEach(cls -> {
			cls.allFields().forEach(fields -> {
				System.out.println(fields.name());	
			});
		});
	}
	
	/**
	 *  Print all local variables in current stack frame
	 */
	public void locals() {
		bpm.printLocals();
	}
	
	/**
	 * Set breakpoint at once.
	 * @param lineNumber line number
	 */
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
	
	/**
	 * Remove breakpoint with a line number.
	 * @param className class name 
	 * @param lineNumber line number
	 */
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
	
	/**
	 * Remove breakpoint with a method name.
	 * @param className class name 
	 * @param methodName method name
	 */
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
	
	/**
	 * Initialize VM before running
	 */
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
	
	/**
	 * Sleep main thread
	 * @param sleepTime wait time
	 */
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
