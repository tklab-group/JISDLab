package debug;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnBreakpoint;
import org.jdiscript.handlers.OnVMStart;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.event.ClassPrepareEvent;

/**
 * Breakpoint manager
 * @author sugiyama
 *
 */
class BreakPointManager {
	/** A procedure before the debugger runs. */
	OnVMStart start;
	/** JDI */
	JDIScript j;
	/** A debug result manager */
	DebugResultManager drm;
	/** breakpoints */
	private final Set<BreakPoint> bps = new HashSet<>();
	/** Stacktraces at a breakpoint */
	final Map<String, AtomicLong> stacktraces = new HashMap<>();
	/** Current Thread Reference */
	ThreadReference currentTRef;
	
	/**
	 * Constructor
	 * @param drm a debug result manager 
	 */
    BreakPointManager(DebugResultManager drm) {
    	this.drm = drm;
	}
    
    void setJDI(JDIScript j) {
    	this.j = j;
    }
    
    /**
     * A stacktrace string
     * @param t Thread
     * @return A stacktrace string
     * @throws IncompatibleThreadStateException Thrown to indicate that the requested operation cannot becompleted while the specified thread is in its current state.
     */
    String stacktraceKey(ThreadReference t) 
            throws IncompatibleThreadStateException {
        StringBuilder sb = new StringBuilder();
        for(StackFrame f: t.frames()) {
            Location loc = f.location(); 
            sb.append(loc.declaringType().name())
              .append(".").append(loc.method().name())
              .append(loc.lineNumber())
              .append(":");
        }
        return sb.substring(0, sb.length() - 1);
    }
    
    /**
     * generate a class path from a source path 
     * @param sp source path
     * @return class name
     */
    String toClassNameFromSourcePath(String sp) {
    	String className = sp.replace(File.separator.charAt(0), '.'); 
    	int length = className.length();
    	if (className.substring(length-5, length).equals(".java")) {
    		return className.substring(0, length-5);
    	}
    	return className;
    }
	
    /**
     * A procedure on breakpoints.
     */
	OnBreakpoint breakpoint = be -> {
		currentTRef = be.thread();
        try {
	    	StackFrame stackFrame = currentTRef.frame(0);
	    	List<LocalVariable> vars = stackFrame.visibleVariables();
	    	Map<LocalVariable, Value> visibleVariables = (Map<LocalVariable, Value>) stackFrame.getValues(vars);
	    	int bpLineNumber = be.location().lineNumber();
	    	String bpClassName = toClassNameFromSourcePath(be.location().sourcePath());
	    	String bpMethodName = be.location().method().name();
	    	BreakPoint bpSetByLineNumber = this.bps.stream()
							    	               .filter(bp -> bp.equals(new BreakPoint(bpClassName, bpLineNumber)))
							    	               .findFirst()
							    	               .orElse(new BreakPoint(bpClassName, 0));
	    	boolean isBPSetByLineNumber = (bpSetByLineNumber.getLineNumber() > 0);
	    	BreakPoint bpSetByMethodName = (! isBPSetByLineNumber) ? this.bps.stream()
														                     .filter(bp -> bp.equals(new BreakPoint(bpClassName, bpMethodName)))
														                     .findFirst()
														                     .orElse(new BreakPoint(bpClassName, 0))
                                                                   : new BreakPoint(bpClassName, 0);
	    	boolean isBPSetByMethodName = (bpSetByMethodName.getMethodName().length() > 0);
	    	BreakPoint bp = (isBPSetByLineNumber) ? bpSetByLineNumber
	    			                              : (isBPSetByMethodName) ? bpSetByMethodName
	    			                            		                  : new BreakPoint(bpClassName, 0);
	    	for (Map.Entry<LocalVariable, Value> entry : visibleVariables.entrySet()) {
	    		String varName = entry.getKey().name();
	    		if ((isBPSetByLineNumber && (bpSetByLineNumber.getVarNames().size() == 0 || bpSetByLineNumber.getVarNames().contains(varName))) ||
	    			(isBPSetByMethodName && (bpSetByMethodName.getVarNames().size() == 0 || bpSetByMethodName.getVarNames().contains(varName)))) {
	        	    drm.addVariable(bp, bpClassName, bpLineNumber, varName, stackFrame.location(), entry);
	    		}
	        }
	    	if ((isBPSetByLineNumber && bpSetByLineNumber.getIsBreak()) ||
	    		(isBPSetByMethodName && bpSetByMethodName.getIsBreak())) {
	    		printCurrentLocation("Breakpoint hit,", bpLineNumber, bpClassName, bpMethodName);
	    	    currentTRef.suspend();
	    	}
        } catch (IncompatibleThreadStateException | AbsentInformationException e) {
        	e.printStackTrace();
        }
    };
    
    /**
     * resume current thread
     */
    void resumeThread() {
    	currentTRef.resume();
    }
    
