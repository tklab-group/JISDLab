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
    long number = 0;
    int maxRecordNoOfValue = 100;
	
	/**
	 * Constructor
	 */
	DebugResultManager() {
		
	}
	
	void setMaxRecordNoOfValue(int maxRecordNoOfValue) {
		if (maxRecordNoOfValue <= 0) {
			DebuggerInfo.printError("A max record number must be a non-negative integer(> 0).");
			return;
		}
		this.maxRecordNoOfValue = maxRecordNoOfValue;
	}
	
	/**
	 * Add an observed result.
	 * @param loc An observed location
	 * @param entry An observed variable and value
	 */
	 void addVariable(String className, int lineNumber, String varName, Location loc, Map.Entry<LocalVariable, Value> entry) {
		synchronized (this) {
			DebugResult tmp = new DebugResult(className, lineNumber, varName);
			for (int i = 0; i < drs.size(); i++) {
				DebugResult res = drs.get(i);
				if (res.equals(tmp)) {
					res.addValue(number++, entry);
					return;
				}
			}
			drs.add(new DebugResult(maxRecordNoOfValue, number++, className, lineNumber, varName, loc, entry));
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
