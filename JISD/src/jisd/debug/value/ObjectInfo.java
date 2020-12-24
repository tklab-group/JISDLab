/** */
package jisd.debug.value;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Information of an object value.
 *
 * @author sugiyama
 */
public class ObjectInfo extends ValueInfo {
  /** reference type */
  ReferenceType rt;

  /** */
  public ObjectInfo(String name, int stratum, LocalDateTime createdAt, Value jValue) {
    super(name, stratum, createdAt, jValue);
    if (jValue != null) {
      rt = ((ObjectReference) jValue).referenceType();
    }
  }

  /**
   * Create value info of fields
   *
   * @return children
   */
  @Override
  public ArrayList<ValueInfo> expand() {
    if (jValue == null) {
      return children;
    }
    if (isExpanded) {
      return children;
    }
    try {
      var objectRef = (ObjectReference) jValue;
      objectRef
          .getValues(rt.fields())
          .forEach(
              (field, value) -> {
                ValueInfo vi =
                    ValueInfoFactory.create(field.name(), stratum + 1, value, "", createdAt);
                children.add(vi);
              });
    } catch (Exception e) {
      e.printStackTrace();
    }
    isExpanded = true;
    return children;
  }

  /**
   * Get ReferenceType
   *
   * @return reference type
   */
  public Optional<ReferenceType> getRT() {
    return Optional.ofNullable(rt);
  }
}
