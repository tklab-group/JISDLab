/**
 * 
 */
package debug.value;

import java.util.ArrayList;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

/**
 * Information of an array value
 * 
 * @author sugiyama
 *
 */
public class ArrayInfo extends ObjectInfo {
  /**
   * @param number  timestamp
   * @param stratum No. of the variable expansion
   * @param jValue  jdi value
   */
  public ArrayInfo(long number, int stratum, Value jValue) {
    super(number, stratum, jValue);
  }

  /**
   * Create value info of array elements.
   * 
   * @return children
   */
  @Override
  public ArrayList<ValueInfo> expand() {
    if (isExpanded)
      return children;
    var arrayRef = (ArrayReference) jValue;
    arrayRef.getValues().forEach(val -> {
      ValueInfo vi = ValueInfoFactory.create(number, stratum + 1, val);
      children.add(vi);
    });
    isExpanded = true;
    return children;
  }

  /**
   * Get ReferenceType
   * 
   * @return reference type
   */
  @Override
  public ReferenceType getRT() {
    return rt;
  }

}
