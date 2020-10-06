/**
 * 
 */
package debug;

/**
 * @author sugiyama
 *
 */
public class ValueInfo {
	String value;
	long number;
	
    ValueInfo(long number, String value) {
    	this.number = number;
    	this.value = value; 
    }
    
    public long GetNumber() {
        return number;	
    }
    
    public String getValue(){
    	return value;
    }
}
