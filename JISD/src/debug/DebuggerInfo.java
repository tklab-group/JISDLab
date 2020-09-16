/**
 * 
 */
package debug;

/**
 * Debugger infomation class.
 * @author sugiyama
 *
 */
class DebuggerInfo {
    static void print(String message) {
    	System.out.println(">> Debugger Info: " + message);
    }
    
    static void printError(String message) {
    	System.err.println(">> Debugger Info: " + message);
    }
}
