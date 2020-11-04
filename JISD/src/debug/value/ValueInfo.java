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
	/** children value info */
	ArrayList<ValueInfo> children = new ArrayList<>();
	/** already expanded? */
	boolean isExpanded = false;
	
	/**
	 * Constructor
	 * @param number time stamp
	 * @param strutum the current number of variable expansion strata
	 * @param jValue jdi value
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
     * @return jdi value
     */
    public Value getJValue() {
    	return jValue;
    }
    
    /**
     * Get the current number of variable expansion strata.
     * @return stratum
     */
    public int getStratum() {
    	return stratum;
    }
    
    /**
     * Create children value info
     * @return children value info
     */
    public ArrayList<ValueInfo> expand() {
    	isExpanded = true;
    	return children;
    }

	@Override
	public String toString() {
		return "ValueInfo [value=" + value + ", number=" + number + ", stratum=" + stratum + "]";
	}
    
	/**
	 * Get children value info
	 * @return children
	 */
    public ArrayList<ValueInfo> ch() {
    	return children;
    }
    
    
}
