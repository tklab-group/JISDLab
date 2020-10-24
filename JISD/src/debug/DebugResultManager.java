package debug;

import java.util.ArrayList;
import java.util.Map;

import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Value;

/**
 * Debug result manager
 * @author sugiyama
 *
 */
class DebugResultManager {
	/** Debug results */
    ArrayList<DebugResult> drs = new ArrayList<>();
    /** time stamp */
    long number = 0;
    /** the max record number of values */
    int maxRecordNoOfValue = 100;
	
	/**
	 * Constructor
	 */
	DebugResultManager() {
		
	}
	
	/**
	 * Set the max record number of values
	 * @param maxRecordNoOfValue the max record number of values
	 */
	void setMaxRecordNoOfValue(int maxRecordNoOfValue) {
		if (maxRecordNoOfValue <= 0) {
			DebuggerInfo.printError("A max record number must be a non-negative integer(> 0).");
			return;
		}
		this.maxRecordNoOfValue = maxRecordNoOfValue;
	}
	
	/**
	 * Add an observed result.
	 * @param bp breakpoint
	 * @param className class name
	 * @param lineNumber line number
	 * @param varName variable name
	 * @param loc An observed location
	 * @param entry entry An observed variable and value
	 */
	 void addVariable(BreakPoint bp, String className, int lineNumber, String varName, Location loc, Map.Entry<LocalVariable, Value> entry) {
		synchronized (this) {
			DebugResult tmp = new DebugResult(className, lineNumber, varName);
			for (int i = 0; i < drs.size(); i++) {
				DebugResult res = drs.get(i);
				if (res.equals(tmp)) {
					res.addValue(number++, entry);
					return;
				}
			}
			DebugResult dr = new DebugResult(maxRecordNoOfValue, number++, className, lineNumber, varName, loc, entry);
			drs.add(dr);
			bp.setDebugResult(dr);
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
		number = 0;
	}
}
