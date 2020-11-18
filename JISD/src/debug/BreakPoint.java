/**
 * 
 */
package debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnBreakpoint;
import org.jdiscript.requests.ChainingBreakpointRequest;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

import util.SrcUtil;
import util.StreamUtil;

/**
 * Breakpoint infomation
 * 
 * @author sugiyama
 *
 */
public class BreakPoint {
  /** class name */
  String className;
  /** line number */
  int lineNumber;
  /** method name */
  String methodName;
  /** varNames and debugresult */
  HashMap<String, DebugResult> drs = new HashMap<>();
  /** varNames and maxRecordNoOfValue */
  HashMap<String, Integer> maxRecords = new HashMap<>();
  /** varNames and maxNoOfExpand */
  HashMap<String, Integer> maxExpands = new HashMap<>();
  Optional<ChainingBreakpointRequest> bpr = Optional.empty();
  boolean isEnable = true;
  /** variable names */
  ArrayList<String> varNames;
  /** break or not at points */
  boolean isBreak;
  /** already request to set Breakpoint? */
  boolean isRequested = false;

  /**
   * Constructor
   * 
   * @param className  class name
   * @param lineNumber line number
   */
  BreakPoint(String className, int lineNumber) {
    this(className, lineNumber, "", new ArrayList<String>(), false);
  }

  /**
   * Constructor
   * 
   * @param className  class name
   * @param methodName method name
   */
  BreakPoint(String className, String methodName) {
    this(className, 0, methodName, new ArrayList<String>(), false);
  }

  /**
   * Constructor
   * 
   * @param className  class name
   * @param lineNumber line number
   * @param varNames   variable names
   * @param isBreak    break or not at points
   */
  BreakPoint(String className, int lineNumber, ArrayList<String> varNames, boolean isBreak) {
    this(className, lineNumber, "", varNames, isBreak);
  }

  /**
   * Constructor
   * 
   * @param className  class name
   * @param methodName method name
   * @param varNames   variable names
   * @param isBreak    break or not at points
   */
  BreakPoint(String className, String methodName, ArrayList<String> varNames, boolean isBreak) {
    this(className, 0, methodName, varNames, isBreak);
  }

  /**
   * Constructor
   * 
   * @param className  class name
   * @param lineNumber line number
   * @param methodName method name
   * @param varNames   variable names
   * @param isBreak    break or not at points
   */
  BreakPoint(String className, int lineNumber, String methodName, ArrayList<String> varNames, boolean isBreak) {
    this.className = className;
    this.lineNumber = lineNumber;
    this.methodName = methodName;
    varNames.remove("");
    this.varNames = varNames;
    this.isBreak = isBreak;
  }

  /**
   * Get a class name.
   * 
   * @return class name
   */
  public String getClassName() {
    return className;
  }

  /**
   * Get a line number
   * 
   * @return line number
   */
  public int getLineNumber() {
    return lineNumber;
  }

  /**
   * Get a method name
   * 
   * @return method name
   */
  public String getMethodName() {
    return methodName;
  }

  /**
   * Get variable names
   * 
   * @return variable names
   */
  public ArrayList<String> getVarNames() {
    return varNames;
  }

  /**
   * Get whether the debugger breaks or not at points
   * 
   * @return break or not at points
   */
  public boolean getIsBreak() {
    return isBreak;
  }

  /**
   * Get DebugResult a variable name matches.
   * 
   * @param varName variable name
   * @return debug result
   */
  public Optional<DebugResult> getResult(String varName) {
    Optional<DebugResult> result = Optional.ofNullable(drs.get(varName));
    return result;
  }

  /**
   * Get DebugResults.
   * 
   * @return debug result
   */
  public HashMap<String, DebugResult> getResults() {
    return drs;
  }

  /**
   * Set DebugResult
   * 
   * @param dr debugresult
   */
  public void addDebugResult(String varName, DebugResult dr) {
    drs.put(varName, dr);
  }

  /**
   * Clear DebugResult
   */
  public void clearDebugResults() {
    drs = new HashMap<>();
    DebugResult.resetNumber();
  }
  
