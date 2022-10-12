package jisd.debug.value;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Observed value information.
 *
 * @author sugiyama
 */
public abstract class ValueInfo {
  /** Get the ValueInfo count */
  @Getter static volatile long count = 0;
  /** Get a variable name */
  @Getter String name;
  /** Get an observed value */
  @Getter String value;
  /** Get a value number */
  @Getter long number;
  /** Get a time stamp */
  @Getter LocalDateTime createdAt;
  /** Get the current number of variable expansion strata */
  @Getter int stratum;
  /** Get a jdi value */
  @Getter Value jValue;
  /** reference type */
  @Getter ReferenceType rt;
  /** value info children */
  ArrayList<ValueInfo> children = new ArrayList<>();
  /** Get already expanded? */
  @Getter boolean isExpanded = false;

  /**
   * Constructor
   *
   * @param name a variable name
   * @param stratum the current number of variable expansion strata
   * @param createdAt a time stamp
   * @param jValue a jdi value
   */
  public ValueInfo(String name, int stratum, LocalDateTime createdAt, Value jValue) {
    this.number = count++;
    this.name = name;
    this.stratum = stratum;
    if (jValue != null) {
      this.value = jValue.toString();
      this.jValue = jValue;
    }
    this.createdAt = createdAt;
  }

  /**
   * Constructor
   *
   * @param name a variable name
   * @param stratum the current number of variable expansion strata
   * @param createdAt a time stamp
   * @param value value's string expression
   */
  public ValueInfo(String name, int stratum, LocalDateTime createdAt, String value) {
    this.number = count++;
    this.name = name;
    this.stratum = stratum;
    this.value = value;
    this.createdAt = createdAt;
  }

  /** Reset the ValueInfo count. */
  public static void resetNumber() {
    count = 0;
  }

  /**
   * Create value info children
   *
   * @return values
   */
  public ArrayList<ValueInfo> expand() {
    if (jValue == null) {
      return children;
    }
    if (isExpanded) {
      return children;
    }
    try {
      var objectRef = (ObjectReference) jValue;
      objectRef
          .getValues(rt.fields())
          .forEach(
              (field, value) -> {
                ValueInfo vi =
                    ValueInfoFactory.create(field.name(), stratum + 1, value, "", createdAt);
                children.add(vi);
              });
    } catch (Exception e) {
      e.printStackTrace();
    }
    isExpanded = true;
    return children;
  }

  public ValueInfo expand(String name) {
    expand();
    return getField(name);
  }

  public ValueInfo getField(String name) {
    var child = children.stream().filter(ch -> ch.getName().equals(name)).findFirst();
    return (child.isPresent()) ? child.get() : this;
  }

  @Override
  public String toString() {
    return name + "=" + value;
  }

  /**
   * Get value info children
   *
   * @return children
   */
  public ArrayList<ValueInfo> ch() {
    return children;
  }
}
