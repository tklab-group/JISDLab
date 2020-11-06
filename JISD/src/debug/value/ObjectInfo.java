/**
 * 
 */
package debug.value;

import java.util.ArrayList;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

/**
 * Information of an object value
 * 
 * @author sugiyama
 *
 */
public class ObjectInfo extends ValueInfo {
  /** reference type */
  ReferenceType rt;

  /**
   * @param number  timestamp
   * @param stratum No. of the variable expansion
   * @param jValue  jdi value
   */
  public ObjectInfo(long number, int stratum, Value jValue) {
    super(number, stratum, jValue);
    rt = ((ObjectReference) jValue).referenceType();
  }

  /**
   * Create value info of fields
   * 
   * @return children
   */
  @Override
  public ArrayList<ValueInfo> expand() {
    if (isExpanded)
      return children;
    var objectRef = (ObjectReference) jValue;
    objectRef.getValues(rt.fields()).forEach((field, value) -> {
      ValueInfo vi = ValueInfoFactory.create(number, stratum + 1, value);
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
  public ReferenceType getRT() {
    return rt;
  }
}
