/**
 * 
 */
package debug.value;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

/**
 * ValueInfo simple factory
 * @author sugiyama
 *
 */
public class ValueInfoFactory {
	/**
	 * create ValueInfo
	 * @param number timestamp
	 * @param stratum No. of the variable expansion 
	 * @param jValue jdi value
	 * @return value info
	 */
	public static ValueInfo create(long number, int stratum, Value jValue) {
		char sign = jValue.type().signature().charAt(0);
		switch (sign) {
			  case '[':
				  return new ArrayInfo(number, stratum, jValue);
			  case 'Z': case 'B': case 'C': case 'S': case 'I': case 'J': case 'F': case 'D':
				  return new PrimitiveInfo(number, stratum, jValue);
			  case 'L':
				  return new ObjectInfo(number, stratum, jValue);
			  default:
				  return new ValueInfo(number, stratum, jValue);
		}
	}
}
