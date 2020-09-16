package debug;

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
 * A breakpoint manager class.
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
    
    String toClassNameFromSourcePath(String sp) {
    	String className = sp.replace('\\', '.');
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
        ThreadReference tref = be.thread();
        try {
	    	StackFrame stackFrame = tref.frame(0);
	    	List<LocalVariable> vars = stackFrame.visibleVariables();
	    	Map<LocalVariable, Value> visibleVariables = (Map<LocalVariable, Value>) stackFrame.getValues(vars);
	    	int bpLineNumber = be.location().lineNumber();
	    	String bpClassName = toClassNameFromSourcePath(be.location().sourcePath());
	    	DebuggerInfo.print("Breakpoint hit, "+ "line=" + bpLineNumber + ", class=" + bpClassName + ", method=" + be.location().method().name());
	    	BreakPoint bpSetByLineNumber = this.bps.stream()
									    	               .filter(bp -> bp.equals( new BreakPoint(bpClassName, bpLineNumber, new ArrayList<String>()) ))
									    	               .findFirst()
									    	               .orElse(new BreakPoint(bpClassName, 0, new ArrayList<String>()));
	    	for (Map.Entry<LocalVariable, Value> entry : visibleVariables.entrySet()) {
	    		if (bpSetByLineNumber.getLineNumber()      == 0 ||
	    			bpSetByLineNumber.getVarNames().size() == 0 ||
	    			bpSetByLineNumber.getVarNames().contains(entry.getKey().name())) {
	        	  drm.addVariable(stackFrame.location(), entry);
	    		}
	        }
        } catch (IncompatibleThreadStateException | AbsentInformationException e) {
        	e.printStackTrace();
        }
    };
	
    /**
     * Request setting breakpoints
     */
    void requestSetBreakPoints() {
    	/*j.vm().allClasses().forEach(i -> {
    		System.out.println(i);
    	});
    	*/
    	bps.forEach(bp -> {
    		ReferenceType rt = j.vm().classesByName(bp.getClassName()).get(0);
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
    	});
    }
    	
    /**
     * Set breakpoint.
     * @param className A target class file name
     * @param lineNumber A line number in a target java file
     */
	public void setBreakPoint(String className, int lineNumber, ArrayList<String> varNames) {
		BreakPoint bp = new BreakPoint(className, lineNumber, varNames); 
		bps.add(bp);
	}
	
	/**
	 * Set breakpoint with a method name.
	 * @param className A target class file name
	 * @param methodName A method name a class has 
	 */
	public void setBreakPoint(String className, String methodName, ArrayList<String> varNames) {
		BreakPoint bp = new BreakPoint(className, methodName, varNames); 
		bps.add(bp);
	}
	
	/**
	 * Remove breakpoint.
	 * @param className A target class file name
	 * @param lineNumber A line number in a target java file
	 */
	public void removeBreakPoint(String className, int lineNumber) {
		BreakPoint bp = new BreakPoint(className, lineNumber, new ArrayList<String>()); 
		bps.remove(bp);
	}
	
	/**
	 * Remove breakpoint.
	 * @param className A target class file name
	 * @param methodName A method name a class has 
	 */
	public void removeBreakPoint(String className, String methodName) {
		BreakPoint bp = new BreakPoint(className, methodName, new ArrayList<String>()); 
		bps.remove(bp);
	}
	
	/**
	 * Get line numbers a breakpoint sets at.
	 * @return lineNumbers
	 */
	Set<BreakPoint> getBreakPoints() {
		return bps;
	}
	
}
