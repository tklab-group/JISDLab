package debug;

import _static.StaticFile;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
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
  @Getter @Setter String main, options, srcDir, binDir;
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
  /** static data * */
  StaticFile staticFile;

  /**
   * Constructor
   *
   * @param main A target class file name
   * @param options A target class path setting
   * @param srcDir source directory
   */
  public Debugger(String main, String options, String srcDir) {
    this(main, options, srcDir, false);
  }

  public Debugger(String main, String options, String srcDir, String binDir) {
    this(main, options, srcDir, binDir, false);
  }

  /**
   * Constructor
   *
   * @param main A target class file name
   * @param options A target class path setting
   */
  public Debugger(String main, String options) {
    this(main, options, false);
  }

  /**
   * Constructor
   *
   * @param srcDir source directory
   * @param port attaching port
   */
  public Debugger(String host, int port, String srcDir) {
    this(host, port, srcDir, false);
  }

  /**
   * Constructor
   *
   * @param port attaching port
   */
  public Debugger(int port) {
    this(port, false);
  }

  /**
   * Constructor
   *
   * @param port attaching port
   */
  public Debugger(String host, int port) {
    this(host, port, false);
  }

  /**
   * Constructor
   *
   * @param main A target class file name
   * @param options A target class path setting
   * @param srcDir source directory
   */
  public Debugger(String main, String options, String srcDir, boolean usesProbeJ) {
    init(main, options, "localhost", 39876, srcDir, "", false, usesProbeJ);
  }

  public Debugger(String main, String options, String srcDir, String binDir, boolean usesProbeJ) {
    init(main, options, "localhost", 39876, srcDir, binDir, false, usesProbeJ);
  }

  /**
   * Constructor
   *
   * @param main A target class file name
   * @param options A target class path setting
   */
  public Debugger(String main, String options, boolean usesProbeJ) {
    this(main, options, ".", usesProbeJ);
  }

  /**
   * Constructor
   *
   * @param srcDir source directory
   * @param port attaching port
   */
  public Debugger(String host, int port, String srcDir, boolean usesProbeJ) {
    init("", "", host, port, srcDir, "", true, usesProbeJ);
  }

  public Debugger(String host, int port, String srcDir, String binDir, boolean usesProbeJ) {
    init("", "", host, port, srcDir, binDir, true, usesProbeJ);
  }

  /**
   * Constructor
   *
   * @param port attaching port
   */
  public Debugger(int port, boolean usesProbeJ) {
    this("localhost", port, usesProbeJ);
  }

  /**
   * Constructor
   *
   * @param port attaching port
   */
  public Debugger(String host, int port, boolean usesProbeJ) {
    this(host, port, ".", usesProbeJ);
  }

  void init(
      String main,
      String options,
      String host,
      int port,
      String srcDir,
      String binDir,
      boolean isRemoteDebug,
      boolean usesProbeJ) {
    setMain(main);
    setOptions(options);
    setSrcDir(srcDir);
    setHost(host);
    setPort(port);
    this.usesProbeJ = usesProbeJ;
    setSrcDir(srcDir);
    setBinDir(binDir);
    this.isRemoteDebug = isRemoteDebug;
    pm = new PointManager();
    staticFile = new StaticFile(srcDir, binDir);
    vmManager = VMManagerFactory.create(main, options, host, port, isRemoteDebug, usesProbeJ);
    vmManager.prepareStart(pm);
  }

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

  // ********** debugger settings ************************************************************//

  // ********** stop ************************************************************//

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
  public Optional<Point> stopAt(int lineNumber, ArrayList<String> varNames) {
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
    return stopAt(className, lineNumber, new ArrayList<String>());
  }

  /**
   * Set breakpoint with a line number and variable names.
   *
   * @param className class name
   * @param lineNumber line number
   * @param varNames variable names
   * @return breakpoint(optional)
   */
  public Optional<Point> stopAt(String className, int lineNumber, ArrayList<String> varNames) {
    if (vmManager instanceof ProbeJManager) {
      return Optional.empty();
    }
    return pm.setPoint(vmManager, className, lineNumber, varNames, true, false);
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
  public Optional<Point> stopAt(String methodName, ArrayList<String> varNames) {
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
    return stopAt(className, methodName, new ArrayList<String>());
  }

  /**
   * Set breakpoint with a method name and variable names.
   *
   * @param className class name
   * @param methodName method name
   * @param varNames variable names
   * @return breakpoint(optional)
   */
  public Optional<Point> stopAt(String className, String methodName, ArrayList<String> varNames) {
    if (vmManager instanceof ProbeJManager) {
      return Optional.empty();
    }
    return pm.setPoint(vmManager, className, methodName, varNames, true, false);
  }
  // ********** stopAt ************************************************************//

  // ********** watch ************************************************************//

  /**
   * Set watchpoint with a line number.
   *
   * @param lineNumber A line number in a target java file
   * @return breakpoint(optional)
   */
  public Optional<Point> watch(int lineNumber) {
    return watch(main, lineNumber);
  }

  /**
   * Set watchpoint with a line number and variable names.
   *
   * @param lineNumber line number
   * @param varNames variable names
   * @return breakpoint(optional)
   */
  public Optional<Point> watch(int lineNumber, ArrayList<String> varNames) {
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
    return watch(className, lineNumber, new ArrayList<String>());
  }

  /**
   * Set watchpoint with a line number and variable names.
   *
   * @param className class name
   * @param lineNumber line number
   * @param varNames variable names
   * @return breakpoint(optional)
   */
  public Optional<Point> watch(String className, int lineNumber, ArrayList<String> varNames) {
    return watch(className, lineNumber, varNames, usesProbeJ);
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
  public Optional<Point> watch(String methodName, ArrayList<String> varNames) {
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
    return watch(className, methodName, new ArrayList<String>());
  }

  /**
   * Set watchpoint with a method name and variable names.
   *
   * @param className class name
   * @param methodName method name
   * @param varNames variable names
   * @return breakpoint(optional)
   */
  public Optional<Point> watch(String className, String methodName, ArrayList<String> varNames) {
    return watch(className, methodName, varNames, usesProbeJ);
  }

  // ********** watch ************************************************************//

  // ********** watch or probe ************************************************************//

  /**
   * Set watchpoint with a line number.
   *
   * @param lineNumber A line number in a target java file
   * @return breakpoint(optional)
   */
  public Optional<Point> watch(int lineNumber, boolean isProbe) {
    return watch(main, lineNumber, isProbe);
  }

  /**
   * Set watchpoint with a line number and variable names.
   *
   * @param lineNumber line number
   * @param varNames variable names
   * @return breakpoint(optional)
   */
  public Optional<Point> watch(int lineNumber, ArrayList<String> varNames, boolean isProbe) {
    return watch(main, lineNumber, varNames, isProbe);
  }

  /**
   * Set watchpoint with a line number.
   *
   * @param className class name
   * @param lineNumber line number
   * @return breakpoint(optional)
   */
  public Optional<Point> watch(String className, int lineNumber, boolean isProbe) {
    return watch(className, lineNumber, new ArrayList<String>(), isProbe);
  }

  public Optional<Point> watch(
      String className, int lineNumber, ArrayList<String> varNames, boolean isProbe) {
    return pm.setPoint(vmManager, className, lineNumber, varNames, false, isProbe);
  }

  /**
   * Set watchpoint with a method name.
   *
   * @param methodName A method name in a target java file
   * @return breakpoint(optional)
   */
  public Optional<Point> watch(String methodName, boolean isProbe) {
    return watch(main, methodName, isProbe);
  }

  /**
   * Set watchpoint with a method name and variable names.
   *
   * @param methodName method name
   * @param varNames variable names
   * @return breakpoint(optional)
   */
  public Optional<Point> watch(String methodName, ArrayList<String> varNames, boolean isProbe) {
    return watch(main, methodName, varNames, isProbe);
  }

  /**
   * Set watchpoint with a method name.
   *
   * @param className class name
   * @param methodName method name
   * @return breakpoint(optional)
   */
  public Optional<Point> watch(String className, String methodName, boolean isProbe) {
    return watch(className, methodName, new ArrayList<String>(), isProbe);
  }

  public Optional<Point> watch(
      String className, String methodName, ArrayList<String> varNames, boolean isProbe) {
    return pm.setPoint(vmManager, className, methodName, varNames, false, isProbe);
  }

  // ********** watch or probe ************************************************************//

  // ********** on breakpoint ************************************************************//

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
  }

  /** Print source code */
  public void list() {
    pm.printSrcAtCurrentLocation("Current location,", srcDir);
  }

  /** Print all local variables in current stack frame */
  public void locals() {
    pm.printLocals();
  }
  // ********** on breakpoint ************************************************************//

  // ********** remove breakpoint ************************************************************//

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

  /**
   * Remove breakpoint with a method name.
   *
   * @param className class name
   * @param methodName method name
   */
  public void clear(String className, String methodName) {
    pm.removePoint(className, methodName);
  }

  // ********** remove breakpoint ************************************************************//

  // ********** debugger control ************************************************************//

  /**
   * Start up the debugger.
   *
   * @param sleepTime Wait time after the debugger starts running
   */
  public void run(int sleepTime) {
    vmThread = new Thread(vmManager);
    vmThread.start();
    sleep(sleepTime);
  }

  /**
   * Sleep main thread
   *
   * @param sleepTime wait time
   */
  public void sleep(int sleepTime) {
    try {
      Thread.sleep(sleepTime);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /** Sleep main thread until current bpm process is done */
  void sleep() {
    try {
      while (pm.isProcessing) {
        Thread.sleep(100);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /** Shutdown the debugger. */
  public void exit() {
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
    staticFile.init(srcDir, binDir);
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
   * Get breakpoints.
   *
   * @return breakpoints
   */
  public ArrayList<Point> getPoints() {
    return new ArrayList<>(pm.getPoints());
  }
  // ********** breakpoint ************************************************************//
}
