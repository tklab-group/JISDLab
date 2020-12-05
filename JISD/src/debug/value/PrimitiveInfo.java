/** */
package debug.value;

import com.sun.jdi.Value;

import java.time.LocalDateTime;

/**
 * Information of an primitive value
 *
 * @author sugiyama
 */
public class PrimitiveInfo extends ValueInfo {

  /**
   * @param stratum No. of the variable expansion
   * @param jValue jdi value
   */
  public PrimitiveInfo(int stratum, LocalDateTime createdAt, Value jValue) {
    super(stratum, createdAt, jValue);
    isExpanded = true;
  }

  public PrimitiveInfo(int stratum, LocalDateTime createdAt, String value) {
    super(stratum, createdAt, value);
    isExpanded = true;
  }
}
