package debug;

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
	/** reference type */
	ReferenceType rt;
	/** value */
	Value val;
	
	/**
	 * Constructor
	 * @param number saved value
	 * @param value time stamp
	 */
    ValueInfo(long number, String value, ReferenceType rt, Value val) {
    	this.number = number;
    	this.value = value; 
    	this.rt = rt;
    	this.val = val;
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
     * Get ReferenceType
     * @return reference type
     */
    public ReferenceType getRT() {
    	return rt;
    }
    
    /**
     * Get jdi raw value 
     * @return
     */
    public Value getVal() {
    	return val;
    }
}
