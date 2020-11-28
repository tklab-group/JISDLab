package debug;

import lombok.Getter;
import lombok.Setter;
import org.jdiscript.requests.ChainingBreakpointRequest;

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
  HashMap<String, Integer> maxRecords = new HashMap<>();
  /** varNames and maxNoOfExpand */
  HashMap<String, Integer> maxExpands = new HashMap<>();

  Optional<ChainingBreakpointRequest> bpr = Optional.empty();
  boolean isEnable = true;
  /** variable names */
  @Getter ArrayList<String> varNames;
  /** break or not at points */
  @Getter boolean isBreak;
  /** already request to set Breakpoint? */
  @Getter @Setter boolean isRequested = false;

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
  public Optional<DebugResult> getResult(String varName) {
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
  public void addDebugResult(String varName, DebugResult dr) {
    drs.put(varName, dr);
  }

  /** Clear DebugResult */
  public void clearDebugResults() {
    drs = new HashMap<>();
    DebugResult.resetNumber();
  }

  /** Request VM to set a breakpoint */
  abstract void requestSetPoint(VMManager vmMgr, PointManager bpm);

  public void enable() {
    isEnable = true;
    if (bpr.isPresent()) {
      bpr.get().enable();
    }
  }

  public void disable() {
    isEnable = false;
    if (bpr.isPresent()) {
      bpr.get().disable();
    }
  }

  public void clear() {
    disable();
  }

  public void setMaxRecordNoOfValue(String varName, int number) {
    if (number <= 0) {
      DebuggerInfo.printError("A max record number must be a non-negative integer(> 0).");
      return;
    }
    maxRecords.put(varName, number);
    Optional<DebugResult> dr = getResult(varName);
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
    Optional<DebugResult> dr = getResult(varName);
    if (dr.isPresent()) {
      dr.get().setMaxNoOfExpand(number);
    }
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
    BreakPoint other = (BreakPoint) obj;
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
