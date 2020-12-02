/** */
package debug.value;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Information of an array value
 *
 * @author sugiyama
 */
public class ArrayInfo extends ObjectInfo {
  /**
   * @param stratum No. of the variable expansion
   * @param jValue jdi value
   */
  public ArrayInfo(int stratum, LocalDateTime createdAt, Value jValue) {
    super(stratum, createdAt, jValue);
  }

  /**
   * Create value info of array elements.
   *
   * @return children
   */
  @Override
  public ArrayList<ValueInfo> expand() {
    if (isExpanded) {
      return children;
    }
    var arrayRef = (ArrayReference) jValue;
    arrayRef
        .getValues()
        .forEach(
            val -> {
              ValueInfo vi = ValueInfoFactory.create(stratum + 1, val, "", createdAt);
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
