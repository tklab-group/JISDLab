/** */
package jisd.debug;

import com.sun.jdi.*;
import jisd.util.Name;
import jisd.util.Stream;
import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnBreakpoint;
import org.jdiscript.requests.ChainingBreakpointRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages a breakpoint or a watchpoint.
 *
 * @author sugiyama
 */
public class BreakPoint extends Point {
  Optional<ChainingBreakpointRequest> bpr = Optional.empty();
  /**
   * Constructor
   *
   * @param className class name
   * @param lineNumber line number
   */
  BreakPoint(String className, int lineNumber) {
    this(className, lineNumber, "", new ArrayList<String>(), false);
  }

  /**
   * Constructor
   *
   * @param className class name
   * @param methodName method name
   */
  BreakPoint(String className, String methodName) {
    this(className, 0, methodName, new ArrayList<String>(), false);
  }

  /**
   * Constructor
   *
   * @param className class name
   * @param lineNumber line number
   * @param varNames variable names
   * @param isBreak break or not at points
   */
  BreakPoint(String className, int lineNumber, ArrayList<String> varNames, boolean isBreak) {
    this(className, lineNumber, "", varNames, isBreak);
  }

  /**
   * Constructor
   *
   * @param className class name
   * @param methodName method name
   * @param varNames variable names
   * @param isBreak break or not at points
   */
  BreakPoint(String className, String methodName, ArrayList<String> varNames, boolean isBreak) {
    this(className, 0, methodName, varNames, isBreak);
  }

  /**
   * Constructor
   *
   * @param className class name
   * @param lineNumber line number
   * @param methodName method name
   * @param varNames variable names
   * @param isBreak break or not at points
   */
  BreakPoint(
      String className,
      int lineNumber,
      String methodName,
      ArrayList<String> varNames,
      boolean isBreak) {
    super(className, lineNumber, methodName, varNames, isBreak);
  }

  @Override
  void reset() {
    clearDebugResults();
    bpr = Optional.empty();
    setRequested(false);
  }

