/** */
package jisd.debug.value;

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
