package debug;

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
	
	/**
	 * Constructor
	 * @param number saved value
	 * @param value time stamp
	 */
    ValueInfo(long number, String value) {
    	this.number = number;
    	this.value = value; 
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
    public String getValue(){
    	return value;
    }
}
