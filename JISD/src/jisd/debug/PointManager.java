package jisd.debug;

import com.sun.jdi.*;
import com.sun.jdi.request.DuplicateRequestException;
import com.sun.jdi.request.StepRequest;
import jisd.util.Name;
import jisd.util.Print;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnStep;
import org.jdiscript.requests.ChainingStepRequest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages observation points.
 *
 * @author sugiyama
 */
class PointManager {
  /** points */
  private final Set<Point> ps = new HashSet<>();

  /** Current Thread Reference */
  @Getter(AccessLevel.PACKAGE)
  @Setter(AccessLevel.PACKAGE)
  ThreadReference currentTRef;

  HashMap<String, DebugResult> currentDebugResults;
  /** is processing now? */
  @Getter
  @Setter(AccessLevel.PACKAGE)
  volatile boolean isProcessing;
  /** is breaked now? */
  @Setter volatile boolean isBreaked;

  /** break or step request count */
  @Getter @Setter(AccessLevel.PACKAGE)
  volatile int count;

  Optional<ChainingStepRequest> stepReq = Optional.empty();

  /** */
  PointManager() {
    init();
  }

  void init() {
    setProcessing(false);
    setCurrentTRef(null);
    setBreaked(false);
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
      com.sun.jdi.Location loc = f.location();
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
    isBreaked = false;
    if (!checkCurrentTRef()) {
      return;
    }
    currentTRef.resume();
    currentTRef = null;
    currentDebugResults = null;
  }

  void completeStep() {
    if (stepReq.isPresent()) {
      stepReq.get().disable();
    }
    stepReq = Optional.empty();
    setProcessing(false);
  }

  /** varNames and debug result */
  HashMap<String, DebugResult> createDebugResults() {
    HashMap<String, DebugResult> drs = new HashMap<>();
    try {
      var stackFrame = currentTRef.frame(0);
      // search the breakpoint which caused this event.
      var jLoc = stackFrame.location();
      int bpLineNumber = jLoc.lineNumber();
      String bpClassName = Name.toClassNameFromSourcePath(jLoc.sourcePath());
      String bpMethodName = jLoc.method().name();
      List<ReferenceType> rts = currentTRef.virtualMachine().classesByName(bpClassName);
      var rt = rts.get(0);
      var date = LocalDateTime.now();
      // get variable data from target VM
      List<LocalVariable> vars;
      var obj = stackFrame.thisObject();
      if (obj != null) {
        obj.getValues(rt.visibleFields())
          .forEach(
            (f, v) -> {
              String varName = "this." + f.name();
              Location loc =
                new Location(
                  bpClassName, bpMethodName, bpLineNumber, varName);
              var dr = new DebugResult(loc);
              dr.addValue(v, date);
              drs.put(varName, dr);
            });
      } else {
        rt.allFields().stream()
          .filter(f -> f.isStatic())
          .forEach(
            f -> {
              String varName = "this." + f.name();
              Location loc =
                new Location(
                  bpClassName, bpMethodName, bpLineNumber, varName);
              var dr = new DebugResult(loc);
              dr.addValue(rt.getValue(f), date);
              drs.put(varName, dr);
            });
      }
      vars = stackFrame.visibleVariables();
      Map<LocalVariable, Value> visibleVariables = stackFrame.getValues(vars);
      // add debug result
      for (Map.Entry<LocalVariable, Value> entry : visibleVariables.entrySet()) {
        String varName = entry.getKey().name();
        Value jValue = entry.getValue();
        Location loc = new Location(bpClassName, bpMethodName, bpLineNumber, varName);
        var dr = new DebugResult(loc);
        dr.addValue(jValue, date);
        drs.put(varName, dr);
      }
    } catch (IncompatibleThreadStateException e) {
      throw new RuntimeException(e);
    } catch (AbsentInformationException e) {
      throw new RuntimeException(e);
    }
    return drs;
  }