  void addValue(Location loc, Value jValue) {
    String varName = loc.getVarName();
    synchronized (this) {
      Optional<DebugResult> res = Optional.ofNullable(drs.get(varName));
      if (res.isPresent()) {
        res.get().addValue(jValue);
        return;
      }
      DebugResult dr = new DebugResult(loc);
      dr.addValue(jValue);
      addDebugResult(varName, dr);
    }
  }
  /** Request VM to set a breakpoint */
  @Override
  void requestSetPoint(VMManager vmMgr, PointManager bpm) {
    if (!(vmMgr instanceof JDIManager)) {
      /* do nothing */
      return;
    }
    JDIScript j = ((JDIManager) vmMgr).getJDI();
    String className = getClassName();
    List<ReferenceType> rts = j.vm().classesByName(className);
    if (rts.size() < 1) {
      if (!isRequested()) {
        deferSetPoint(vmMgr, bpm);
      }
      return;
    }
    ReferenceType rt = rts.get(0);
    /** A procedure on breakpoints. */
    OnBreakpoint breakpoint =
        be -> {
          boolean isNotSuspended = !bpm.checkCurrentTRef(false);
          if (isNotSuspended) {
            bpm.setCurrentTRef(be.thread());
          }
          try {
            // search the breakpoint which caused this event.
            int bpLineNumber = be.location().lineNumber();
            String bpClassName = Name.toClassNameFromSourcePath(be.location().sourcePath());
            String bpMethodName = be.location().method().name();
            // get variable data from target VM
            List<LocalVariable> vars;
            StackFrame stackFrame = be.thread().frame(0);
            if (varNames.size() == 0) {
              var obj = stackFrame.thisObject();
              if (obj != null) {
                obj.getValues(rt.visibleFields())
                    .forEach(
                        (f, v) -> {
                          Location loc =
                              new Location(
                                  bpClassName, bpMethodName, bpLineNumber, "this." + f.name());
                          addValue(loc, v);
                        });
              } else {
                rt.allFields().stream()
                    .filter(f -> f.isStatic())
                    .forEach(
                        f -> {
                          Location loc =
                              new Location(
                                  bpClassName, bpMethodName, bpLineNumber, "this." + f.name());
                          addValue(loc, rt.getValue(f));
                        });
              }
              vars = stackFrame.visibleVariables();
            } else {
              vars =
                  varNames.stream()
                      .map(
                          name -> {
                            try {
                              if (name.startsWith("this.")) {
                                var obj = stackFrame.thisObject();
                                if (obj != null) {
                                  var f = rt.fieldByName(name.substring(5));
                                  Location loc =
                                      new Location(
                                          bpClassName,
                                          bpMethodName,
                                          bpLineNumber,
                                          "this." + f.name());
                                  addValue(loc, obj.getValue(f));
                                } else {
                                  var f = rt.fieldByName(name.substring(5));
                                  if (rt.isStatic()) {
                                    Location loc =
                                        new Location(
                                            bpClassName,
                                            bpMethodName,
                                            bpLineNumber,
                                            "this." + f.name());
                                    addValue(loc, rt.getValue(f));
                                  }
                                }
                              }
                              return stackFrame.visibleVariableByName(name);
                            } catch (AbsentInformationException ee) {
                              DebuggerInfo.printError("such a variable name not found.");
                              return null;
                            }
                          })
                      .filter(o -> o != null)
                      .collect(Stream.toArrayList());
            }
            Map<LocalVariable, Value> visibleVariables = stackFrame.getValues(vars);
            // add debug result
            for (Map.Entry<LocalVariable, Value> entry : visibleVariables.entrySet()) {
              String varName = entry.getKey().name();
              Location loc = new Location(bpClassName, bpMethodName, bpLineNumber, varName);
              addValue(loc, entry.getValue());
            }
            // if isBreak is true
            if (isBreak()) {
              if (bpm.isProcessing()) {
                bpm.completeStep();
                DebuggerInfo.print("Step completed");
              }
              bpm.printCurrentLocation("Breakpoint hit", bpLineNumber, bpClassName, bpMethodName);
              bpm.setBreaked(true);
              if (isNotSuspended) {
                ThreadReference currentTRef = bpm.getCurrentTRef();
                currentTRef.suspend();
              }
            }
          } catch (IncompatibleThreadStateException | AbsentInformationException e) {
            e.printStackTrace();
          }
        };
    if (getLineNumber() == 0) { // breakpoints set by methodName
      rt.methodsByName(getMethodName())
          .forEach(
              methods -> {
                try {
                  var locs = methods.allLineLocations();
                  if (locs.size() > 0) {
                    bpr = Optional.of(j.breakpointRequest(locs.get(0), breakpoint));
                    if (bpr.isPresent() && isEnable) {
                      bpr.get().enable();
                    }
                    setRequested(true);
                  }
                } catch (AbsentInformationException e) {
                  e.printStackTrace();
                }
              });
    } else { // breakpoints set by lineNumber
      try {
        rt.locationsOfLine(getLineNumber())
            .forEach(
                m -> {
                  bpr = Optional.of(j.breakpointRequest(m, breakpoint));
                  if (bpr.isPresent() && isEnable) {
                    bpr.get().enable();
                  }
                  setRequested(true);
                });
      } catch (AbsentInformationException e) {
        e.printStackTrace();
      }
    }
  }

  /** Defer to set breakpoint until the class loaded. */
  void deferSetPoint(VMManager vmMgr, PointManager bpm) {
    if (isRequested()) {
      DebuggerInfo.printError("Cannot set breakpoint. Skipped.");
      return;
    }
    setRequested(true);
    if (!(vmMgr instanceof JDIManager)) {
      /* do nothing */
      return;
    }
    JDIScript j = ((JDIManager) vmMgr).getJDI();
    String className = getClassName();
    j.onClassPrep(
        p -> {
          if (p.referenceType().name().equals(className)) {
            requestSetPoint(vmMgr, bpm);
          }
        });
    DebuggerInfo.print(
        "Deferring breakpoint in " + className + ". It will be set after the class is loaded.");
  }

  @Override
  public void add(String varName) {
    addVarName(varName);
  }

  @Override
  public void remove(String varName) {
    removeVarName(varName);
  }

  @Override
  public void enable() {
    isEnable = true;
    if (bpr.isPresent()) {
      bpr.get().enable();
    }
  }

  @Override
  public void disable() {
    isEnable = false;
    if (bpr.isPresent()) {
      bpr.get().disable();
    }
  }
}
