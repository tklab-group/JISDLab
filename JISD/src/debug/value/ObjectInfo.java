/**
 * 
 */
package debug.value;

import java.util.ArrayList;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

/**
 * @author sugiyama
 *
 */
public class ObjectInfo extends ValueInfo {
	/** reference type */
	ReferenceType rt;
	
	/**
	 * @param number
	 * @param stratum
	 * @param rt
	 * @param jValue
	 */
	public ObjectInfo(long number, int stratum, Value jValue) {
		super(number, stratum, jValue);
		rt = ((ObjectReference) jValue).referenceType();
	}
	
	public ArrayList<ValueInfo> expand() {
		var objectRef = (ObjectReference) jValue;
		objectRef.getValues(rt.fields()).forEach((field, value) -> {
			ValueInfo vi = ValueInfoFactory.create(number, stratum+1, value);
			children.add(vi);
		});
    	return children;
    }
	
	/**
     * Get ReferenceType
     * @return reference type
     */
    public ReferenceType getRT() {
    	return rt;
    }
}