  void addValue(String className, int lineNumber, String varName,
      Map.Entry<LocalVariable, Value> entry) {
    synchronized (this) {
      Optional<DebugResult> res = Optional.ofNullable(drs.get(varName));
      if (res.isPresent()) {
        res.get().addValue(entry);
        return;
      }
      DebugResult dr = new DebugResult(className, lineNumber, varName);
      if (maxRecords.containsKey(varName)) {
        dr.setMaxRecordNoOfValue(maxRecords.get(varName));
      }
      if (maxExpands.containsKey(varName)) {
        dr.setMaxRecordNoOfValue(maxExpands.get(varName));
      }
      dr.addValue(entry);
      addDebugResult(varName, dr);
    }
  }
  /**
   * Request VM to set a breakpoint
   * 
   * @param bp breakpoint
   */
  void requestSetBreakPoint(VMManager vmMgr, BreakPointManager bpm) {
    if (!(vmMgr instanceof JDIManager)) {
      /* do nothing */
      return;  
    }
    JDIScript j = ((JDIManager) vmMgr).getJDI();
    String className = getClassName();
    List<ReferenceType> rts = j.vm().classesByName(className);
    if (rts.size() < 1) {
      if (! getRequestState()) {
        deferSetBreakPoint(vmMgr, bpm);
      }
      return;
    }
    ReferenceType rt = rts.get(0);
    /**
     * A procedure on breakpoints.
     */
    OnBreakpoint breakpoint = be -> {
      boolean isNotSuspended = ! bpm.checkCurrentTRef(false);
      if (isNotSuspended) {
        bpm.setCurrentTRef(be.thread());
      }
      try {
        // search the breakpoint which caused this event.
        int bpLineNumber = be.location().lineNumber();
        String bpClassName = SrcUtil.toClassNameFromSourcePath(be.location().sourcePath());
        String bpMethodName = be.location().method().name();
        // get variable data from target VM
        var varNames = getVarNames();
        List<LocalVariable> vars;
        StackFrame stackFrame = be.thread().frame(0);
        if (varNames.size() == 0) {
          vars = stackFrame.visibleVariables();
        } else {
          vars = varNames.stream()
                         .map(name -> {
                                try {
                                  return stackFrame.visibleVariableByName(name);
                                } catch (AbsentInformationException ee) {
                                  DebuggerInfo.printError("such a variable name not found.");
                                  return null;
                                }
                              })
                         .filter(o -> o != null)
                         .collect(StreamUtil.toArrayList());
        }
        Map<LocalVariable, Value> visibleVariables = stackFrame.getValues(vars);
        // add debug result
        for (Map.Entry<LocalVariable, Value> entry : visibleVariables.entrySet()) {
          String varName = entry.getKey().name();
          addValue(bpClassName, bpLineNumber, varName, entry);
        }
        // if isBreak is true
        if (getIsBreak()) {
          bpm.printCurrentLocation("Breakpoint hit", bpLineNumber, bpClassName, bpMethodName);
          if (isNotSuspended) {
            ThreadReference currentTRef = bpm.getCurrentTRef();
            currentTRef.suspend();
          }
          bpm.setIsProcessing(false);
        }
      } catch (IncompatibleThreadStateException | AbsentInformationException e) {
        e.printStackTrace();
      }
    };
    if (getLineNumber() == 0) { // breakpoints set by methodName
      rt.methodsByName(getMethodName()).forEach(methods -> {
        try {
          var locs = methods.allLineLocations();
          if (locs.size() > 0) {
            bpr = Optional.of(j.breakpointRequest(locs.get(0), breakpoint));
            if (bpr.isPresent() && isEnable) {
              bpr.get().enable();
            }
            setRequestState(true);
          };
        } catch (AbsentInformationException e) {
          e.printStackTrace();
        }
      });
    } else { // breakpoints set by lineNumber
      try {
        rt.locationsOfLine(getLineNumber()).forEach(m -> {
          bpr = Optional.of(j.breakpointRequest(m, breakpoint));
          if (bpr.isPresent() && isEnable) {
            bpr.get().enable();
          }
          setRequestState(true);
        });
      } catch (AbsentInformationException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Defer to set breakpoint until the class loaded.
   * 
   * @param bp breakpoint
   */
  void deferSetBreakPoint(VMManager vmMgr, BreakPointManager bpm) {
    if (getRequestState()) {
      DebuggerInfo.printError("Cannot set breakpoint. Skipped.");
      return;
    }
    setRequestState(true);
    if (!(vmMgr instanceof JDIManager)) {
      /* do nothing */
      return;  
    }
    JDIScript j = ((JDIManager) vmMgr).getJDI();
    String className = getClassName();
    j.onClassPrep(p -> {
      if (p.referenceType().name().equals(className)) {
        requestSetBreakPoint(vmMgr, bpm);   
      }
    });
    DebuggerInfo.print("Deferring breakpoint in " + className + ". It will be set after the class is loaded.");
  }

  /**
   * Set request state
   * 
   * @param state already request to set breakpoint?
   */
  void setRequestState(boolean state) {
    isRequested = state;
  }
  
  public void enable() {
    isEnable = true;
    if (bpr.isPresent()) {
      bpr.get().enable();
    }
  }
  
  public void disable() {
    isEnable = false;
    if (bpr.isPresent()) {
      bpr.get().disable();
    }
  }

  /**
   * Get request state
   * 
   * @return request state
   */
  boolean getRequestState() {
    return isRequested;
  }
  
  public void setMaxRecordNoOfValue(String varName, int number) {
    if (number <= 0) {
      DebuggerInfo.printError("A max record number must be a non-negative integer(> 0).");
      return;
    }
    maxRecords.put(varName, number);
    Optional<DebugResult> dr = getResult(varName);
    if (dr.isPresent()) {
      dr.get().setMaxRecordNoOfValue(number);
    }
  }
  
  public void setMaxNoOfExpand(String varName, int number) {
    if (number < 0) {
      DebuggerInfo.printError("A max number of the variable expansion must be a positive integer(>= 0).");
    }
    maxExpands.put(varName, number);
    Optional<DebugResult> dr = getResult(varName);
    if (dr.isPresent()) {
      dr.get().setMaxNoOfExpand(number);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((className == null) ? 0 : className.hashCode());
    result = prime * result + lineNumber;
    result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
    result = prime * result + ((this instanceof ProbePoint) ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    BreakPoint other = (BreakPoint) obj;
    if (className == null) {
      if (other.className != null)
        return false;
    } else if (!className.equals(other.className))
      return false;
    if (lineNumber != other.lineNumber)
      return false;
    if (methodName == null) {
      if (other.methodName != null)
        return false;
    } else if (!methodName.equals(other.methodName))
      return false;
    return true;
  }
}
