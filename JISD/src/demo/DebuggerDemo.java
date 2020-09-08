package demo;

import java.util.ArrayList;

import debug.Debugger;
import debug.DebugResult;

/**
 * A debugger demo class you can use for simple debugging scripts. 
 * @author sugiyama
 *
 */
class DebuggerDemo {

	public static void main(String[] args) {
		Debugger dbg = new Debugger("demo.HelloWorld", "-cp bin/");
		dbg.setBreakPoint(28);
		dbg.setBreakPoint(30);
		//dbg.setBreakPoint("sayHello");
		//dbg.setBreakPoint("java.io.PrintStream", "println");
		dbg.run(1000);
		ArrayList<DebugResult> results = dbg.getResults();
	    results.forEach(res -> {
			System.out.println("-----------------------------");
	    	System.out.println(res.getLineNumber());
	    	System.out.println(res.getClassOfResult());
	    	System.out.println(res.getName());
	    	System.out.println(res.getValue());
	    	System.out.println("");
	    });
	    dbg.exit();
	}

}
