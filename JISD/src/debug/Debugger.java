package debug;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * JISDLab's main debugger
 *
 * @author sugiyama
 */
public class Debugger {
  /** Manage breakpoints */
  PointManager pm;
  /** Target class setting items */
  @Getter @Setter String main, options;
  /** JDI thread */
  Thread vmThread;
  /** VM manager */
  VMManager vmManager;
  /** use attaching connector or not */
  boolean isRemoteDebug;
  /** uses ProbeJ ? */
  boolean usesProbeJ;
  /** attaching port */
  @Getter int port;
  /** */
  @Getter @Setter String host;

  /**
   * Constructor
   *
   * @param main A target class file name
   * @param options A target class path setting
   */
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

  /**
   * Sleep main thread
   *
   * @param sleepTime wait time
   */
  public static void sleep(int sleepTime) {
    try {
      Thread.sleep(sleepTime);
    } catch (InterruptedException e) {
      DebuggerInfo.print("Interrupted.");
    }
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
    vmManager = VMManagerFactory.create(main, options, host, port, isRemoteDebug, usesProbeJ);
    vmManager.prepareStart(pm);
  }

  // ********** debugger settings ************************************************************//

  // ********** stop ************************************************************//

  // ********** debugger settings ************************************************************//
  public void setPort(int port) {
    if (port < 1024 || port > 65535) {
      this.port = 8000;
      DebuggerInfo.printError(
          "This port is out of range. So, now port is set 8000. Please set the port bewtween 1024 ~ 65535.");
    } else {
      this.port = port;
    }
  }

  /**
   * Set breakpoint with a line number.
   *
   * @param lineNumber A line number in a target java file
   * @return breakpoint(optional)
   */
  public Optional<Point> stopAt(int lineNumber) {
    return stopAt(main, lineNumber);
  }

  /**
   * Set breakpoint with a line number and variable names.
   *
   * @param lineNumber line number
   * @param varNames variable names
   * @return breakpoint(optional)
   */
  public Optional<Point> stopAt(int lineNumber, String[] varNames) {
    return stopAt(main, lineNumber, varNames);
  }

  /**
   * Set breakpoint with a line number and variable names.
   *
   * @param className class name
   * @param lineNumber line number
   * @return breakpoint(optional)
   */
  public Optional<Point> stopAt(String className, int lineNumber) {
    return stopAt(className, lineNumber, new String[0]);
  }

  /**
   * Set breakpoint with a line number and variable names.
   *
   * @param className class name
   * @param lineNumber line number
   * @param varNames variable names
   * @return breakpoint(optional)
   */
  public Optional<Point> stopAt(String className, int lineNumber, String[] varNames) {
    if (usesProbeJ) {
      DebuggerInfo.printError("If you use ProbeJ, you can use watch() only.");
      return Optional.empty();
    }
    return pm.setPoint(
        vmManager, className, lineNumber, new ArrayList<>(Arrays.asList(varNames)), true, false);
  }

  /**
   * Set breakpoint with a method name.
   *
   * @param methodName A method name in a target java file
   * @return breakpoint(optional)
   */
  public Optional<Point> stopAt(String methodName) {
    return stopAt(main, methodName);
  }

  /**
   * Set breakpoint with a method name and variable names.
   *
   * @param methodName method name
   * @param varNames variable names
   * @return breakpoint(optional)
   */
  public Optional<Point> stopAt(String methodName, String[] varNames) {
    return stopAt(main, methodName, varNames);
  }

  /**
   * Set breakpoint with a method name.
   *
   * @param className class name
   * @param methodName method name
   * @return breakpoint(optional)
   */
  public Optional<Point> stopAt(String className, String methodName) {
    return stopAt(className, methodName, new String[0]);
  }

  /**
   * Set breakpoint with a method name and variable names.
   *
   * @param className class name
   * @param methodName method name
   * @param varNames variable names
   * @return breakpoint(optional)
   */
  public Optional<Point> stopAt(String className, String methodName, String[] varNames) {
    if (usesProbeJ) {
      DebuggerInfo.printError("If you use ProbeJ, you can use watch() only.");
      return Optional.empty();
    }
    return pm.setPoint(
        vmManager, className, methodName, new ArrayList<>(Arrays.asList(varNames)), true, false);
  }

  /**
   * Set watchpoint with a line number.
   *
   * @param lineNumber A line number in a target java file
   * @return breakpoint(optional)
   */
  public Optional<Point> watch(int lineNumber) {
    if (usesProbeJ) {
      DebuggerInfo.printError("variable names are not set.");
      return Optional.empty();
    }
    return watch(main, lineNumber);
  }

  /**
   * Set watchpoint with a line number and variable names.
   *
   * @param lineNumber line number
   * @param varNames variable names
   * @return breakpoint(optional)
   */
  public Optional<Point> watch(int lineNumber, String[] varNames) {
    return watch(main, lineNumber, varNames);
  }

