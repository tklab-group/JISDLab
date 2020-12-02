/** */
package debug.value;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Information of an object value
 *
 * @author sugiyama
 */
public class ObjectInfo extends ValueInfo {
  /** reference type */
  ReferenceType rt;

  /**
   * @param stratum No. of the variable expansion
   * @param jValue jdi value
   */
  public ObjectInfo(int stratum, LocalDateTime createdAt, Value jValue) {
    super(stratum, createdAt, jValue);
    rt = ((ObjectReference) jValue).referenceType();
  }

  /**
   * Create value info of fields
   *
   * @return children
   */
  @Override
  public ArrayList<ValueInfo> expand() {
    if (isExpanded) {
      return children;
    }
    var objectRef = (ObjectReference) jValue;
    objectRef
        .getValues(rt.fields())
        .forEach(
            (field, value) -> {
              ValueInfo vi = ValueInfoFactory.create(stratum + 1, value, "", createdAt);
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
