package jisd.debug;

import com.sun.jdi.ThreadReference;
import jisd.debug.value.ValueInfo;
import jisd.vis.IExporter;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The JISDLab's main debugger.
 *
 * @author sugiyama
 */
public class Debugger {
  /** Manage points */
  PointManager pm;

  @Getter @Setter String main, options;
  /** VM thread */
  Thread vmThread;
  /** VM manager */
  @Getter VMManager vmManager;
  /** use attaching connector or not */
  boolean isRemoteDebug;
  /** uses ProbeJ ? */
  boolean usesProbeJ;
  /** observer(exporter) */
  @Getter
  List<IExporter> exporters = new ArrayList<IExporter>();

  @Getter int port;

  @Getter @Setter String host;

  @Getter List<String> srcDir = new ArrayList<String>();

  public Debugger(String main, String options) {
    this(main, options, false);
  }

  public Debugger(String main, String options, boolean usesProbeJ) {
    this(main, options, 39876, usesProbeJ);
  }

  public Debugger(String main, String options, int port, boolean usesProbeJ) {
    init(main, options, "localhost", port, false, usesProbeJ);
  }

  public Debugger(int port) {
    this(port, false);
  }

  public Debugger(String host, int port) {
    this(host, port, false);
  }

  public Debugger(int port, boolean usesProbeJ) {
    this("localhost", port, usesProbeJ);
  }

  public Debugger(String host, int port, boolean usesProbeJ) {
    init("", "", host, port, true, usesProbeJ);
  }

  void init(
      String main,
      String options,
      String host,
      int port,
      boolean isRemoteDebug,
      boolean usesProbeJ) {
    setMain(main);
    setOptions(options);
    setHost(host);
    setPort(port);
    this.usesProbeJ = usesProbeJ;
    this.isRemoteDebug = isRemoteDebug;
    pm = new PointManager();
    vmManager = VMManagerFactory.create(this, main, options, host, port, isRemoteDebug, usesProbeJ);
    vmManager.prepareStart(pm);
  }

  public void setPort(int port) {
    if (port < 1024 || port > 65535) {
      this.port = 8000;
      DebuggerInfo.printError(
          "This port is out of range. So, now port is set 8000. Please set the port bewtween 1024 ~ 65535.");
    } else {
      this.port = port;
    }
  }

  public void setSrcDir(String... paths) {
    srcDir = Arrays.stream(paths).collect(Collectors.toList());
  }

  /**
   * Set an exporter
   */
  public void setExporter(IExporter exporter) {
    exporters.add(exporter);
  }

  /**
   * Notify all exporters to update debug data
   */
  public int notifyExporters(ValueInfo valueInfo) {
    int sleepTimeMax = 0;
    for (IExporter exporter : exporters) {
      int sleepTime = exporter.update(valueInfo);
      sleepTimeMax = (sleepTime < sleepTimeMax) ? sleepTimeMax : sleepTime;
    }
    return sleepTimeMax;
  }

  /**
   * Clear all exporters
   */
  public void clearExporters() {
    exporters.clear();
  }

  /**
   * Remove an exporter
   */
  public void removeExporter(IExporter exporter) {
    exporters.remove(exporter);
  }

  /** Set a breakpoint by a line number. */
  public Optional<Point> stopAt(int lineNumber) {
    return stopAt(main, lineNumber);
  }
  /** Set a breakpoint by a line number. */
  public Optional<Point> stopAt(int lineNumber, String[] varNames) {
    return stopAt(main, lineNumber, varNames);
  }
  /** Set a breakpoint by a line number. */
  public Optional<Point> stopAt(String className, int lineNumber) {
    return stopAt(className, lineNumber, new String[0]);
  }
  /** Set a breakpoint by a line number. */
  public Optional<Point> stopAt(String className, int lineNumber, String[] varNames) {
    if (usesProbeJ) {
      DebuggerInfo.printError("If you use ProbeJ, you can use watch() only.");
      return Optional.empty();
    }
    return pm.setPoint(
        vmManager, className, lineNumber, new ArrayList<>(Arrays.asList(varNames)), true, false);
  }

  /** Set a breakpoint by a method name. */
  public Optional<Point> stopAt(String methodName) {
    return stopAt(main, methodName);
  }
  /** Set a breakpoint by a method name. */
  public Optional<Point> stopAt(String methodName, String[] varNames) {
    return stopAt(main, methodName, varNames);
  }
  /** Set a breakpoint by a method name. */
  public Optional<Point> stopAt(String className, String methodName) {
    return stopAt(className, methodName, new String[0]);
  }
  /** Set a breakpoint by a method name. */
  public Optional<Point> stopAt(String className, String methodName, String[] varNames) {
    if (usesProbeJ) {
      DebuggerInfo.printError("If you use ProbeJ, you can use watch() only.");
      return Optional.empty();
    }
    return pm.setPoint(
        vmManager, className, methodName, new ArrayList<>(Arrays.asList(varNames)), true, false);
  }

  /** Set a watchpoint by a line number. */
  public Optional<Point> watch(int lineNumber) {
    if (usesProbeJ) {
      DebuggerInfo.printError("variable names are not set.");
      return Optional.empty();
    }
    return watch(main, lineNumber);
  }

  /** Set a watchpoint by a line number. */
  public Optional<Point> watch(int lineNumber, String[] varNames) {
    return watch(main, lineNumber, varNames);
  }

  /** Set a watchpoint by a line number. */
  public Optional<Point> watch(String className, int lineNumber) {
    if (usesProbeJ) {
      DebuggerInfo.printError("variable names are not set.");
      return Optional.empty();
    }
    return watch(className, lineNumber, new String[0]);
  }