  /**
   * request step execution
   *
   * @param depth depth of step
   */
 void requestStep(VMManager vmMgr, int depth, int times) {
    if (times <= 0) {
      Print.err("Negative number");
      return;
    }
    /** A procedure on step. */
    if (!(vmMgr instanceof JDIManager)) {
      /* do nothing */
      return;
    }
    for (; times > 0; times--) {
      if (!checkCurrentTRef()) {
        setCount(0);
        return;
      }
      setCount(times);
      if (isProcessing) {
        completeStep();
      }
      JDIScript j = ((JDIManager) vmMgr).getJDI();
      int finalTimes = count;
      OnStep onStep =
        j.once(
          (s) -> {
            if (isBreaked) {
              if (finalTimes == 1) {
                DebuggerInfo.print("Step completed");
              }
              completeStep();
              return;
            }
            int lineNumber = s.location().lineNumber();
            String methodName = s.location().method().name();
            if (finalTimes == 1) {
              try {
                String className = Name.toClassNameFromSourcePath(s.location().sourcePath());
                printCurrentLocation("Step completed", lineNumber, className, methodName);
              } catch (AbsentInformationException e) {
                printCurrentLocation("Step completed", lineNumber, "Not attached", methodName);
              }
            }
            currentTRef = s.thread();
            currentTRef.suspend();
            completeStep();
          });
      try {
        stepReq =
          Optional.of(j.stepRequest(currentTRef, StepRequest.STEP_LINE, depth, onStep).enable());
      } catch (DuplicateRequestException e) {
        e.printStackTrace();
      }
      resumeThread();
      isProcessing = true;
      boolean isBreakLoop = sleep(j);
      if (isBreakLoop) {
        return;
      }
    }
  }

  /** Sleep main thread until current bpm process is done */
  boolean sleep(JDIScript j) {
    try {
      while (isProcessing) {
        Thread.sleep(100);
      }
    } catch (InterruptedException e) {
      DebuggerInfo.print("Interrupted.");
      return true;
    }
    return false;
  }

  /**
   * request step into execution
   *
   * @return
   */
  void requestStepInto(VMManager vm, int times) {
    requestStep(vm, StepRequest.STEP_INTO, times);
  }

  /**
   * request step over execution
   *
   * @return
   */
  void requestStepOver(VMManager vm, int times) {
    requestStep(vm, StepRequest.STEP_OVER, times);
  }

  /**
   * request step out execution
   *
   * @return
   */
  void requestStepOut(VMManager vm, int times) {
    requestStep(vm, StepRequest.STEP_OUT, times);
  }

  /** Request VM to set a point */
  void requestSetPoints(VMManager vm) {
    ps.forEach(
        bp -> {
          bp.requestSetPoint(vm, this);
        });
  }

  /**
   * Set point by a line number.
   *
   * @param className class name
   * @param lineNumber line number
   * @param varNames variable names
   * @param isBreak break or not at points
   * @return point
   */
  public Optional<Point> setPoint(
      VMManager vm,
      String className,
      int lineNumber,
      ArrayList<String> varNames,
      boolean isBreak,
      boolean isProbe) {
    if (className.length() == 0) {
      DebuggerInfo.printError("Point is not set. A class name must be one or more letters.");
      return Optional.empty();
    }
    if (lineNumber <= 0) {
      DebuggerInfo.printError(
          "Point is not set. A line number must be a non-negative integer(> 0).");
      return Optional.empty();
    }
    Point bp;
    if (isProbe) {
      bp = new ProbePoint(className, lineNumber, varNames, isBreak);
    } else {
      bp = new BreakPoint(className, lineNumber, varNames, isBreak);
    }
    if (ps.contains(bp)) {
      throw new RuntimeException("This observation point has already been set.");
    }
    bp.requestSetPoint(vm, this);
    ps.add(bp);
    return Optional.of(bp);
  }

  /**
   * Set point by a method name.
   *
   * @param className class name
   * @param methodName method name
   * @param varNames value name
   * @param isBreak break or not at points
   * @return point
   */
  public Optional<Point> setPoint(
      VMManager vm,
      String className,
      String methodName,
      ArrayList<String> varNames,
      boolean isBreak,
      boolean isProbe) {
    if (className.length() == 0) {
      DebuggerInfo.printError("Point is not set. A class name must be one or more letters.");
      return Optional.empty();
    }
    if (methodName.length() == 0) {
      DebuggerInfo.printError("Point is not set. A method name must be one or more letters.");
      return Optional.empty();
    }
    Point bp;
    if (isProbe) {
      bp = new ProbePoint(className, methodName, varNames, isBreak);
    } else {
      bp = new BreakPoint(className, methodName, varNames, isBreak);
    }
    if (ps.contains(bp)) {
      throw new RuntimeException("This observation point has already been set.");
    }
    bp.requestSetPoint(vm, this);
    ps.add(bp);
    return Optional.of(bp);
  }

