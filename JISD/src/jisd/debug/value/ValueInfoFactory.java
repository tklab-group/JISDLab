/** */
package jisd.debug.value;

import com.sun.jdi.Value;

import java.time.LocalDateTime;

/**
 * Creates ValueInfo.
 *
 * @author sugiyama
 */
public class ValueInfoFactory {
  /**
   * Create ValueInfo.
   *
   * @param name a variable name
   * @param stratum the current number of variable expansion strata
   * @param jValue a jdi value
   * @param value value's string expression
   * @param createdAt a time stamp
   */
  public static ValueInfo create(
      String name, int stratum, Value jValue, String value, LocalDateTime createdAt) {
    if (jValue == null) { // no jValue
      return new PrimitiveInfo(name, stratum, createdAt, value);
    }
    char sign = jValue.type().signature().charAt(0);
    switch (sign) {
      case '[':
        return new ArrayInfo(name, stratum, createdAt, jValue);
      case 'L':
        return new ObjectInfo(name, stratum, createdAt, jValue);
      default:
        return new PrimitiveInfo(name, stratum, createdAt, jValue);
    }
  }
}