  /**
   * Set watchpoint with a line number.
   *
   * @param className class name
   * @param lineNumber line number
   * @return breakpoint(optional)
   */
  public Optional<Point> watch(String className, int lineNumber) {
    if (usesProbeJ) {
      DebuggerInfo.printError("variable names are not set.");
      return Optional.empty();
    }
    return watch(className, lineNumber, new String[0]);
  }

  /**
   * Set watchpoint with a line number and variable names.
   *
   * @param className class name
   * @param lineNumber line number
   * @param varNames variable names
   * @return breakpoint(optional)
   */
  public Optional<Point> watch(String className, int lineNumber, String[] varNames) {
    return pm.setPoint(
        vmManager,
        className,
        lineNumber,
        new ArrayList<>(Arrays.asList(varNames)),
        false,
        usesProbeJ);
  }

  /**
   * Set watchpoint with a method name.
   *
   * @param methodName A method name in a target java file
   * @return breakpoint(optional)
   */
  public Optional<Point> watch(String methodName) {
    return watch(main, methodName);
  }

  /**
   * Set watchpoint with a method name and variable names.
   *
   * @param methodName method name
   * @param varNames variable names
   * @return breakpoint(optional)
   */
  public Optional<Point> watch(String methodName, String[] varNames) {
    return watch(main, methodName, varNames);
  }

  /**
   * Set watchpoint with a method name.
   *
   * @param className class name
   * @param methodName method name
   * @return breakpoint(optional)
   */
  public Optional<Point> watch(String className, String methodName) {
    return watch(className, methodName, new String[0]);
  }

  /**
   * Set watchpoint with a method name and variable names.
   *
   * @param className class name
   * @param methodName method name
   * @param varNames variable names
   * @return breakpoint(optional)
   */
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
    pm.requestStepInto(vmManager);
    sleep();
  }

  /** Execute "step over" */
  public void next() {
    pm.requestStepOver(vmManager);
    sleep();
  }

  /** Execute "step out"/"step return" */
  public void finish() {
    pm.requestStepOut(vmManager);
    sleep();
  }

  /** Continue execution from breakpoint */
  public void cont() {
    pm.resumeThread();
    pm.setBreaked(false);
  }

  /** Print source code */
  public void list(String srcDir) {
    pm.printSrcAtCurrentLocation("Current location,", srcDir);
  }
  // ********** on breakpoint ************************************************************//

  // ********** remove breakpoint ************************************************************//

  /** Print all local variables in current stack frame */
  public void locals() {
    pm.printLocals();
  }

  /** Print stacktrace in current stack frame. */
  public void where() {
    pm.printStackTrace();
  }

  /**
   * Remove breakpoint with a line number.
   *
   * @param lineNumber A line number in a target java file
   */
  public void clear(int lineNumber) {
    clear(main, lineNumber);
  }

  /**
   * Remove breakpoint with a line number.
   *
   * @param className class name
   * @param lineNumber line number
   */
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

  // ********** remove breakpoint ************************************************************//

  // ********** debugger control ************************************************************//

  /**
   * Remove breakpoint with a method name.
   *
   * @param className class name
   * @param methodName method name
   */
  public void clear(String className, String methodName) {
    pm.removePoint(className, methodName);
  }

  /**
   * Start up the debugger.
   *
   * @param sleepTime Wait time after the debugger starts running
   */
  public void run(int sleepTime) {
    vmThread = new Thread(vmManager);
    vmThread.start();
    if (!isRemoteDebug && usesProbeJ) {
      sleep(1000);
    }
    sleep(sleepTime);
  }

  /** Sleep main thread until current bpm process is done */
  void sleep() {
    try {
      while (pm.isProcessing) {
        Thread.sleep(100);
      }
    } catch (InterruptedException e) {
      DebuggerInfo.print("Interrupted.");
    }
  }

  /** Shutdown the debugger. */
  public void exit() {
    pm.completeStep();
    vmManager.shutdown();
  }

  /** Shutdown the debugger.(alias of "exit") */
  public void quit() {
    exit();
  }

  /**
   * Restart the debugger.
   *
   * @param sleepTime Wait time after the debugger starts running
   */
  public void restart(int sleepTime) {
    exit();
    clearResults();
    vmManager = VMManagerFactory.create(main, options, host, port, isRemoteDebug, usesProbeJ);
    vmManager.prepareStart(pm);
    run(sleepTime);
  }
  // ********** debugger control ************************************************************//

  // ********** debug result ************************************************************//

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
  // ********** debug result ************************************************************//

  // ********** breakpoint ************************************************************//

  /**
   * Get observation points.
   *
   * @return observation points
   */
  public ArrayList<Point> getPoints() {
    return new ArrayList<>(pm.getPoints());
  }
  // ********** breakpoint ************************************************************//
}
