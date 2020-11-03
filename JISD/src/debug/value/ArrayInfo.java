/**
 * 
 */
package debug.value;

import java.util.ArrayList;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

/**
 * @author sugiyama
 *
 */
public class ArrayInfo extends ObjectInfo {
	/**
	 * @param number
	 * @param stratum
	 * @param rt
	 * @param jValue
	 */
	public ArrayInfo(long number, int stratum, Value jValue) {
		super(number, stratum, jValue);
	}
	
	public ArrayList<ValueInfo> expand() {
		var arrayRef = (ArrayReference) jValue;
		arrayRef.getValues().forEach(val -> {
			ValueInfo vi = ValueInfoFactory.create(number, stratum+1, val);
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
