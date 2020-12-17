package debug;

import debug.value.ValueInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public abstract class Point {
  /** class name */
  @Getter String className;
  /** line number */
  @Getter int lineNumber;
  /** method name */
  @Getter String methodName;
  /** varNames and debugresult */
  HashMap<String, DebugResult> drs = new HashMap<>();

  boolean isEnable = true;
  /** variable names */
  @Getter ArrayList<String> varNames;
  /** break or not at points */
  @Getter boolean isBreak;
  /** already request to set Breakpoint? */
  @Getter
  @Setter(AccessLevel.PACKAGE)
  boolean isRequested = false;

  /**
   * Constructor
   *
   * @param className class name
   * @param lineNumber line number
   */
  Point(String className, int lineNumber) {
    this(className, lineNumber, "", new ArrayList<String>(), false);
  }

  /**
   * Constructor
   *
   * @param className class name
   * @param methodName method name
   */
  Point(String className, String methodName) {
    this(className, 0, methodName, new ArrayList<String>(), false);
  }

  /**
   * Constructor
   *
   * @param className class name
   * @param lineNumber line number
   * @param methodName method name
   * @param varNames variable names
   * @param isBreak break or not at points
   */
  Point(
      String className,
      int lineNumber,
      String methodName,
      ArrayList<String> varNames,
      boolean isBreak) {
    this.className = className;
    this.lineNumber = lineNumber;
    this.methodName = methodName;
    varNames.remove("");
    this.varNames = varNames;
    this.isBreak = isBreak;
    varNames.forEach(
        varName -> {
          createDebugResult(varName);
        });
  }

  abstract void reset();

  /**
   * Get DebugResult a variable name matches.
   *
   * @param varName variable name
   * @return debug result
   */
  public Optional<DebugResult> getResults(String varName) {
    Optional<DebugResult> result = Optional.ofNullable(drs.get(varName));
    return result;
  }

  public HashMap<String, DebugResult> getResults() {
    return drs;
  }

  /**
   * Set DebugResult
   *
   * @param dr debugresult
   */
  void addDebugResult(String varName, DebugResult dr) {
    drs.put(varName, dr);
  }

  /** Clear DebugResult */
  public void clearDebugResults() {
    drs = new HashMap<>();
    ValueInfo.resetNumber();
  }

  /** Request VM to set a breakpoint */
  abstract void requestSetPoint(VMManager vmMgr, PointManager bpm);

  public abstract void enable();

  public abstract void disable();

  public void clear() {
    disable();
  }

  public abstract void add(String varName);

  public abstract void remove(String varName);

  void addVarName(String varName) {
    if (varNames.contains(varName)) {
      DebuggerInfo.print("This name is already registered.");
      return;
    }
    varNames.add(varName);
  }

  void removeVarName(String varName) {
    // for local
    drs.remove(varName);
    // for field
    drs.remove("this." + varName);
    // for local and field
    varNames.remove(varName);
  }

  void addValue(String varName, ValueInfo value) {
    synchronized (this) {
      Optional<DebugResult> res = Optional.ofNullable(drs.get(varName));
      if (res.isPresent()) {
        res.get().addValue(value);
        return;
      }
      createDebugResult(varName).addValue(value);
    }
  }

  DebugResult createDebugResult(String varName) {
    Location loc = new Location(className, methodName, lineNumber, varName);
    DebugResult dr = new DebugResult(loc);
    addDebugResult(varName, dr);
    return dr;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((className == null) ? 0 : className.hashCode());
    result = prime * result + lineNumber;
    result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Point other = (Point) obj;
    if (className == null) {
      if (other.className != null) {
        return false;
      }
    } else if (!className.equals(other.className)) {
      return false;
    }
    if (lineNumber != other.lineNumber) {
      return false;
    }
    if (methodName == null) {
      if (other.methodName != null) {
        return false;
      }
    } else if (!methodName.equals(other.methodName)) {
      return false;
    }
    return true;
  }
}
