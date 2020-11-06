package debug;

import java.util.ArrayList;
import java.util.Optional;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnVMStart;
import org.jdiscript.util.VMLauncher;
import org.jdiscript.util.VMSocketAttacher;

import com.sun.jdi.VirtualMachine;

/**
 * JISDLab's main debugger
 * 
 * @author sugiyama
 *
 */
public class Debugger {
  /** JDI */
  JDIScript j;
  /** Manage breakpoints */
  BreakPointManager bpm;
  /** Manage debug results */
  DebugResultManager drm;
  /** Target class setting items */
  String main, options, srcDir;
  /** JDI thread */
  Thread jdiThread;
  /** VM manager */
  VMManager vmManager;
  /** A procedure when vm started */
  OnVMStart start = s -> {
  };
  /** use attaching connector or not */
  boolean isRemoteDebug = false;
  /** attaching port */
  int port = 0;

  /**
   * Constructor
   * 
   * @param main    A target class file name
   * @param options A target class path setting
   * @param srcDir  source directory
   */
  public Debugger(String main, String options, String srcDir) {
    this.main = main;
    this.options = options;
    this.srcDir = srcDir;
    drm = new DebugResultManager();
    bpm = new BreakPointManager(drm);
    vmInit();
  }

  /**
   * Constructor
   * 
   * @param main    A target class file name
   * @param options A target class path setting
   */
  public Debugger(String main, String options) {
    this(main, options, ".");
  }

  /**
   * Constructor
   * 
   * @param main   A target class file name
   * @param srcDir source directory
   * @param port   attaching port
   */
  public Debugger(String main, String srcDir, int port) {
    this(main, "", srcDir);
    this.isRemoteDebug = true;
    if (port < 1024 || port > 65535) {
      this.port = 8000;
      DebuggerInfo.printError(
          "This port is out of range. So, now port is set 8000. Please set the port bewtween 1024 ~ 65535.");
    } else {
      this.port = port;
    }
  }

  /**
   * Constructor
   * 
   * @param main A target class file name
   * @param port attaching port
   */
  public Debugger(String main, int port) {
    this(main, "", port);
  }

  //********** debugger settings ************************************************************//
  /**
   * Set the max record number of values
   * 
   * @param number the max record number of values
   */
  public void setMaxRecordNumber(int number) {
    drm.setMaxRecordNoOfValue(number);
  }

  /**
   * Set the max number of the variable expantion strata
   * 
   * @param number the max number of the variable expantion strata
   */
  public void setMaxNoOfExpand(int number) {
    drm.setMaxNoOfExpand(number);
  }
  //********** debugger settings ************************************************************//

  //********** stop ************************************************************//
  /**
   * Set breakpoint with a line number.
   * 
   * @param lineNumber A line number in a target java file
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> stop(int lineNumber) {
    return stop(main, lineNumber);
  }

  /**
   * Set breakpoint with a line number and variable names.
   * 
   * @param lineNumber line number
   * @param varNames   variable names
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> stop(int lineNumber, ArrayList<String> varNames) {
    return stop(main, lineNumber, varNames);
  }

  /**
   * Set breakpoint with a line number and variable names.
   * 
   * @param className  class name
   * @param lineNumber line number
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> stop(String className, int lineNumber) {
    return stop(className, lineNumber, new ArrayList<String>());
  }

  /**
   * Set breakpoint with a line number and variable names.
   * 
   * @param className  class name
   * @param lineNumber line number
   * @param varNames   variable names
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> stop(String className, int lineNumber, ArrayList<String> varNames) {
    Optional<BreakPoint> bp = bpm.setBreakPoint(className, lineNumber, varNames, true);
    if (bp.isPresent()) {
      bpm.requestSetBreakPoint(bp.get());
    }
    return bp;
  }

  /**
   * Set breakpoint with a method name.
   * 
   * @param methodName A method name in a target java file
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> stop(String methodName) {
    return stop(main, methodName);
  }

  /**
   * Set breakpoint with a method name and variable names.
   * 
   * @param methodName method name
   * @param varNames   variable names
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> stop(String methodName, ArrayList<String> varNames) {
    return stop(main, methodName, varNames);
  }

  /**
   * Set breakpoint with a method name.
   * 
   * @param className  class name
   * @param methodName method name
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> stop(String className, String methodName) {
    return stop(className, methodName, new ArrayList<String>());
  }

  /**
   * Set breakpoint with a method name and variable names.
   * 
   * @param className  class name
   * @param methodName method name
   * @param varNames   variable names
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> stop(String className, String methodName, ArrayList<String> varNames) {
    Optional<BreakPoint> bp = bpm.setBreakPoint(className, methodName, varNames, true);
    if (bp.isPresent()) {
      bpm.requestSetBreakPoint(bp.get());
    }
    return bp;
  }
  //********** stop ************************************************************//
  
