/**
 * 
 */
package debug;

import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnVMStart;

import com.sun.jdi.VMDisconnectedException;

/**
 * @author sugiyama
 *
 */
class JDIManager extends VMManager {
  /** JDI */
  JDIScript j;
  /** A procedure before the debugger runs */
  OnVMStart start;

  /**
   * Constructor
   * 
   * @param j     JDI
   * @param start A procedure before the debugger runs
   */
  JDIManager(JDIScript j, OnVMStart start) {
    this.j = j;
    this.start = start;
  }
  
  JDIManager(JDIScript j) {
    this(j, (s)->{});
  }
  
  void addStart(OnVMStart start) {
    this.start = start;
  }

  /**
   * Run the debugger.
   */
  @Override
  public void run() {
    DebuggerInfo.print("VM started.");
    try {
      j.run(start);
    } catch (VMDisconnectedException e) {
      /* Do nothing */
    }
  }

  /**
   * Shut down the debugger.
   */
  void shutdown() {
    try {
      j.vm().exit(0);
      DebuggerInfo.print("VM exited.");
    } catch (VMDisconnectedException e) {
      DebuggerInfo.print("VM already exited.");
    }
  }
  
  public JDIScript getJDI() {
    return j;
  }
  
  @Override
  void prepareStart(BreakPointManager bpm) {
    start = se -> {
      /* procedure when vm starts. */
      bpm.requestSetBreakPoints(this);
    };
    bpm.init();
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
}
