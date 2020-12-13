package debug.value;

import com.sun.jdi.Value;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Saved value information
 *
 * @author sugiyama
 */
public abstract class ValueInfo {
  /** time stamp */
  @Getter static volatile long count = 0;
  /** saved value */
  @Getter String value;
  /** value number */
  @Getter long number;
  /** time stamp */
  @Getter LocalDateTime createdAt;
  /** the current number of variable expansion strata */
  @Getter int stratum;
  /** value */
  @Getter Value jValue;
  /** children value info */
  ArrayList<ValueInfo> children = new ArrayList<>();
  /** already expanded? */
  @Getter boolean isExpanded = false;

  /**
   * Constructor
   *
   * @param stratum the current number of variable expansion strata
   * @param jValue jdi value
   */
  public ValueInfo(int stratum, LocalDateTime createdAt, Value jValue) {
    this.number = count++;
    this.stratum = stratum;
    if (jValue != null) {
      this.value = jValue.toString();
      this.jValue = jValue;
    }
    this.createdAt = createdAt;
  }

  public ValueInfo(int stratum, LocalDateTime createdAt, String value) {
    this.number = count++;
    this.stratum = stratum;
    this.value = value;
    this.createdAt = createdAt;
  }

  public static void resetNumber() {
    count = 0;
  }

  /**
   * Create children value info
   *
   * @return children value info
   */
  public ArrayList<ValueInfo> expand() {
    isExpanded = true;
    return children;
  }

  @Override
  public String toString() {
    return "ValueInfo [value=" + value + ", number=" + number + ", stratum=" + stratum + "]";
  }

  /**
   * Get children value info
   *
   * @return children
   */
  public ArrayList<ValueInfo> ch() {
    return expand();
  }
}