  //********** watch ************************************************************//
  /**
   * Set watchpoint with a line number.
   * 
   * @param lineNumber A line number in a target java file
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> watch(int lineNumber) {
    return watch(main, lineNumber);
  }

  /**
   * Set watchpoint with a line number and variable names.
   * 
   * @param lineNumber line number
   * @param varNames   variable names
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> watch(int lineNumber, ArrayList<String> varNames) {
    return watch(main, lineNumber, varNames);
  }

  /**
   * Set watchpoint with a line number.
   * 
   * @param className  class name
   * @param lineNumber line number
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> watch(String className, int lineNumber) {
    return watch(className, lineNumber, new ArrayList<String>());
  }

  /**
   * Set watchpoint with a line number and variable names.
   * 
   * @param className  class name
   * @param lineNumber line number
   * @param varNames   variable names
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> watch(String className, int lineNumber, ArrayList<String> varNames) {
    Optional<BreakPoint> bp = bpm.setBreakPoint(className, lineNumber, varNames, false);
    if (bp.isPresent()) {
      bpm.requestSetBreakPoint(bp.get());
    }
    return bp;
  }

  /**
   * Set watchpoint with a method name.
   * 
   * @param methodName A method name in a target java file
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> watch(String methodName) {
    return watch(main, methodName);
  }

  /**
   * Set watchpoint with a method name and variable names.
   * 
   * @param methodName method name
   * @param varNames   variable names
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> watch(String methodName, ArrayList<String> varNames) {
    return watch(main, methodName, varNames);
  }

  /**
   * Set watchpoint with a method name.
   * 
   * @param className  class name
   * @param methodName method name
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> watch(String className, String methodName) {
    return watch(className, methodName, new ArrayList<String>());
  }

  /**
   * Set watchpoint with a method name and variable names.
   * 
   * @param className  class name
   * @param methodName method name
   * @param varNames   variable names
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> watch(String className, String methodName, ArrayList<String> varNames) {
    Optional<BreakPoint> bp = bpm.setBreakPoint(className, methodName, varNames, false);
    if (bp.isPresent()) {
      bpm.requestSetBreakPoint(bp.get());
    }
    return bp;
  }
  //********** watch ************************************************************//
  
  //********** on breakpoint ************************************************************//
  /** Continue execution from breakpoint */
  public void cont() {
    bpm.resumeThread();
  }

  /**
   * Print source code
   */
  public void list() {
    bpm.printSrcAtCurrentLocation("Current location,", srcDir);
  }

  /**
   * List currently known classes
   * 
   * @param className if className sets "", all classes are shown.
   */
  public void classes(String className) {
    j.vm().allClasses().stream().filter(cls -> cls.name().contains(className)).forEach(cls -> {
      System.out.println(cls.name());
    });
  }