    /**
     * Request VM to set a breakpoint 
     * @param bp breakpoint
     */
    void requestSetBreakPoint(BreakPoint bp) {
    	List<ReferenceType> rts = j.vm().classesByName(bp.getClassName());
		if (rts.size() < 1) {
			DebuggerInfo.printError("Cannot set breakpoint. Skipped.");
			return;
		}
		ReferenceType rt = rts.get(0);
		if (bp.getLineNumber() == 0) { // breakpoints set by methodName
			rt.methodsByName(bp.getMethodName()).forEach(methods -> {
    			try {
					methods.allLineLocations().forEach(m -> {
					    j.breakpointRequest(m, this.breakpoint).enable();
					});
				} catch (AbsentInformationException e) {
					e.printStackTrace();
				}
	    	});
		} else { // breakpoints set by lineNumber
			try {
				rt.locationsOfLine(bp.getLineNumber()).forEach(m -> {
					j.breakpointRequest(m, this.breakpoint).enable();
				});
			} catch (AbsentInformationException e) {
				e.printStackTrace();
			}
		}
    }
	
    /**
     * Request VM to set a breakpoint 
     */
    void requestSetBreakPoints() {
    	bps.forEach(bp -> requestSetBreakPoint(bp));
    }
    	
    /**
     * Set breakpoint by a line number.
     * @param className class name
     * @param lineNumber line number
     * @param varNames variable names 
     * @param isBreak break or not at points
     * @return breakpoint
     */
	public Optional<BreakPoint> setBreakPoint(String className, int lineNumber, ArrayList<String> varNames, boolean isBreak) {
		if (className.length() == 0) {
			DebuggerInfo.printError("Breakpoint is not set. A class name must be one or more letters.");
			return Optional.empty();
		}
		if (lineNumber <= 0) {
			DebuggerInfo.printError("Breakpoint is not set. A line number must be a non-negative integer(> 0).");
			return Optional.empty();
		}
		BreakPoint bp = new BreakPoint(className, lineNumber, varNames, isBreak); 
		bps.add(bp);
		return Optional.of(bp);
	}
	
	/**
	 * Set breakpoint by a method name.
	 * @param className class name
	 * @param methodName method name
	 * @param varNames value name
	 * @param isBreak break or not at points
	 * @return breakpoint
	 */
	public Optional<BreakPoint> setBreakPoint(String className, String methodName, ArrayList<String> varNames, boolean isBreak) {
		if (className.length() == 0) {
			DebuggerInfo.printError("Breakpoint is not set. A class name must be one or more letters.");
			return Optional.empty();
		}
		if (methodName.length() == 0) {
			DebuggerInfo.printError("Breakpoint is not set. A method name must be one or more letters.");
			return Optional.empty();
		}
		BreakPoint bp = new BreakPoint(className, methodName, varNames, isBreak); 
		bps.add(bp);
		return Optional.of(bp);
	}
	
	/**
	 * Remove breakpoint.
	 * @param className A target class file name
	 * @param lineNumber A line number in a target java file
	 */
	public void removeBreakPoint(String className, int lineNumber) {
		BreakPoint bp = new BreakPoint(className, lineNumber); 
		bps.remove(bp);
	}
	
	/**
	 * Remove breakpoint.
	 * @param className A target class file name
	 * @param methodName A method name a class has 
	 */
	public void removeBreakPoint(String className, String methodName) {
		BreakPoint bp = new BreakPoint(className, methodName); 
		bps.remove(bp);
	}
	
	/**
	 * Get breakpoints
	 * @return breakpoints
	 */
	Set<BreakPoint> getBreakPoints() {
		return bps;
	}
	
	/**
	 * Print current location infomation
	 * @param prefix print reason 
	 * @param lineNumber line number
	 * @param className class name
	 * @param methodName method name
	 */
	void printCurrentLocation(String prefix, int lineNumber, String className, String methodName) {
		DebuggerInfo.print(prefix + " line=" + lineNumber + ", class=" + className + ", method=" + methodName);	
	}
	
	/**
	 * Print source code.
	 * @param prefix print reason
	 * @param srcDir source directory
	 */
	void printSrcAtCurrentLocation(String prefix, String srcDir) {
		try {
			Location currentLocation = currentTRef.frame(0).location();
			int lineNumber = currentLocation.lineNumber();
			String srcRelPath = currentLocation.sourcePath();
	    	String className = toClassNameFromSourcePath(srcRelPath);
	    	String methodName = currentLocation.method().name();
	    	printCurrentLocation(prefix, lineNumber, className, methodName);
	    	if (! srcDir.equals("")) {
	    	    String srcAbsPath = srcDir + File.separator.charAt(0) + srcRelPath; 
	    	    DebuggerInfo.printSrc(srcAbsPath, lineNumber);
	    	}
		} catch (IncompatibleThreadStateException | AbsentInformationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Print local variables
	 */
	void printLocals() {
		try {
			StackFrame stackFrame = currentTRef.frame(0);
			List<LocalVariable> vars = stackFrame.visibleVariables();
	    	Map<LocalVariable, Value> visibleVariables = (Map<LocalVariable, Value>) stackFrame.getValues(vars);
	    	System.out.println("Method arguments:");
	    	visibleVariables.entrySet().stream().filter(entry -> entry.getKey().isArgument()).forEach(entry -> {
	    		System.out.println(entry.getKey().name() + " = " + entry.getValue().toString());
	    	});
	    	System.out.println("\nLocal variables:");
	    	visibleVariables.entrySet().stream().filter(entry -> ! entry.getKey().isArgument()).forEach(entry -> {
	    		System.out.println(entry.getKey().name() + " = " + entry.getValue().toString());
	    	});
		} catch (IncompatibleThreadStateException | AbsentInformationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
