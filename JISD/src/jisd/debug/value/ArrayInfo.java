/** */
package jisd.debug.value;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Information of an array value.
 *
 * @author sugiyama
 */
public class ArrayInfo extends ObjectInfo {
  /** */
  public ArrayInfo(String name, int stratum, LocalDateTime createdAt, Value jValue) {
    super(name, stratum, createdAt, jValue);
  }

  /**
   * Create value info of array elements.
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
      var arrayRef = (ArrayReference) jValue;
      var values = arrayRef.getValues();
      for (int i = 0; i < values.size(); i++) {
        ValueInfo vi = ValueInfoFactory.create(i + "", stratum + 1, values.get(i), "", createdAt);
        children.add(vi);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    isExpanded = true;
    return children;
  }
}
