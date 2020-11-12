/**
 * 
 */
package debug.value;

import com.sun.jdi.Value;

/**
 * Information of an primitive value
 * 
 * @author sugiyama
 *
 */
public class PrimitiveInfo extends ValueInfo {

  /**
   * @param number  timestamp
   * @param stratum No. of the variable expansion
   * @param jValue  jdi value
   */
  public PrimitiveInfo(long number, int stratum, Value jValue) {
    super(number, stratum, jValue);
    isExpanded = true;
  }

}
