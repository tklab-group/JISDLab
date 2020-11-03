package debug.value;

import java.util.ArrayList;
import java.util.Map;

import com.sun.jdi.ArrayType;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;

/**
 * Saved value information
 * @author sugiyama
 *
 */
public class ValueInfo {
	/** saved value */
	String value;
	/** time stamp */
	long number;
	/** the current number of variable expansion strata */
	int stratum;
	/** value */
	Value jValue;
	ArrayList<ValueInfo> children = new ArrayList<>();
	
	/**
	 * Constructor
	 * @param number saved value
	 * @param value time stamp
	 */
    public ValueInfo(long number, int strutum, Value jValue) {
    	this.number = number;
    	this.stratum = strutum;
    	this.value = jValue.toString(); 
    	this.jValue = jValue;
    }
    
    /**
     * Get number
     * @return time stamp
     */
    public long GetNumber() {
        return number;	
    }
    
    /**
     * Get value
     * @return saved value
     */
    public String getValue() {
    	return value;
    }
    
    /**
     * Get jdi raw value 
     * @return
     */
    public Value getJValue() {
    	return jValue;
    }
    
    public int getStratum() {
    	return stratum;
    }
    
    public ArrayList<ValueInfo> expand() {
    	return new ArrayList<>();
    }

	@Override
	public String toString() {
		return "ValueInfo [value=" + value + ", number=" + number + ", stratum=" + stratum + "]";
	}
    
    public ArrayList<ValueInfo> ch() {
    	return children;
    }
    
    
}