  /**
   * List a class's methods
   * 
   * @param className class name
   */
  public void methods(String className) {
    j.vm().classesByName(className).forEach(cls -> {
      cls.allMethods().forEach(methods -> {
        System.out.println(methods.name());
      });
    });
  }

  /**
   * List a class's fields
   * 
   * @param className class name
   */
  public void fields(String className) {
    j.vm().classesByName(className).forEach(cls -> {
      cls.allFields().forEach(fields -> {
        System.out.println(fields.name());
      });
    });
  }

  /**
   * Print all local variables in current stack frame
   */
  public void locals() {
    bpm.printLocals();
  }
  //********** on breakpoint ************************************************************//

  //********** remove breakpoint ************************************************************//
  /**
   * Remove breakpoint with a line number.
   * 
   * @param lineNumber A line number in a target java file
   */
  public void clear(int lineNumber) {
    bpm.removeBreakPoint(main, lineNumber);
  }

  /**
   * Remove breakpoint with a line number.
   * 
   * @param className  class name
   * @param lineNumber line number
   */
  public void clear(String className, int lineNumber) {
    bpm.removeBreakPoint(className, lineNumber);
  }

  /**
   * Remove breakpoint with a method name.
   * 
   * @param methodName A method name in a target java file
   */
  public void clear(String methodName) {
    bpm.removeBreakPoint(main, methodName);
  }

  /**
   * Remove breakpoint with a method name.
   * 
   * @param className  class name
   * @param methodName method name
   */
  public void clear(String className, String methodName) {
    bpm.removeBreakPoint(className, methodName);
  }
  //********** remove breakpoint ************************************************************//

  //********** debugger control ************************************************************//
  /**
   * Set some settings(breakpoints) before the debugger runs.
   * 
   * @return A procedure before the debugger runs
   */
  OnVMStart prepareStart() {
    start = se -> {
      /* procedure when vm starts. */
      bpm.requestSetBreakPoints();
    };
    return start;
  }

  /**
   * Initialize VM before running
   */
  void vmInit() {
    VirtualMachine vm;
    if (isRemoteDebug) {
      vm = new VMSocketAttacher(port).attach();
    } else {
      vm = new VMLauncher(options, main).start();
    }
    j = new JDIScript(vm);
    bpm.setJDI(j);
  }

  /**
   * Start up the debugger.
   * 
   * @param sleepTime Wait time after the debugger starts running
   */
  public void run(int sleepTime) {
    vmManager = new VMManager(j, start);
    jdiThread = new Thread(vmManager);
    jdiThread.start();
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

  /**
   * Shutdown the debugger.
   */
  public void exit() {
    vmManager.shutdown();
  }

  /**
   * Clear debug results all.
   */
  public void clearResults() {
    drm.clearResults();
    ArrayList<BreakPoint> bps = getBreakPoints();
    bps.forEach(bp -> {
      bp.clearDebugResults();
      bp.setRequestState(false);
    });
  }

  /**
   * Restart the debugger.
   * 
   * @param sleepTime Wait time after the debugger starts running
   */
  public void restart(int sleepTime) {
    exit();
    clearResults();
    vmInit();
    prepareStart();
    run(sleepTime);
  }
  //********** debugger control ************************************************************//

  //********** debug result ************************************************************//
  /**
   * Get debug results.
   * 
   * @return Debug results
   */
  public ArrayList<DebugResult> getResults() {
    return drm.getResults();
  }

  /**
   * Get debug results a variable name matches.
   * 
   * @param varName variable name
   * @return Debug results
   */
  public ArrayList<DebugResult> getResults(String varName) {
    return drm.getResults(varName);
  }
  //********** debug result ************************************************************//

  //********** breakpoint ************************************************************//
  /**
   * Get breakpoints.
   * 
   * @return breakpoints
   */
  public ArrayList<BreakPoint> getBreakPoints() {
    return new ArrayList<>(bpm.getBreakPoints());
  }
  //********** breakpoint ************************************************************//
}
