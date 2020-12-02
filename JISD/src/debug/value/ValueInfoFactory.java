/** */
package debug.value;

import com.sun.jdi.Value;

import java.time.LocalDateTime;

/**
 * ValueInfo simple factory
 *
 * @author sugiyama
 */
public class ValueInfoFactory {
  /**
   * create ValueInfo
   *
   * @param stratum No. of the variable expansion
   * @param jValue jdi value
   * @return value info
   */
  public static ValueInfo create(int stratum, Value jValue, String value, LocalDateTime createdAt) {
    if (jValue == null) { // no jValue
      return new PrimitiveInfo(stratum, createdAt, value);
    }
    char sign = jValue.type().signature().charAt(0);
    switch (sign) {
      case '[':
        return new ArrayInfo(stratum, createdAt, jValue);
      case 'L':
        return new ObjectInfo(stratum, createdAt, jValue);
      default:
        return new PrimitiveInfo(stratum, createdAt, jValue);
    }
  }
}
