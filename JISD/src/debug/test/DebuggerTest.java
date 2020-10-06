package debug.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import debug.DebugResult;
import debug.Debugger;

/**
 * Debugger Test
 * @author sugiyama
 *
 */
class DebuggerTest {

	@Test
	void basicTest() {
		Debugger dbg = new Debugger("demo.HelloWorld", "-cp bin/");
		ArrayList<Integer> bps = new ArrayList<>();
		bps.add(28);
		bps.add(30);
		bps.forEach(item -> {
			dbg.setWatchPoint(item);
		});
		dbg.run(1000);
		ArrayList<DebugResult> results = dbg.getResults();
	    for (int i = 0; i < results.size(); i++) {
	    	DebugResult res = results.get(i);
	    	System.out.println("-----------------------------");
	    	System.out.println(res.getLineNumber());
	    	System.out.println(res.getClassOfResult());
	    	System.out.println(res.getName());
	    	System.out.println(res.getValues().get(0).getValue());
	    	System.out.println("");
	    	Assertions.assertEquals(res.getLineNumber(), bps.get(i/4));
	    }
	    dbg.exit();
	}
	
	@Test
	void watchPointTest() {
		Debugger dbg = new Debugger("demo.HelloWorld", "-cp bin/");
		ArrayList<Integer> bps = new ArrayList<>();
		bps.add(28);
		bps.add(30);
		bps.forEach(item -> {
			dbg.setWatchPoint(item);
		});
		dbg.run(1000);
		ArrayList<DebugResult> results = dbg.getResults();
	    for (int i = 0; i < results.size(); i++) {
	    	DebugResult res = results.get(i);
	    	assertEquals(res.getLineNumber(), bps.get(i/4));
	    }
	    dbg.removeBreakPoint(30);
	    assertEquals(dbg.getBreakPoints().size(), 1);
	    assertEquals(dbg.getBreakPoints().get(0).getLineNumber(), bps.get(0));
	    dbg.setBreakPoint("sayHello");
	    assertEquals(dbg.getBreakPoints().size(), 2);
	    dbg.removeBreakPoint("sayHello");
	    assertEquals(dbg.getBreakPoints().size(), 1);
	    dbg.exit();
	}
	
	@Test
	void methodNameWatchPointTest() {
		Debugger dbg = new Debugger("demo.HelloWorld", "-cp bin/");
		ArrayList<Integer> bps = new ArrayList<>();
		dbg.setWatchPoint("java.io.PrintStream", "println");
		bps.forEach(item -> {
			dbg.setWatchPoint(item);
		});
		dbg.run(1000);
		dbg.exit();
		ArrayList<DebugResult> results = dbg.getResults();
	    for (int i = 0; i < results.size(); i++) {
	    	DebugResult res = results.get(i);
	    	System.out.println("-----------------------------");
	    	System.out.println(res.getLineNumber());
	    	System.out.println(res.getClassOfResult());
	    	System.out.println(res.getName());
	    	System.out.println(res.getValues().get(0).getValue());
	    	System.out.println("");
	    }
	}
	
	@Test
	void debuggerRestartTest() {
		Debugger dbg = new Debugger("demo.HelloWorld", "-cp bin/");
		ArrayList<Integer> bps = new ArrayList<>();
		bps.add(28);
		bps.add(30);
		bps.forEach(item -> {
			dbg.setWatchPoint(item);
		});
		int sleepTime = 1000;
		dbg.run(sleepTime);
		ArrayList<DebugResult> results = dbg.getResults();
	    for (int i = 0; i < results.size(); i++) {
	    	DebugResult res = results.get(i);
	    	assertEquals(res.getLineNumber(), bps.get(i/4));
	    }
	    dbg.removeBreakPoint(30);
	    assertEquals(dbg.getBreakPoints().size(), 1);
	    dbg.restart(sleepTime);
	    results = dbg.getResults();
	    assertEquals(results.size(), 4);
	    dbg.exit();
	}
	
