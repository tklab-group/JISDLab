package debug;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Debugger infomation
 * @author sugiyama
 *
 */
class DebuggerInfo {
	/**
	 * print debugger infomation to stdout.
	 * @param message debugger infomation
	 */
    static void print(String message) {
    	System.out.println(">> Debugger Info: " + message);
    }
    
    /**
     * print debugger information to stderr.
     * @param message debugger infomation
     */
    static void printError(String message) {
    	System.err.println(">> Debugger Info: " + message);
    }
    
    /**
     * print source code.
     * @param path source path
     * @param currentLineNumber line number the breakpoint is set at
     */
    static void printSrc(String path, int currentLineNumber) {
    	Path srcFile = Path.of(path);
    	try {
			List<String> lines = Files.readAllLines(srcFile);
			int offset = 5;
			int start = (currentLineNumber - offset >= 0) ? currentLineNumber - offset + 1 : 1;
			int end   = (currentLineNumber + offset <= lines.size()) ? currentLineNumber + offset : lines.size();
			for (int i = start; i <= end; i++) {
				String position = "";
				if (i == currentLineNumber) {
					position = "=>";
				}
				System.out.println(i + " " + position + "\t" + lines.get(i-1));	
			}		
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
