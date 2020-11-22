package debug;

import com.sun.jdi.*;
import com.sun.jdi.request.DuplicateRequestException;
import com.sun.jdi.request.StepRequest;
import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnStep;
import util.Name;
import util.Stream;

import java.io.File;
import java.util.*;

/**
 * Breakpoint manager
 *
 * @author sugiyama
 */
class BreakPointManager {
  /** breakpoints */
  private final Set<BreakPoint> bps = new HashSet<>();
  /** Current Thread Reference */
  ThreadReference currentTRef;
  /** is processing now? */
  volatile boolean isProcessing;

  /** */
  BreakPointManager() {
    init();
  }

  void setIsProcessing(boolean isProcessing) {
    this.isProcessing = isProcessing;
  }

  ThreadReference getCurrentTRef() {
    return currentTRef;
  }

  void setCurrentTRef(ThreadReference tRef) {
    this.currentTRef = tRef;
  }

  void init() {
    setIsProcessing(false);
    setCurrentTRef(null);
  }

  /**
   * A stacktrace string
   *
   * @param t Thread
   * @return A stacktrace string
   * @throws IncompatibleThreadStateException Thrown to indicate that the requested operation cannot
   *     becompleted while the specified thread is in its current state.
   */
  String stackTraceKey(ThreadReference t) throws IncompatibleThreadStateException {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    for (StackFrame f : t.frames()) {
      Location loc = f.location();
      sb.append("[" + i + "] ")
          .append(loc.declaringType().name())
          .append(".")
          .append(loc.method().name())
          .append(" (line: ")
          .append(loc.lineNumber())
          .append(")\n");
      i++;
    }
    return sb.substring(0, sb.length() - 1);
  }

  /**
   * Check current thread reference state
   *
   * @return whether the current thread is suspended or not
   */
  boolean checkCurrentTRef(boolean isVerbose) {
    boolean isSuspended = false;
    if (currentTRef == null) {
      isSuspended = false;
    } else {
      isSuspended = currentTRef.isSuspended();
    }
    if (isVerbose && !isSuspended) {
      DebuggerInfo.print("The target VM thread is not suspended now.");
    }
    return isSuspended;
  }

  boolean checkCurrentTRef() {
    return checkCurrentTRef(true);
  }

  /** resume current thread */
  void resumeThread() {
    if (!checkCurrentTRef()) {
      return;
    }
    currentTRef.resume();
    currentTRef = null;
  }

  /**
   * request step execution
   *
   * @param depth depth of step
   */
  void requestStep(VMManager vmMgr, int depth) {
    if (!checkCurrentTRef()) {
      return;
    }
    isProcessing = true;
    /** A procedure on step. */
    if (!(vmMgr instanceof JDIManager)) {
      /* do nothing */
      return;
    }
    JDIScript j = ((JDIManager) vmMgr).getJDI();
    OnStep onStep =
        j.once(
            (s) -> {
              if (!isProcessing) {
                DebuggerInfo.print("Step completed");
                return;
              }
              int lineNumber = s.location().lineNumber();
              String methodName = s.location().method().name();
              try {
                String className = Name.toClassNameFromSourcePath(s.location().sourcePath());
                printCurrentLocation("Step completed", lineNumber, className, methodName);
              } catch (AbsentInformationException e) {
                printCurrentLocation("Step completed", lineNumber, "Not attached", methodName);
              }
              currentTRef = s.thread();
              currentTRef.suspend();
              isProcessing = false;
            });
    try {
      j.onStep(currentTRef, StepRequest.STEP_LINE, depth, onStep);
    } catch (DuplicateRequestException e) {
      e.printStackTrace();
    }
    resumeThread();
  }

  /** request step into execution */
  void requestStepInto(VMManager vm) {
    requestStep(vm, StepRequest.STEP_INTO);
  }

  /** request step over execution */
  void requestStepOver(VMManager vm) {
    requestStep(vm, StepRequest.STEP_OVER);
  }

  /** request step out execution */
  void requestStepOut(VMManager vm) {
    requestStep(vm, StepRequest.STEP_OUT);
  }

  /** Request VM to set a breakpoint */
  void requestSetBreakPoints(VMManager vm) {
    bps.forEach(
        bp -> {
          bp.requestSetBreakPoint(vm, this);
        });
  }

  /**
   * Set breakpoint by a line number.
   *
   * @param className class name
   * @param lineNumber line number
   * @param varNames variable names
   * @param isBreak break or not at points
   * @return breakpoint
   */
  public Optional<BreakPoint> setBreakPoint(
      VMManager vm,
      String className,
      int lineNumber,
      ArrayList<String> varNames,
      boolean isBreak,
      boolean isProbe) {
    if (className.length() == 0) {
      DebuggerInfo.printError("Breakpoint is not set. A class name must be one or more letters.");
      return Optional.empty();
    }
    if (lineNumber <= 0) {
      DebuggerInfo.printError(
          "Breakpoint is not set. A line number must be a non-negative integer(> 0).");
      return Optional.empty();
    }
    BreakPoint bp;
    if (isProbe) {
      bp = new ProbePoint(className, lineNumber, varNames, isBreak);
    } else {
      bp = new BreakPoint(className, lineNumber, varNames, isBreak);
    }
    bp.requestSetBreakPoint(vm, this);
    bps.add(bp);
    return Optional.of(bp);
  }

