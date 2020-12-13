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
  /** varNames and maxRecordNoOfValue */
  @Getter HashMap<String, Integer> maxRecords = new HashMap<>();
  /** varNames and maxNoOfExpand */
  @Getter HashMap<String, Integer> maxExpands = new HashMap<>();

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

  public void setMaxRecordNoOfValue(String varName, int number) {
    if (number <= 0) {
      DebuggerInfo.printError("A max record number must be a non-negative integer(> 0).");
      return;
    }
    maxRecords.put(varName, number);
    Optional<DebugResult> dr = getResults(varName);
    if (dr.isPresent()) {
      dr.get().setMaxRecordNoOfValue(number);
    }
  }

  public void setMaxNoOfExpand(String varName, int number) {
    if (number < 0) {
      DebuggerInfo.printError(
          "A max number of the variable expansion must be a positive integer(>= 0).");
    }
    maxExpands.put(varName, number);
    Optional<DebugResult> dr = getResults(varName);
    if (dr.isPresent()) {
      dr.get().setMaxNoOfExpand(number);
    }
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
    drs.remove(varName);
    maxExpands.remove(varName);
    maxRecords.remove(varName);
    varNames.remove(varName);
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
