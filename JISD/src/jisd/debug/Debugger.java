package jisd.debug;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import jisd.debug.value.ValueInfo;
import jisd.vis.IExporter;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The JISDLab's main debugger.
 *
 * @author sugiyama
 */
public class Debugger {
  /** Manage points */
  PointManager pm;

  /** Default sleep time when executes run()/cont()/restart() */
  @Getter @Setter public static int defaultSleepTime = 0;

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

  @Getter @Setter String projectName;
  @Getter @Setter String projectId;

  @Getter List<String> srcDir = new ArrayList<String>();

  public Debugger(String main, String options) {
    this(main, options, false);
  }

  public Debugger(String main, String options, boolean usesProbeJ) {
    this(main, options, 39876, usesProbeJ);
  }

  public Debugger(String main, String options, int port, boolean usesProbeJ) {
    init(main, options, "localhost", port, false, usesProbeJ, "", "");
  }

  public Debugger(int port) {
    this(port, false);
  }

  public Debugger(int port, String projectName, String projectId) {
    init("", "", "localhost", port, true, false, projectName, projectId);
  }

  public Debugger(String host, int port) {
    this(host, port, false);
  }

  public Debugger(int port, boolean usesProbeJ) {
    this("localhost", port, usesProbeJ);
  }

  public Debugger(String host, int port, boolean usesProbeJ) {
    init("", "", host, port, true, usesProbeJ, "", "");
  }

  Debugger(String main,
           String options,
           String host,
           int port,
           boolean isRemoteDebug,
           boolean usesProbeJ) {
    init(main, options, host, port, isRemoteDebug, usesProbeJ, "", "");
  }

