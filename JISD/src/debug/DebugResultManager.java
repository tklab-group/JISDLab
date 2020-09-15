package debug;

import java.util.ArrayList;
import java.util.Map;

import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Value;

/**
 * A debug result manager class.
 * @author sugiyama
 *
 */
class DebugResultManager {
	/** Debug results */
    ArrayList<DebugResult> drs = new ArrayList<>();
	
	/**
	 * Constructor
	 */
	DebugResultManager() {
		
	}
	
	/**
	 * Add an observed result.
	 * @param loc An observed location
	 * @param entry An observed variable and value
	 */
	 void addVariable(Location loc, Map.Entry<LocalVariable, Value> entry) {
		synchronized (this) {
			drs.add(new DebugResult(loc, entry));
		}
	 }
	
	/**
	 * Get debug results.
	 * @return debug results
	 */
	ArrayList<DebugResult> getResults() {
		return drs;
	}
	
	/**
	 * Clear debug result all.
	 */
	void clearResults() {
		drs.clear();
	}
}
