package debug;

import com.sun.jdi.LocalVariable;
import com.sun.jdi.Value;
import debug.value.ValueInfo;
import debug.value.ValueInfoFactory;
import lombok.Getter;
import util.Stream;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Map;

/**
 * Debug result
 *
 * @author sugiyama
 */
public class DebugResult {
  /** the default max record number of values */
  @Getter static volatile int defaultMaxRecordNoOfValue = 100;
  /** the default max number of the variable expantion strata */
  @Getter static volatile int defaultMaxNoOfExpand = 1;
  /** class name */
  @Getter Location location;
  /** saved values */
  ArrayDeque<ValueInfo> values = new ArrayDeque<>();
  /** the max record number of values */
  @Getter int maxRecordNoOfValue;
  /** the max number of the variable expantion strata */
  @Getter int maxNoOfExpand;

  /** Constructor */
  DebugResult(Location loc) {
    maxRecordNoOfValue = defaultMaxRecordNoOfValue;
    maxNoOfExpand = defaultMaxNoOfExpand;
    this.location = loc;
  }

  public static void setDefaultMaxNoOfExpand(int number) {
    if (number < 0) {
      DebuggerInfo.printError(
          "A max number of the variable expansion must be a positive integer(>= 0).");
    }
    defaultMaxNoOfExpand = number;
  }

  public static void setDefaultMaxRecordNoOfValue(int number) {
    if (number <= 0) {
      DebuggerInfo.printError("A max record number must be a non-negative integer(> 0).");
      return;
    }
    defaultMaxRecordNoOfValue = number;
  }

  void addValue(ValueInfo value) {
    synchronized (this) {
      if (values.size() >= maxRecordNoOfValue) {
        values.pop();
        values.add(value);
        return;
      }
      values.add(value);
    }
  }

  /**
   * Add value to deque
   *
   * @param entry entry An observed variable and value
   */
  void addValue(Map.Entry<LocalVariable, Value> entry) {
    ArrayDeque<ValueInfo> valueExpansionQue = new ArrayDeque<>();
    ValueInfo value = ValueInfoFactory.create(0, entry.getValue(), "", LocalDateTime.now());
    valueExpansionQue.add(value);
    while (true) {
      ValueInfo v = valueExpansionQue.pop();
      if (v.getStratum() >= maxNoOfExpand) {
        break;
      }
      ArrayList<ValueInfo> childValues = v.expand();
      childValues.forEach(
          cv -> {
            valueExpansionQue.add(cv);
          });
      if (valueExpansionQue.isEmpty()) {
        break;
      }
    }
    addValue(value);
  }

  /**
   * Get an observed value
   *
   * @return value
   */
  public ArrayList<ValueInfo> getValues() {
    return (ArrayList<ValueInfo>) values.stream().collect(Stream.toArrayList());
  }

  public void setMaxRecordNoOfValue(int number) {
    if (number <= 0) {
      DebuggerInfo.printError("A max record number must be a non-negative integer(> 0).");
      return;
    }
    maxRecordNoOfValue = number;
  }

  /**
   * Get the latest observed value
   *
   * @return latest value
   */
  public ValueInfo getLatestValue() {
    return values.getLast();
  }

  public void setMaxNoOfExpand(int number) {
    if (number < 0) {
      DebuggerInfo.printError(
          "A max number of the variable expansion must be a positive integer(>= 0).");
    }
    maxNoOfExpand = number;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((location == null) ? 0 : location.hashCode());
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
    DebugResult other = (DebugResult) obj;
    if (location == null) {
      if (other.location != null) {
        return false;
      }
    } else if (!location.equals(other.location)) {
      return false;
    }
    return true;
  }
}
