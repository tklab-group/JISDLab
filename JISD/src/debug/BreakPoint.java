/**
 * 
 */
package debug;

import java.util.ArrayList;

/**
 * Breakpoint infomation
 * @author sugiyama
 *
 */
public class BreakPoint {
	/** class name */
	String className;
	/** line number */
	int lineNumber;
	/** method name */
	String methodName;
	/** variable names */
	ArrayList<String> varNames;
	/** break or not at points*/
	boolean isBreak;
	
	/**
	 * Constructor
	 * @param className class name
	 * @param lineNumber line number
	 */
	BreakPoint(String className, int lineNumber) {
		this(className, lineNumber, "", new ArrayList<String>(), false);
	}
	
	/**
	 * Constructor
	 * @param className class name
	 * @param methodName method name
	 */
	BreakPoint(String className, String methodName) {
		this(className, 0, methodName, new ArrayList<String>(), false);
	}
	
	/**
	 * Constructor
	 * @param className class name
	 * @param lineNumber line number
	 * @param varNames variable names
	 * @param isBreak break or not at points
	 */
	BreakPoint(String className, int lineNumber, ArrayList<String> varNames, boolean isBreak) {
		this(className, lineNumber, "", varNames, isBreak);
	}
	
	/**
	 * Constructor
	 * @param className class name
	 * @param methodName method name
	 * @param varNames variable names
	 * @param isBreak break or not at points
	 */
	BreakPoint(String className, String methodName, ArrayList<String> varNames, boolean isBreak) {
		this(className, 0, methodName, varNames, isBreak);
	}
	
	/**
	 * Constructor
	 * @param className class name
	 * @param lineNumber line number
	 * @param methodName method name
	 * @param varNames variable names
	 * @param isBreak break or not at points
	 */
	BreakPoint(String className, int lineNumber, String methodName, ArrayList<String> varNames, boolean isBreak) {
		this.className = className;
		this.lineNumber = lineNumber;
		this.methodName = methodName;
		varNames.remove("");
		this.varNames = varNames;
		this.isBreak = isBreak;
	}
	
	/**
	 * Get a class name.
	 * @return class name
	 */
	public String getClassName() {
	    return className;	
	}
	
	/**
	 * Get a line number
	 * @return line number
	 */
	public int getLineNumber() {
		return lineNumber;
	}
	
	/**
	 * Get a method name
	 * @return method name
	 */
	public String getMethodName() {
		return methodName; 
	}
	
	/**
	 * Get variable names
	 * @return variable names
	 */
	public ArrayList<String> getVarNames() {
		return varNames;
	}
	
	/**
	 * Get whether the debugger breaks or not at points
	 * @return break or not at points
	 */
	public boolean getIsBreak() {
		return isBreak;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + lineNumber;
		result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
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
		BreakPoint other = (BreakPoint) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (lineNumber != other.lineNumber)
			return false;
		if (methodName == null) {
			if (other.methodName != null)
				return false;
		} else if (!methodName.equals(other.methodName))
			return false;
		return true;
	}
}