  void init(
      String main,
      String options,
      String host,
      int port,
      boolean isRemoteDebug,
      boolean usesProbeJ,
      String projectName,
      String projectId) {
    setMain(main);
    setOptions(options);
    setHost(host);
    setPort(port);
    setProjectName(projectName);
    setProjectId(projectId);
    this.usesProbeJ = usesProbeJ;
    this.isRemoteDebug = isRemoteDebug;
    pm = new PointManager();
    vmManager = VMManagerFactory.create(this, main, options, host, port, isRemoteDebug, usesProbeJ, projectName, projectId);
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

  /**
   * Execute "step in"/"step into"
   *
   * @return
   */
  public String step() {
    return step(1);
  }

  /**
   * Execute "step in"/"step into" multiple times
   *
   * @return
   */
  public String step(int times) {
    pm.requestStepInto(vmManager, times);
    return list();
  }

  /**
   * Execute "step in"/"step into" (alias of step())
   *
   * @return
   */
  public String stepIn() {
    return step();
  }

  /**
   * Execute "step in"/"step into" multiple times (alias of step(times))
   *
   * @return
   */
  public String stepIn(int times) {
    return step(times);
  }

  /**
   * Execute "step in"/"step into" (alias of step())
   *
   * @return
   */
  public String stepInto() {
    return step();
  }

  /**
   * Execute "step in"/"step into" multiple times (alias of step(times))
   *
   * @return
   */
  public String stepInto(int times) {
    return step(times);
  }

  /**
   * Execute "step over"
   *
   * @return
   */
  public String next() {
    return next(1);
  }

  /**
   * Execute "step over" multiple times
   *
   * @return
   */
  public String next(int times) {
    pm.requestStepOver(vmManager, times);
    return list();
  }

  /**
   * Execute "step over" (alias of next())
   *
   * @return
   */
  public String stepOver() {
    return next();
  }

  /**
   * Execute "step over" multiple times (alias of next(times))
   *
   * @return
   */
  public String stepOver(int times) {
    return next(times);
  }

  /**
   * Execute "step out"/"step return"
   *
   * @return
   */
  public String finish() {
    return finish(1);
  }

  /**
   * Execute "step out"/"step return" multiple times
   *
   * @return
   */
  public String finish(int times) {
    pm.requestStepOut(vmManager, times);
    return list();
  }

  /**
   * Execute "step out"/"step return" (alias of finish())
   *
   * @return
   */
  public String stepOut() {
    return finish();
  }

  /**
   * Execute "step out"/"step return" multiple times (alias of finish(times))
   *
   * @return
   */
  public String stepOut(int times) {
    return finish(times);
  }

  /**
   * Execute "step out"/"step return" (alias of finish())
   *
   * @return
   */
  public String stepReturn() {
    return finish();
  }

  /**
   * Execute "step out"/"step return" multiple times (alias of finish(times))
   *
   * @return
   */
  public String stepReturn(int times) {
    return finish(times);
  }

  /** Continue execution from breakpoint */
  public String cont() {
    return cont(defaultSleepTime);
  }

  /** Continue execution from breakpoint */
  public String cont(int sleepTime) {
    pm.resumeThread();
    pm.setBreaked(false);
    if (sleepTime == 0) {
      return "";
    }
    Utility.sleep(sleepTime);
    try {
      pm.printSrcAtCurrentLocation("Current location,", srcDir);
    } catch (VMNotSuspendedException e) {
      return "";
    }
    return uri();
  }

  /** Print source code */
  public String list(String srcDir) {
    pm.printSrcAtCurrentLocation("Current location,", new ArrayList<>(Arrays.asList(srcDir)));
    return uri();
  }

  /** Print source code */
  public String list() {
    try {
      pm.printSrcAtCurrentLocation("Current location,", srcDir);
    } catch (VMNotSuspendedException e) {
      DebuggerInfo.printError("Debugger not suspended");
      return "";
    }
    return uri();
  }

  /** Print all local variables in current stack frame */
  public void locals() {
    pm.printLocals();
  }

  /** Print all variables in current stack frame */
  public void vars() { pm.printDebugResults(); }

  /** Get DebugResults at the current location (alias of getCurrentDebugResults()) */
  public HashMap<String, DebugResult> drs() {
    return getCurrentDebugResults();
  }

  /** Get DebugResults at the current location */
  public HashMap<String, DebugResult> getCurrentDebugResults() {return pm.printDebugResults();}

  /** Print stacktrace in current stack frame. */
  public void where() {
    pm.printStackTrace();
  }

  /** Return a current file location. */
  public Location loc() {
    return pm.getCurrentLocation();
  }

  /** Return a string which represents the current file location by VSCode-like format. */
  public String uri() {
    var loc = loc();
    var lineNumber = loc.lineNumber;
    return uri(loc, lineNumber);
  }

  /** Return a string which represents the current file location by VSCode-like format. */
  public String uri(Location loc, int lineNumber) {
    return Utility.uri(loc, srcDir, lineNumber);
  }

  /** Return current file location by VSCode-like format. */
  public String uri(int lineNumber) {
    var loc = loc();
    return Utility.uri(loc, srcDir, lineNumber);
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
  public String run() {
    return run(defaultSleepTime);
  }

  /**
   * Start up the debugger.
   *
   * @param sleepTime Wait time after the debugger starts running
   */
  public String run(int sleepTime) {
    if (vmThread != null) {
      throw new VMAlreadyStartedException("VM has already started once.");
    }

    vmThread = new Thread(vmManager);
    vmThread.start();
    if (!isRemoteDebug && usesProbeJ) {
      Utility.sleep(1000);
    }
    if (sleepTime == 0) {
      return "";
    }
    Utility.sleep(sleepTime);
    try {
      pm.printSrcAtCurrentLocation("Current location,", srcDir);
    } catch (VMNotSuspendedException e) {
      return "";
    }
    return uri();
  }

  /** Shutdown the debugger. */
  public void exit() {
    pm.completeStep();
    try {
      vmManager.shutdown();
    } catch (VMDisconnectedException e) {
      // do nothing
    }
    vmThread = null;
  }

  /** Shutdown the debugger.(alias of exit()) */
  public void quit() {
    exit();
  }

  /** Restart the debugger at once.(equals to restart(0)) */
  public String restart() {
    return restart(defaultSleepTime);
  }

  /**
   * Restart the debugger (breakpoints inherited).
   *
   * @param sleepTime Wait time after the debugger starts running
   */
  public String restart(int sleepTime) {
    exit();
    clearResults();
    vmManager = VMManagerFactory.create(this, main, options, host, port, isRemoteDebug, usesProbeJ, projectName,projectId);
    vmManager.prepareStart(pm);
    return run(sleepTime);
  }

  /** <div>Redefine the debugger so that the parameters are the same (breakpoints <strong>NOT</strong> inherited).</div>
   *  <br>
   * <div>Inherited parameters:
   *   <ul>
   *     <li> main </li>
   *     <li> options </li>
   *     <li> host </li>
   *     <li> port </li>
   *     <li> break or probe </li>
   *     <li> srcDir </li>
   *     <li> exporters </li>
   *   </ul>
   * </div>
   * */
  public Debugger redef() {
    var dbg = new Debugger(main, options, host, port, isRemoteDebug, usesProbeJ);
    dbg.setSrcDir(srcDir.toArray(new String[0]));
    exporters.stream().peek(e->dbg.setExporter(e)).collect(Collectors.toList());
    return dbg;
  }

  /**
   * Reset this debugger own (breakpoints <strong>NOT</strong> inherited).
   */
  public void reset() {
    exit();
    pm = new PointManager();
    vmManager = VMManagerFactory.create(this, main, options, host, port, isRemoteDebug, usesProbeJ, projectName, projectId);
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
