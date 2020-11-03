/**
 * 
 */
package debug.value;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

/**
 * @author sugiyama
 *
 */
public class ValueInfoFactory {
	public static ValueInfo create(long number, int stratum, Value val) {
		char sign = val.type().signature().charAt(0);
		switch (sign) {
			  case '[':
				  return new ArrayInfo(number, stratum, val);
			  case 'Z': case 'B': case 'C': case 'S': case 'I': case 'J': case 'F': case 'D':
				  return new PrimitiveInfo(number, stratum, val);
			  case 'L':
				  return new ObjectInfo(number, stratum, val);
			  default:
				  return new ValueInfo(number, stratum, val);
		}
	}
}
