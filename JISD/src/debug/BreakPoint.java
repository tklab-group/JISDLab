/**
 * 
 */
package debug;

/**
 * Breakpoint infomation class.
 * @author sugiyama
 *
 */
public class BreakPoint {
	String className;
	int lineNumber;
	String methodName;
	
	BreakPoint(String className, int lineNumber) {
		this.className = className;
		this.lineNumber = lineNumber;
	}
	
	BreakPoint(String className, String methodName) {
		this.className = className;
		this.lineNumber = 0;
		this.methodName = methodName;
	}
	
	public String getClassName() {
	    return className;	
	}
	
	public int getLineNumber() {
		return lineNumber;
	}
	
	public String getMethodName() {
		return methodName; 
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
