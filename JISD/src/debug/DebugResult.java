package debug;

import java.util.ArrayDeque;
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
	String className;
	int lineNumber;
	/** An observed location*/
	Location loc;
	/** An observed variable and value*/
	Map.Entry<LocalVariable, Value> entry;
	ArrayDeque<ValueInfo> values = new ArrayDeque<>();
	int maxRecordNoOfValue;
	
	/**
	 * Constructor
	 * @param loc An observed location
	 * @param entry An observed variable and value
	 */
    DebugResult(int maxRecordNoOfValue, long number, String className, int lineNumber, Location loc, Map.Entry<LocalVariable, Value> entry) {
        this.maxRecordNoOfValue = maxRecordNoOfValue;
    	this.className = className;
        this.lineNumber = lineNumber;
    	this.loc = loc;
        this.entry = entry;
        addValue(number, entry);
    }
    
    DebugResult(String className, int lineNumber) {
        this.className = className;
        this.lineNumber = lineNumber;
    }
    
    void addValue(long number, Map.Entry<LocalVariable, Value> entry) {
    	ValueInfo value = new ValueInfo(number, entry.getValue().toString());
    	synchronized (this) {
	    	if (values.size() >= maxRecordNoOfValue) {
	    		values.pop();
	    		values.add(value);
	    		return;
	    	}
	        values.add(value);
    	}
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
    public ValueInfo[] getValues() {
    	return values.toArray(ValueInfo[]::new);
    }
    
    public ValueInfo getLatestValue() {
    	return values.getLast();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + lineNumber;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DebugResult other = (DebugResult) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (lineNumber != other.lineNumber)
			return false;
		return true;
	}
    
}
