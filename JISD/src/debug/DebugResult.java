package debug;

import java.util.Map;

import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Value;

/**
 * Debug result class.
 * @author sugiyama
 *
 */
public class DebugResult {
	/** An observed location*/
	Location loc;
	/** An observed variable and value*/
	Map.Entry<LocalVariable, Value> entry;
	String value;
	
	/**
	 * Constructor
	 * @param loc An observed location
	 * @param entry An observed variable and value
	 */
    DebugResult(Location loc, Map.Entry<LocalVariable, Value> entry) {
        this.loc = loc;
        this.entry = entry;
        this.value = entry.getValue().toString();
    }
    
    /**
     * Get an observed location.
     * @return location
     */
    Location getLocation() {
        return loc;	
    }
    
    /**
     * Get an observed variable and value
     * @return variable and value
     */
    Map.Entry<LocalVariable, Value> getEntry() {
    	return entry;
    }
    
    /**
     * Get a line number of an observed location in a target java file.
     * @return line number
     */
    public int getLineNumber() {
    	return loc.lineNumber();
    }
    
    /**
     * Get a class name of an observed variable.
     * @return class name
     */
    public String getClassOfResult() {
        return entry.getKey().typeName();
    }
    
    /**
     * Get a name of an observed variable.
     * @return variable name
     */
    public String getName() {
    	return entry.getKey().name();
    }
    
    /**
     * Get an observed value
     * @return value
     */
    public String getValue() {
    	return value;
    }
    
}