  /** Set a watchpoint or <strong>a probepoint</strong> by a line number. */
  public Optional<Point> watch(String className, int lineNumber, String[] varNames) {
    return pm.setPoint(
        vmManager,
        className,
        lineNumber,
        new ArrayList<>(Arrays.asList(varNames)),
        false,
        usesProbeJ);
  }

  /** Set a watchpoint by a method name. */
  public Optional<Point> watch(String methodName) {
    return watch(main, methodName);
  }
  /** Set a watchpoint by a method name. */
  public Optional<Point> watch(String methodName, String[] varNames) {
    return watch(main, methodName, varNames);
  }
  /** Set a watchpoint by a method name. */
  public Optional<Point> watch(String className, String methodName) {
    return watch(className, methodName, new String[0]);
  }
  /** Set a watchpoint by a method name. */
  public Optional<Point> watch(String className, String methodName, String[] varNames) {
    if (usesProbeJ) {
      DebuggerInfo.printError("Cannot allow to set a probepoint by a method name.");
      return Optional.empty();
    }
    return pm.setPoint(
        vmManager,
        className,
        methodName,
        new ArrayList<>(Arrays.asList(varNames)),
        false,
        usesProbeJ);
  }

  /** Execute "step in"/"step into" */
  public void step() {
    step(1);
  }

  /** Execute "step in"/"step into" */
  public void step(int times) {
    pm.requestStepInto(vmManager, times);
  }

  /** Execute "step in"/"step into" (alias of step()) */
  public void stepIn() {
    step();
  }

  /** Execute "step in"/"step into" (alias of step()) */
  public void stepIn(int times) {
    step(times);
  }

  /** Execute "step over" */
  public void next() {
    next(1);
  }

  /** Execute "step in"/"step into" (alias of next()) */
  public void next(int times) {
    pm.requestStepOver(vmManager, times);
  }

  /** Execute "step in"/"step into" (alias of next()) */
  public void stepOver() {
    next();
  }

  /** Execute "step in"/"step into" (alias of next()) */
  public void stepOver(int times) {
    next(times);
  }

  /** Execute "step out"/"step return" */
  public void finish() {
    finish(1);
  }

  /** Execute "step out"/"step return" */
  public void finish(int times) {
    pm.requestStepOut(vmManager, times);
  }

  /** Execute "step in"/"step into" (alias of finish()) */
  public void stepOut() {
    finish();
  }

  /** Execute "step in"/"step into" (alias of finish()) */
  public void stepOut(int times) {
    finish(times);
  }

  /** Continue execution from breakpoint */
  public void cont() {
    pm.resumeThread();
    pm.setBreaked(false);
  }

  /** Print source code */
  public void list(String srcDir) {
    pm.printSrcAtCurrentLocation("Current location,", new ArrayList<>(Arrays.asList(srcDir)));
  }

  /** Print source code */
  public void list() {
    pm.printSrcAtCurrentLocation("Current location,", srcDir);
  }

  /** Print all local variables in current stack frame */
  public void locals() {
    pm.printLocals();
  }

  /** Print stacktrace in current stack frame. */
  public void where() {
    pm.printStackTrace();
  }

  /** Remove breakpoint with a line number. */
  public void clear(int lineNumber) {
    clear(main, lineNumber);
  }

  /** Remove breakpoint with a class name and a line number. */
  public void clear(String className, int lineNumber) {
    pm.removePoint(className, lineNumber);
  }

  /**
   * Remove breakpoint with a method name.
   *
   * @param methodName A method name in a target java file
   */
  public void clear(String methodName) {
    clear(main, methodName);
  }

  /** Remove breakpoint with a class name and a method name. */
  public void clear(String className, String methodName) {
    pm.removePoint(className, methodName);
  }

  /** Start up the debugger.(equals to run(0)) */
  public void run() {
    run(0);
  }

  /**
   * Start up the debugger.
   *
   * @param sleepTime Wait time after the debugger starts running
   */
  public void run(int sleepTime) {
    if (vmThread != null) {
      throw new VMAlreadyStartedException("VM has already started once.");
    }
    vmThread = new Thread(vmManager);
    vmThread.start();
    if (!isRemoteDebug && usesProbeJ) {
      Utility.sleep(1000);
    }
    Utility.sleep(sleepTime);
  }

  /** Shutdown the debugger. */
  public void exit() {
    pm.completeStep();
    vmManager.shutdown();
    vmThread = null;
  }

  /** Shutdown the debugger.(alias of exit()) */
  public void quit() {
    exit();
  }

  /** Restart the debugger at once.(equals to restart(0)) */
  public void restart() {
    restart(0);
  }

  /**
   * Restart the debugger.
   *
   * @param sleepTime Wait time after the debugger starts running
   */
  public void restart(int sleepTime) {
    exit();
    clearResults();
    vmManager = VMManagerFactory.create(this, main, options, host, port, isRemoteDebug, usesProbeJ);
    vmManager.prepareStart(pm);
    run(sleepTime);
  }

  /** Clear debug results all. */
  public void clearResults() {
    ArrayList<Point> bps = getPoints();
    bps.forEach(
        bp -> {
          bp.reset();
        });
  }

  /**
   * Get debug results.
   *
   * @return Debug results
   */
  public ArrayList<DebugResult> getResults() {
    return pm.getResults();
  }

  /**
   * Get debug results a variable name matches.
   *
   * @param varName variable name
   * @return Debug results
   */
  public ArrayList<DebugResult> getResults(String varName) {
    return pm.getResults(varName);
  }

  /**
   * Get observation points.
   *
   * @return observation points
   */
  public ArrayList<Point> getPoints() {
    return new ArrayList<>(pm.getPoints());
  }

  public ThreadReference thread() {
    return pm.thread();
  }
}
