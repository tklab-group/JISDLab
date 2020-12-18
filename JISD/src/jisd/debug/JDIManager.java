/** */
package jisd.debug;

import com.sun.jdi.VMDisconnectedException;
import org.jdiscript.JDIScript;
import org.jdiscript.handlers.OnVMStart;

/** @author sugiyama */
class JDIManager extends VMManager {
  /** JDI */
  JDIScript j;
  /** A procedure before the debugger runs */
  OnVMStart start;

  /**
   * Constructor
   *
   * @param j JDI
   * @param start A procedure before the debugger runs
   */
  JDIManager(JDIScript j, OnVMStart start) {
    this.j = j;
    this.start = start;
  }

  JDIManager(JDIScript j) {
    this(j, (s) -> {});
  }

  void addStart(OnVMStart start) {
    this.start = start;
  }

  /** Run the debugger. */
  @Override
  public void run() {
    try {
      DebuggerInfo.print("Debugger started.");
      j.run(start);
    } catch (VMDisconnectedException e) {
      throw e;
    }
  }

  /** Shut down the debugger. */
  @Override
  void shutdown() {
    try {
      j.vm().exit(0);
      DebuggerInfo.print("Debugger exited.");
    } catch (VMDisconnectedException e) {
      throw e;
    }
  }

  public JDIScript getJDI() {
    return j;
  }

  @Override
  void prepareStart(PointManager bpm) {
    start =
        se -> {
          /* procedure when vm starts. */
          bpm.requestSetPoints(this);
        };
    bpm.init();
  }

  /**
   * List currently known classes
   *
   * @param className if className sets "", all classes are shown.
   */
  public void classes(String className) {
    j.vm().allClasses().stream()
        .filter(cls -> cls.name().contains(className))
        .forEach(
            cls -> {
              System.out.println(cls.name());
            });
  }

  /**
   * List a class's methods
   *
   * @param className class name
   */
  public void methods(String className) {
    j.vm()
        .classesByName(className)
        .forEach(
            cls -> {
              cls.allMethods()
                  .forEach(
                      methods -> {
                        java.lang.System.out.println(methods.name());
                      });
            });
  }

  /**
   * List a class's fields
   *
   * @param className class name
   */
  public void fields(String className) {
    j.vm()
        .classesByName(className)
        .forEach(
            cls -> {
              cls.allFields()
                  .forEach(
                      fields -> {
                        java.lang.System.out.println(fields.name());
                      });
            });
  }
}
