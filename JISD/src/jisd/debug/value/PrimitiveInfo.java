/** */
package jisd.debug.value;

import com.sun.jdi.Value;

import java.time.LocalDateTime;

/**
 * Information of an primitive value.
 *
 * @author sugiyama
 */
public class PrimitiveInfo extends ValueInfo {

  /** */
  public PrimitiveInfo(String name, int stratum, LocalDateTime createdAt, Value jValue) {
    super(name, stratum, createdAt, jValue);
    isExpanded = true;
  }

  public PrimitiveInfo(String name, int stratum, LocalDateTime createdAt, String value) {
    super(name, stratum, createdAt, value);
    isExpanded = true;
  }
}
