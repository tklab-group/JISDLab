package debug;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Map;

import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Value;

import debug.value.ValueInfo;
import debug.value.ValueInfoFactory;

/**
 * Debug result
 * 
 * @author sugiyama
 *
 */
public class DebugResult {
  /** class name */
  String className;
  /** line number */
  int lineNumber;
  /** value name */
  String varName;
  /** time stamp */
  static long number = 0;
  /** saved values */
  ArrayDeque<ValueInfo> values = new ArrayDeque<>();
  /** the max record number of values */
  int maxRecordNoOfValue;
  /** the max number of the variable expantion strata */
  int maxNoOfExpand;
  /** the default max record number of values */
  static int defaultMaxRecordNoOfValue = 100;
  /** the default max number of the variable expantion strata */
  static int defaultMaxNoOfExpand = 1;
  /**
   * Constructor
   * 
   * @param maxRecordNoOfValue the max record number of values
   * @param maxNoOfExpand      the max number of the variable expantion strata
   * @param number             time stamp
   * @param className          class name
   * @param lineNumber         line number
   * @param varName            value name
   * @param loc                An observed location
   * @param entry              An observed variable and value
   */
  DebugResult(String className, int lineNumber, String varName) {
    maxRecordNoOfValue = defaultMaxRecordNoOfValue;
    maxNoOfExpand = defaultMaxNoOfExpand;
    this.className = className;
    this.lineNumber = lineNumber;
    this.varName = varName;
  }

  /**
   * Add value to deque
   * 
   * @param number time stamp
   * @param entry  entry An observed variable and value
   */
  void addValue(Map.Entry<LocalVariable, Value> entry) {
    ArrayDeque<ValueInfo> valueExpansionQue = new ArrayDeque<>();
    ValueInfo value = ValueInfoFactory.create(number++, 0, entry.getValue());
    valueExpansionQue.add(value);
    while (true) {
      ValueInfo v = valueExpansionQue.pop();
      if (v.getStratum() >= maxNoOfExpand) {
        break;
      }
      ArrayList<ValueInfo> childValues = v.expand();
      childValues.forEach(cv -> {
        valueExpansionQue.add(cv);
      });
      if (valueExpansionQue.isEmpty()) {
        break;
      }
    }
    synchronized (this) {
      if (values.size() >= maxRecordNoOfValue) {
        values.pop();
        values.add(value);
        return;
      }
      values.add(value);
    }
  }
  
  static void resetNumber() {
    number = 0;
  }

  /**
   * Get a line number of an observed location in a target java file.
   * 
   * @return line number
   */
  public int getLineNumber() {
    return lineNumber;
  }

  /**
   * Get a name of an observed variable.
   * 
   * @return variable name
   */
  public String getName() {
    return varName;
  }

  /**
   * Get an observed value
   * 
   * @return value
   */
  public ValueInfo[] getValues() {
    return values.toArray(ValueInfo[]::new);
  }

  /**
   * Get the latest observed value
   * 
   * @return latest value
   */
  public ValueInfo getLatestValue() {
    return values.getLast();
  }
  
  public void setMaxRecordNoOfValue(int number) {
    if (number <= 0) {
      DebuggerInfo.printError("A max record number must be a non-negative integer(> 0).");
      return;
    }
    maxRecordNoOfValue = number;
  }
  
  public void setMaxNoOfExpand(int number) {
    if (number < 0) {
      DebuggerInfo.printError("A max number of the variable expansion must be a positive integer(>= 0).");
    }
    maxNoOfExpand =number;
  }
  
  public static void setDefaultMaxRecordNoOfValue(int number) {
    if (number <= 0) {
      DebuggerInfo.printError("A max record number must be a non-negative integer(> 0).");
      return;
    }
    defaultMaxRecordNoOfValue = number;
  }
  
  public static void setDefaultMaxNoOfExpand(int number) {
    if (number < 0) {
      DebuggerInfo.printError("A max number of the variable expansion must be a positive integer(>= 0).");
    }
    defaultMaxNoOfExpand =number;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((className == null) ? 0 : className.hashCode());
    result = prime * result + lineNumber;
    result = prime * result + ((varName == null) ? 0 : varName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DebugResult other = (DebugResult) obj;
    if (className == null) {
      if (other.className != null)
        return false;
    } else if (!className.equals(other.className))
      return false;
    if (lineNumber != other.lineNumber)
      return false;
    if (varName == null) {
      if (other.varName != null)
        return false;
    } else if (!varName.equals(other.varName))
      return false;
    return true;
  }

}
