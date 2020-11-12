package debug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
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
  Thread vmThread;
  /** VM manager */
  VMManager vmManager;
  /** A procedure when vm started */
  OnVMStart start = s -> {};
  /** use attaching connector or not */
  boolean isRemoteDebug = false;
  /** uses ProbeJ ? */
  boolean usesProbeJ = false;
  /** attaching port */
  int port = 0;
  /** */
  String host;

  /**
   * Constructor
   * 
   * @param main    A target class file name
   * @param options A target class path setting
   * @param srcDir  source directory
   */
  public Debugger(String main, String options, String srcDir) {
    this(main, options, srcDir, false);
  }

  /**
   * Constructor
   * 
   * @param main    A target class file name
   * @param options A target class path setting
   */
  public Debugger(String main, String options) {
    this(main, options, false);
  }

  /**
   * Constructor
   * 
   * @param main   A target class file name
   * @param srcDir source directory
   * @param port   attaching port
   */
  public Debugger(String host, int port, String srcDir) {
    this(host, port, srcDir, false);
  }

  /**
   * Constructor
   * 
   * @param main A target class file name
   * @param port attaching port
   */
  public Debugger(int port) {
    this(port, false);
  }
  
  /**
   * Constructor
   * 
   * @param main A target class file name
   * @param port attaching port
   */
  public Debugger(String host, int port) {
    this(host, port, false);
  }
  
  /**
   * Constructor
   * 
   * @param main    A target class file name
   * @param options A target class path setting
   * @param srcDir  source directory
   */
  public Debugger(String main, String options, String srcDir, boolean usesProbeJ) {
    setMain(main);
    this.options = options;
    this.usesProbeJ = usesProbeJ;
    setSrcDir(srcDir);
    drm = new DebugResultManager();
    bpm = new BreakPointManager(drm);
    vmManager = VMManagerFactory.create(main, options, host, port, isRemoteDebug, usesProbeJ);
    vmManager.prepareStart(bpm);
  }

  /**
   * Constructor
   * 
   * @param main    A target class file name
   * @param options A target class path setting
   */
  public Debugger(String main, String options, boolean usesProbeJ) {
    this(main, options, ".", usesProbeJ);
  }

  /**
   * Constructor
   * 
   * @param main   A target class file name
   * @param srcDir source directory
   * @param port   attaching port
   */
  public Debugger(String host, int port, String srcDir, boolean usesProbeJ) {
    setSrcDir(srcDir);
    this.host = host;
    this.usesProbeJ = usesProbeJ;
    drm = new DebugResultManager();
    bpm = new BreakPointManager(drm);
    this.isRemoteDebug = true;
    setPort(port);
    vmManager = VMManagerFactory.create(main, options, host, port, isRemoteDebug, usesProbeJ);
    vmManager.prepareStart(bpm);
  }

  /**
   * Constructor
   * 
   * @param main A target class file name
   * @param port attaching port
   */
  public Debugger(int port, boolean usesProbeJ) {
    this("localhost", port, usesProbeJ);
  }
  
  /**
   * Constructor
   * 
   * @param main A target class file name
   * @param port attaching port
   */
  public Debugger(String host, int port, boolean usesProbeJ) {
    this(host, port, "", usesProbeJ);
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
  
  public void setPort(int port) {
    if (port < 1024 || port > 65535) {
      this.port = 8000;
      DebuggerInfo.printError(
          "This port is out of range. So, now port is set 8000. Please set the port bewtween 1024 ~ 65535.");
    } else {
      this.port = port;
    }
  }
  
  public void setMain(String main) {
    this.main = main;
  }
  
  public void setSrcDir(String srcDir) {
    this.srcDir = srcDir;
  }
  //********** debugger settings ************************************************************//

  //********** stop ************************************************************//
  /**
   * Set breakpoint with a line number.
   * 
   * @param lineNumber A line number in a target java file
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> stopAt(int lineNumber) {
    return stopAt(main, lineNumber);
  }

  /**
   * Set breakpoint with a line number and variable names.
   * 
   * @param lineNumber line number
   * @param varNames   variable names
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> stopAt(int lineNumber, ArrayList<String> varNames) {
    return stopAt(main, lineNumber, varNames);
  }

  /**
   * Set breakpoint with a line number and variable names.
   * 
   * @param className  class name
   * @param lineNumber line number
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> stopAt(String className, int lineNumber) {
    return stopAt(className, lineNumber, new ArrayList<String>());
  }

  /**
   * Set breakpoint with a line number and variable names.
   * 
   * @param className  class name
   * @param lineNumber line number
   * @param varNames   variable names
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> stopAt(String className, int lineNumber, ArrayList<String> varNames) {
    Optional<BreakPoint> bp = bpm.setBreakPoint(className, lineNumber, varNames, true, false);
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
  public Optional<BreakPoint> stopAt(String methodName) {
    return stopAt(main, methodName);
  }

  /**
   * Set breakpoint with a method name and variable names.
   * 
   * @param methodName method name
   * @param varNames   variable names
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> stopAt(String methodName, ArrayList<String> varNames) {
    return stopAt(main, methodName, varNames);
  }

  /**
   * Set breakpoint with a method name.
   * 
   * @param className  class name
   * @param methodName method name
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> stopAt(String className, String methodName) {
    return stopAt(className, methodName, new ArrayList<String>());
  }

  /**
   * Set breakpoint with a method name and variable names.
   * 
   * @param className  class name
   * @param methodName method name
   * @param varNames   variable names
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> stopAt(String className, String methodName, ArrayList<String> varNames) {
    Optional<BreakPoint> bp = bpm.setBreakPoint(className, methodName, varNames, true, false);
    if (bp.isPresent()) {
      bpm.requestSetBreakPoint(bp.get());
    }
    return bp;
  }
  //********** stopAta ************************************************************//
  
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
    return watch(className, lineNumber, varNames, usesProbeJ);
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
    return watch(className, methodName, varNames, usesProbeJ);
  }
  //********** watch ************************************************************//
  
  //********** watch or probe ************************************************************//
  /**
   * Set watchpoint with a line number.
   * 
   * @param lineNumber A line number in a target java file
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> watch(int lineNumber, boolean isProbe) {
    return watch(main, lineNumber, isProbe);
  }

  /**
   * Set watchpoint with a line number and variable names.
   * 
   * @param lineNumber line number
   * @param varNames   variable names
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> watch(int lineNumber, ArrayList<String> varNames, boolean isProbe) {
    return watch(main, lineNumber, varNames, isProbe);
  }

  /**
   * Set watchpoint with a line number.
   * 
   * @param className  class name
   * @param lineNumber line number
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> watch(String className, int lineNumber, boolean isProbe) {
    return watch(className, lineNumber, new ArrayList<String>(), isProbe);
  }
  
  public Optional<BreakPoint> watch(String className, int lineNumber, ArrayList<String> varNames, boolean isProbe) {
    Optional<BreakPoint> bpOpt = bpm.setBreakPoint(className, lineNumber, varNames, false, isProbe);
    if (bpOpt.isPresent()) {
      BreakPoint bp = bpOpt.get();
      if (bp instanceof ProbePoint) {
        bpm.requestSetProbePoint((ProbePoint) bp);
      } else if (bp instanceof BreakPoint) {
        bpm.requestSetBreakPoint(bp);
      } else {
        return Optional.empty();
      }
    }
    return bpOpt;
  }
  
  /**
   * Set watchpoint with a method name.
   * 
   * @param methodName A method name in a target java file
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> watch(String methodName, boolean isProbe) {
    return watch(main, methodName, isProbe);
  }

  /**
   * Set watchpoint with a method name and variable names.
   * 
   * @param methodName method name
   * @param varNames   variable names
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> watch(String methodName, ArrayList<String> varNames, boolean isProbe) {
    return watch(main, methodName, varNames, isProbe);
  }

  /**
   * Set watchpoint with a method name.
   * 
   * @param className  class name
   * @param methodName method name
   * @return breakpoint(optional)
   */
  public Optional<BreakPoint> watch(String className, String methodName, boolean isProbe) {
    return watch(className, methodName, new ArrayList<String>(), isProbe);
  }
  
  public Optional<BreakPoint> watch(String className, String methodName, ArrayList<String> varNames, boolean isProbe) {
    Optional<BreakPoint> bpOpt = bpm.setBreakPoint(className, methodName, varNames, false, isProbe);
    if (bpOpt.isPresent()) {
      BreakPoint bp = bpOpt.get();
      if (bp instanceof ProbePoint) {
        bpm.requestSetProbePoint((ProbePoint) bp);
      } else if (bp instanceof BreakPoint) {
        bpm.requestSetBreakPoint(bp);
      } else {
        return Optional.empty();
      }
    }
    return bpOpt;
  }
  //********** watch or probe ************************************************************//
  
  //********** on breakpoint ************************************************************//
  /**
   * Execute "step in"/"step into"
   */
  public void step() {
    bpm.requestStepInto();
    sleep();
  }
  
  /**
   * Execute "step over"
   */
  public void next() {
    bpm.requestStepOver();
    sleep();
  }
  
  /**
   * Execute "step out"/"step return"
   */
  public void finish() {
    bpm.requestStepOut();
    sleep();
  }
  
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
  
  /**
   * Print stacktrace in current stack frame.
   */
  public void where() {
    bpm.printStackTrace();
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
  
  /**
   * Sleep main thread until current bpm process is done
   * 
   */
  public void sleep() {
    try {
      while (bpm.isProcessing) {
        Thread.sleep(100);
      }
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
   * Shutdown the debugger.(alias of "exit")
   */
  public void quit() {
    exit();
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
    vmManager = VMManagerFactory.create(main, options, host, port, isRemoteDebug, usesProbeJ);
    vmManager.prepareStart(bpm);
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
  
  //********** external process ************************************************************//
  /**
   * Execute external command.
   * @param command command (wildcard * is unavailable)
   * @return [stdout, stderr, exit code] (optional)
   */
  public static Optional<String[]> exec(String command) {
    String lineSeparator = System.getProperty("line.separator");
    String[] results = new String[3];
    Arrays.fill(results, "");
    try {
      Process p = Runtime.getRuntime().exec(command);
      InputStream in = null;
      BufferedReader br = null;
      try {
        in = p.getInputStream();
        StringBuffer out = new StringBuffer();
        br = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = br.readLine()) != null) {
            out.append(line + lineSeparator);
        }
        results[0] = out.toString();
        br.close();
        in.close();
        in = p.getErrorStream();
        StringBuffer err = new StringBuffer();
        br = new BufferedReader(new InputStreamReader(in));
        while ((line = br.readLine()) != null) {
            err.append(line + lineSeparator);
        }
        results[1] = err.toString();
        results[2] = Integer.toString(p.waitFor());
        System.out.print(results[0]);
        System.err.print(results[1]);
      } finally {
        if (br != null) {
            br.close();
        }
        if (in != null) {
            in.close();
        }
      }
      p.destroy();
      return Optional.of(results);
    } catch (IOException e) {
      e.printStackTrace();
      return Optional.empty();
    } catch (InterruptedException e) {
      DebuggerInfo.print("Interrupted.");
      return Optional.empty();
    }
  }
  //********** external process ************************************************************//
}
