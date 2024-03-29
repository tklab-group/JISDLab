package jisd.debug;

import com.sun.jdi.Value;
import jisd.debug.value.ValueInfo;
import jisd.debug.value.ValueInfoFactory;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Debug results which contains observed values.
 *
 * @author sugiyama
 */
public class DebugResult {
  /** Get the default max record number of values */
  @Getter static volatile int defaultMaxRecordNoOfValue = 100;
  /** Get the default max number of the variable expantion strata */
  @Getter static volatile int defaultMaxNoOfExpand = 1;
  /** Get the location this result belongs to */
  @Getter Location location;
  /** Observed values */
  ArrayDeque<ValueInfo> values = new ArrayDeque<>();
  /** Get the max record number of values */
  @Getter volatile int maxRecordNoOfValue;
  /** Get the max number of the variable expantion strata */
  @Getter volatile int maxNoOfExpand;

  /** Constructor */
  DebugResult(Location loc) {
    maxRecordNoOfValue = defaultMaxRecordNoOfValue;
    maxNoOfExpand = defaultMaxNoOfExpand;
    this.location = loc;
  }

  /** Set the default max number of the variable expantion strata */
  public static void setDefaultMaxNoOfExpand(int number) {
    if (number < 0) {
      DebuggerInfo.printError(
          "A max number of the variable expansion must be a positive integer(>= 0).");
    }
    defaultMaxNoOfExpand = number;
  }

  /** Set the default max record number of values */
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

  void addValues(List<ValueInfo> notRegesteredValues) {
    int regSize = values.size();
    if (regSize == 0) {
      notRegesteredValues.forEach(
          v -> {
            addValue(v);
          });
      return;
    }
    int regLastIndex = regSize - 1;
    var regLastTime = values.getLast().getCreatedAt();
    int notRegIndex = -1;
    for (int i = 0; i < notRegesteredValues.size(); i++) {
      if (notRegesteredValues.get(i).getCreatedAt().isEqual(regLastTime)) {
        notRegIndex = i + 1;
        break;
      }
    }
    if (notRegIndex == -1) {
      notRegIndex = 0;
    }
    for (int i = notRegIndex; i < notRegesteredValues.size(); i++) {
      addValue(notRegesteredValues.get(i));
    }
  }

  /** Add value to deque */
  ValueInfo addValue(Value jValue, LocalDateTime date) {
    ArrayDeque<ValueInfo> valueExpansionQue = new ArrayDeque<>();
    String name = location.getVarName();
    ValueInfo value = ValueInfoFactory.create(name, 0, jValue, "", date);
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
    return value;
  }

  /**
   * Get an observed value
   *
   * @return value
   */
  public ArrayList<ValueInfo> getValues() {
    return (ArrayList<ValueInfo>) values.stream().collect(Collectors.toList());
  }

  /** Get the max record number of values */
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
    if (values.size() == 0) {
      throw new RuntimeException("No value was set");
    }
    return values.getLast();
  }

  /**
   * Get the latest observed value (alias of getLatestValue())
   *
   * @return latest value
   */
  public ValueInfo lv() {
    return getLatestValue();
  }

  /** Set the max number of the variable expantion strata */
  public void setMaxNoOfExpand(int number) {
    if (number < 0) {
      DebuggerInfo.printError(
          "A max number of the variable expansion must be a positive integer(>= 0).");
    }
    maxNoOfExpand = number;
  }

  /** Generate the hash code. */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((location == null) ? 0 : location.hashCode());
    return result;
  }

  /** Check equality by location */
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
