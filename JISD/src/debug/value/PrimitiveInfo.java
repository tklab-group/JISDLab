/**
 * 
 */
package debug.value;

import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

/**
 * @author sugiyama
 *
 */
public class PrimitiveInfo extends ValueInfo {

	/**
	 * @param number
	 * @param stratum
	 * @param rt
	 * @param val
	 */
	public PrimitiveInfo(long number, int stratum, Value val) {
		super(number, stratum, val);
	}

}