	@Test
	void vmConnectionTest() {
		Debugger dbg = new Debugger("demo.HelloWorld", "-cp bin/");
		ArrayList<Integer> bps = new ArrayList<>();
		bps.add(28);
		bps.add(30);
		bps.forEach(item -> {
			dbg.setWatchPoint(item);
		});
		dbg.run(1000);
		dbg.exit();
		ArrayList<DebugResult> results = dbg.getResults();
	    for (int i = 0; i < results.size(); i++) {
	    	DebugResult res = results.get(i);
	    	System.out.println("-----------------------------");
	    	System.out.println(res.getLineNumber());
	    	System.out.println(res.getClassOfResult());
	    	System.out.println(res.getName());
	    	System.out.println(res.getValues().get(0).getValue());
	    	System.out.println("");
	    	Assertions.assertEquals(res.getLineNumber(), bps.get(i/4));
	    }
	}
	
	@Test
	void watchPointSetByVarNamesTest() {
		Debugger dbg = new Debugger("demo.HelloWorld", "-cp bin/");
		ArrayList<Integer> bps = new ArrayList<>();
		ArrayList<String> varNames = new ArrayList<>();
		bps.add(28);
		bps.add(30);
		varNames.add("a");
		bps.forEach(item -> {
			dbg.setWatchPoint(item, varNames);
		});
		dbg.run(1000);
		dbg.exit();
		ArrayList<DebugResult> results = dbg.getResults();
	    for (int i = 0; i < results.size(); i++) {
	    	DebugResult res = results.get(i);
	    	System.out.println("-----------------------------");
	    	System.out.println(res.getLineNumber());
	    	System.out.println(res.getClassOfResult());
	    	System.out.println(res.getName());
	    	System.out.println(res.getValues().get(0).getValue());
	    	System.out.println("");
	    	Assertions.assertEquals(res.getName(), "a");
	    }
	}
	
	@Test
	void illegalWatchPointSetTest() {
		Debugger dbg = new Debugger("demo.HelloWorld", "-cp bin/");
		ArrayList<Integer> bps = new ArrayList<>();
		ArrayList<String> varNames = new ArrayList<>();
		bps.add(28);
		bps.add(30);
		bps.add(0);
		varNames.add("a");
		bps.forEach(item -> {
			dbg.setWatchPoint(item, varNames);
		});
		dbg.run(1000);
		dbg.exit();
		ArrayList<DebugResult> results = dbg.getResults();
	    for (int i = 0; i < results.size(); i++) {
	    	DebugResult res = results.get(i);
	    	System.out.println("-----------------------------");
	    	System.out.println(res.getLineNumber());
	    	System.out.println(res.getClassOfResult());
	    	System.out.println(res.getName());
	    	System.out.println(res.getValues().get(0).getValue());
	    	System.out.println("");
	    	Assertions.assertEquals(res.getName(), "a");
	    }
	}
	
	@Test
	void breakPointTest() {
		Debugger dbg = new Debugger("demo.HelloWorld", "-cp bin/");
		ArrayList<String> varNames = new ArrayList<>();
		varNames.add("a");
		dbg.setBreakPoint(28, varNames);
		dbg.setWatchPoint(30, varNames);
		dbg.run(1000);
		ArrayList<DebugResult> results = dbg.getResults();
	    for (int i = 0; i < results.size(); i++) {
	    	DebugResult res = results.get(i);
	    	System.out.println("-----------------------------");
	    	System.out.println(res.getLineNumber());
	    	System.out.println(res.getClassOfResult());
	    	System.out.println(res.getName());
	    	System.out.println(res.getValues().get(0).getValue());
	    	System.out.println("");
	    	Assertions.assertEquals(res.getLineNumber(), 28);
	    }
	    dbg.cont();
		dbg.exit();
	}

}
