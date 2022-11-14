/**
 *
 */
package jisd.debug;

import com.sun.jdi.*;
import jisd.debug.value.ValueInfo;
import jisd.util.Name;
import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnBreakpoint;
import org.jdiscript.requests.ChainingBreakpointRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

  ValueInfo addValue(Location loc, Value jValue, List<Location> stList, LocalDateTime date) {
    String varName = loc.getVarName();
    synchronized (this) {
      Optional<DebugResult> res_opt = Optional.ofNullable(drs.get(varName));
      if (res_opt.isPresent()) {
        var res = res_opt.get();
        var valueInfo = res.addValue(jValue, stList, date);
        return valueInfo;
      }
      DebugResult dr = new DebugResult(loc);
      var valueInfo = dr.addValue(jValue, stList, date);
      addDebugResult(varName, dr);
      return valueInfo;
    }
  }

  /** Request VM to set a breakpoint */
  @Override
  void requestSetPoint(VMManager vmMgr, PointManager bpm) {
    if (!(vmMgr instanceof JDIManager)) {
      /* do nothing */
      return;
    }
    var dbg = vmMgr.getDebugger();
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
        var stList = bpm.getStackTrace();
        try {
          // search the breakpoint which caused this event.
          int bpLineNumber = be.location().lineNumber();
          String bpClassName = Name.toClassNameFromSourcePath(be.location().sourcePath());
          String bpMethodName = be.location().method().name();
          var date = LocalDateTime.now();
          AtomicInteger sleepTimeMax = new AtomicInteger();
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
                    var valueInfo = addValue(loc, v, stList, date);
                    // update metrics
                    int sleepTime = dbg.notifyExporters(valueInfo);
                    if (sleepTime > sleepTimeMax.get()) {
                      sleepTimeMax.set(sleepTime);
                    }
                  });
            } else {
              rt.allFields().stream()
                .filter(f -> f.isStatic())
                .forEach(
                  f -> {
                    Location loc =
                      new Location(
                        bpClassName, bpMethodName, bpLineNumber, "this." + f.name());
                    var valueInfo = addValue(loc, rt.getValue(f), stList, date);
                    // update metrics
                    int sleepTime = dbg.notifyExporters(valueInfo);
                    if (sleepTime > sleepTimeMax.get()) {
                      sleepTimeMax.set(sleepTime);
                    }
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
                        var f = rt.fieldByName(name.substring(5));
                        Location loc =
                          new Location(bpClassName, bpMethodName, bpLineNumber, "this." + f.name());
                        if (obj != null) {
                          var valueInfo = addValue(loc, obj.getValue(f), stList, date);
                          // update metrics
                          int sleepTime = dbg.notifyExporters(valueInfo);
                          if (sleepTime > sleepTimeMax.get()) {
                            sleepTimeMax.set(sleepTime);
                          }
                        } else if (rt.isStatic()) {
                          var valueInfo = addValue(loc, rt.getValue(f), stList, date);
                          // update metrics
                          int sleepTime = dbg.notifyExporters(valueInfo);
                          if (sleepTime > sleepTimeMax.get()) {
                            sleepTimeMax.set(sleepTime);
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
                .collect(Collectors.toList());
          }
          Map<LocalVariable, Value> visibleVariables = stackFrame.getValues(vars);
          // add debug result
          for (Map.Entry<LocalVariable, Value> entry : visibleVariables.entrySet()) {
            String varName = entry.getKey().name();
            Location loc = new Location(bpClassName, bpMethodName, bpLineNumber, varName);
            var valueInfo = addValue(loc, entry.getValue(), stList, date);
            // update metrics
            int sleepTime = dbg.notifyExporters(valueInfo);
            if (sleepTime > sleepTimeMax.get()) {
              sleepTimeMax.set(sleepTime);
            }
          }
          Utility.sleep(sleepTimeMax.get());
          // if isBreak is true
          if (bpm.isProcessing()) {
            bpm.completeStep();
            if (bpm.count == 1) {
              DebuggerInfo.print("Step completed");
            }
          }
          if (bpm.count == 1 || bpm.count == 0) {
            bpm.printCurrentLocation("Breakpoint hit", bpLineNumber, bpClassName, bpMethodName);
          }
          bpm.setBreaked(true);
          if (isNotSuspended) {
            if (isBreak) {
              ThreadReference currentTRef = bpm.getCurrentTRef();
              currentTRef.suspend();
            } else {
              bpm.clearThread();
            }
          }
        } catch (IncompatibleThreadStateException | AbsentInformationException e) {
          bpm.clearThread();
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