  /**
   * Set breakpoint by a method name.
   *
   * @param className class name
   * @param methodName method name
   * @param varNames value name
   * @param isBreak break or not at points
   * @return breakpoint
   */
  public Optional<BreakPoint> setBreakPoint(
      VMManager vm,
      String className,
      String methodName,
      ArrayList<String> varNames,
      boolean isBreak,
      boolean isProbe) {
    if (className.length() == 0) {
      DebuggerInfo.printError("Breakpoint is not set. A class name must be one or more letters.");
      return Optional.empty();
    }
    if (methodName.length() == 0) {
      DebuggerInfo.printError("Breakpoint is not set. A method name must be one or more letters.");
      return Optional.empty();
    }
    BreakPoint bp;
    if (isProbe) {
      bp = new ProbePoint(className, methodName, varNames, isBreak);
    } else {
      bp = new BreakPoint(className, methodName, varNames, isBreak);
    }
    bp.requestSetBreakPoint(vm, this);
    bps.add(bp);
    return Optional.of(bp);
  }

  /**
   * Remove breakpoint.
   *
   * @param className A target class file name
   * @param lineNumber A line number in a target java file
   */
  public void removeBreakPoint(String className, int lineNumber) {
    BreakPoint bp = new BreakPoint(className, lineNumber);
    bp.disable();
    bps.remove(bp);
  }

  /**
   * Remove breakpoint.
   *
   * @param className A target class file name
   * @param methodName A method name a class has
   */
  public void removeBreakPoint(String className, String methodName) {
    BreakPoint bp = new BreakPoint(className, methodName);
    bp.disable();
    bps.remove(bp);
  }

  /**
   * Get breakpoints
   *
   * @return breakpoints
   */
  Set<BreakPoint> getBreakPoints() {
    return bps;
  }

  /**
   * Print current location infomation
   *
   * @param prefix print reason
   * @param lineNumber line number
   * @param className class name
   * @param methodName method name
   */
  void printCurrentLocation(String prefix, int lineNumber, String className, String methodName) {
    DebuggerInfo.print(
        prefix + ": line=" + lineNumber + ", class=" + className + ", method=" + methodName);
  }

  /**
   * Print source code.
   *
   * @param prefix print reason
   * @param srcDir source directory
   */
  void printSrcAtCurrentLocation(String prefix, String srcDir) {
    if (!checkCurrentTRef()) {
      return;
    }
    try {
      Location currentLocation = currentTRef.frame(0).location();
      int lineNumber = currentLocation.lineNumber();
      String srcRelPath = currentLocation.sourcePath();
      String className = Name.toClassNameFromSourcePath(srcRelPath);
      String methodName = currentLocation.method().name();
      printCurrentLocation(prefix, lineNumber, className, methodName);
      if (!srcDir.equals("")) {
        String srcAbsPath = srcDir + File.separator.charAt(0) + srcRelPath;
        DebuggerInfo.printSrc(srcAbsPath, lineNumber);
      }
    } catch (IncompatibleThreadStateException | AbsentInformationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /** Print local variables */
  void printLocals() {
    if (!checkCurrentTRef()) {
      return;
    }
    try {
      StackFrame stackFrame = currentTRef.frame(0);
      List<LocalVariable> vars = stackFrame.visibleVariables();
      Map<LocalVariable, Value> visibleVariables = stackFrame.getValues(vars);
      System.out.println("Method arguments:");
      visibleVariables.entrySet().stream()
          .filter(entry -> entry.getKey().isArgument())
          .forEach(
              entry -> {
                System.out.println(entry.getKey().name() + " = " + entry.getValue().toString());
              });
      System.out.println("\nLocal variables:");
      visibleVariables.entrySet().stream()
          .filter(entry -> !entry.getKey().isArgument())
          .forEach(
              entry -> {
                System.out.println(entry.getKey().name() + " = " + entry.getValue().toString());
              });
    } catch (IncompatibleThreadStateException | AbsentInformationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /** Print stacktrace */
  void printStackTrace() {
    if (!checkCurrentTRef()) {
      return;
    }
    try {
      System.out.println();
      System.out.println(stackTraceKey(currentTRef));
    } catch (IncompatibleThreadStateException e) {
      e.printStackTrace();
    }
  }

  public ArrayList<DebugResult> getResults() {
    ArrayList<DebugResult> drs = new ArrayList<>();
    bps.forEach(
        bp -> {
          bp.getResults()
              .forEach(
                  (key, value) -> {
                    drs.add(value);
                  });
        });
    drs.sort(
        Comparator.comparing(DebugResult::getClassName)
            .thenComparing(DebugResult::getLineNumber)
            .thenComparing(DebugResult::getName));
    return drs;
  }

  public ArrayList<DebugResult> getResults(String varName) {
    ArrayList<DebugResult> drs =
        (ArrayList<DebugResult>)
            bps.stream()
                .map(bp -> bp.getResult(varName))
                .filter(res -> res.isPresent())
                .map(res -> res.get())
                .sorted(
                    Comparator.comparing(DebugResult::getClassName)
                        .thenComparing(DebugResult::getLineNumber)
                        .thenComparing(DebugResult::getName))
                .collect(Stream.toArrayList());
    return drs;
  }
}