  /**
   * Remove point.
   *
   * @param className A target class file name
   * @param lineNumber A line number in a target java file
   */
  public void removePoint(String className, int lineNumber) {
    ArrayList<Point> psArray = new ArrayList<>(ps);
    for (int i = 0; i < psArray.size(); i++) {
      Point point = psArray.get(i);
      if (point.getClassName().equals(className)) {
        if (point.getLineNumber() == lineNumber) {
          point.disable();
          ps.remove(point);
        }
      }
    }
  }

  /**
   * Remove point.
   *
   * @param className A target class file name
   * @param methodName A method name a class has
   */
  public void removePoint(String className, String methodName) {
    ArrayList<Point> psArray = new ArrayList<>(ps);
    for (int i = 0; i < psArray.size(); i++) {
      Point point = psArray.get(i);
      if (point.getClassName().equals(className)) {
        if (point.getMethodName().equals(methodName)) {
          point.disable();
          ps.remove(point);
        }
      }
    }
  }

  /**
   * Get points
   *
   * @return points
   */
  Set<Point> getPoints() {
    return ps;
  }

  /**
   * Print current location information
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

  /**
   * Print source code.
   *
   * @param prefix print reason
   * @param srcDirs source directory
   */
  void printSrcAtCurrentLocation(String prefix, List<String> srcDirs) throws VMNotSuspendedException {
    if (!checkCurrentTRef()) {
      throw new VMNotSuspendedException("");
    }
    srcDirs.add(".");
    try {
      com.sun.jdi.Location currentLocation = currentTRef.frame(0).location();
      int lineNumber = currentLocation.lineNumber();
      String srcRelPath = currentLocation.sourcePath();
      String className = Name.toClassNameFromSourcePath(srcRelPath);
      String methodName = currentLocation.method().name();
      printCurrentLocation(prefix, lineNumber, className, methodName);
      for (int i = 0; i < srcDirs.size(); i++) {
        var srcDir = srcDirs.get(i);
        var srcAbsPathStr = srcDir + File.separator.charAt(0) + srcRelPath;
        var srcAbsPath = Paths.get(srcAbsPathStr);
        if (Files.exists(srcAbsPath)) {
          DebuggerInfo.printSrc(srcAbsPathStr, lineNumber);
          return;
        }
      }
      DebuggerInfo.printError(srcRelPath+" not found. Set srcDir by list(String srcDir) or Debugger.setSrcDir(String... srcDir))");
    } catch (IncompatibleThreadStateException | AbsentInformationException e ) {
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

  Location getCurrentLocation() {
    if (!checkCurrentTRef()) {
      return null;
    }
    try {
      var currentLocation = currentTRef.frame(0).location();
      int lineNumber = currentLocation.lineNumber();
      String srcRelPath = currentLocation.sourcePath();
      String className = Name.toClassNameFromSourcePath(srcRelPath);
      String methodName = currentLocation.method().name();
      Location loc = new Location(className, methodName, lineNumber, "");
      return loc;
    } catch (IncompatibleThreadStateException | AbsentInformationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
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
    ps.forEach(
        bp -> {
          bp.getResults()
              .forEach(
                  (key, value) -> {
                    drs.add(value);
                  });
        });
    drs.sort(Utility.compDR);
    return drs;
  }

  public ArrayList<DebugResult> getResults(String varName) {
    ArrayList<DebugResult> drs =
        (ArrayList<DebugResult>)
            ps.stream()
                .map(bp -> bp.getResults(varName))
                .filter(res -> res.isPresent())
                .map(res -> res.get())
                .sorted(Utility.compDR)
                .collect(Collectors.toList());
    return drs;
  }

  ThreadReference thread() {
    if (!checkCurrentTRef()) {
      return null;
    }
    return currentTRef;
  }

  HashMap<String, DebugResult> printDebugResults() {
    if (!checkCurrentTRef()) {
      return new HashMap<>();
    }
    if (currentDebugResults == null) {
      currentDebugResults = createDebugResults();
    }
    Utility.prints(currentDebugResults);
    return currentDebugResults;
  }
}
