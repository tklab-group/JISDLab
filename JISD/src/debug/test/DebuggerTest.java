package debug.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import debug.BreakPoint;
import debug.DebugResult;
import debug.Debugger;
import debug.value.ValueInfo;

/**
 * Debugger Test
 * 
 * @author sugiyama
 *
 */
class DebuggerTest {
  /* line numbers a breakpoint is set */
  static final int bpln1 = 29;
  static final int bpln2 = 31;
  ArrayList<Integer> bps = new ArrayList<>();
  
  DebuggerTest() {
    bps.add(bpln1);
    bps.add(bpln2);
  }
  Debugger makeDebugger() {
    Debugger dbg = new Debugger("demo.HelloWorld", "-cp bin/");
    bps.forEach(item -> {
      dbg.watch(item, false);
    });
    return dbg;
  }

  @Test
  void basicTest() {
    Debugger dbg = makeDebugger();
    dbg.run(1000);
    ArrayList<DebugResult> results = dbg.getResults();
    for (int i = 0; i < results.size(); i++) {
      DebugResult res = results.get(i);
      System.out.println("-----------------------------");
      System.out.println(res.getLineNumber());
      System.out.println(res.getClassOfResult());
      System.out.println(res.getName());
      System.out.println(res.getLatestValue());
      System.out.println("");
      Assertions.assertEquals(res.getLineNumber(), bps.get(i / 4));
    }
    dbg.exit();
  }

  @Test
  void watchPointTest() {
    Debugger dbg = makeDebugger();
    dbg.run(1000);
    ArrayList<DebugResult> results = dbg.getResults();
    for (int i = 0; i < results.size(); i++) {
      DebugResult res = results.get(i);
      assertEquals(res.getLineNumber(), bps.get(i / 4));
    }
    dbg.clear(bpln2);
    assertEquals(dbg.getBreakPoints().size(), 1);
    assertEquals(dbg.getBreakPoints().get(0).getLineNumber(), bps.get(0));
    dbg.stopAt("sayHello");
    assertEquals(dbg.getBreakPoints().size(), 2);
    dbg.clear("sayHello");
    assertEquals(dbg.getBreakPoints().size(), 1);
    dbg.exit();
  }

  @Test
  void methodNameWatchPointTest() {
    Debugger dbg = new Debugger("demo.HelloWorld", "-cp bin/");
    ArrayList<Integer> bps = new ArrayList<>();
    dbg.watch("java.io.PrintStream", "println", false);
    bps.forEach(item -> {
      dbg.watch(item);
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
      System.out.println(res.getLatestValue());
      System.out.println("");
    }
  }

  @Test
  void debuggerRestartTest() {
    Debugger dbg = makeDebugger();
    int sleepTime = 1000;
    dbg.run(sleepTime);
    ArrayList<DebugResult> results = dbg.getResults();
    assertEquals(results.size(), 8);
    for (int i = 0; i < results.size(); i++) {
      DebugResult res = results.get(i);
      assertEquals(res.getLineNumber(), bps.get(i / 4));
    }
    dbg.clear(bpln2);
    assertEquals(dbg.getBreakPoints().size(), 1);
    dbg.restart(sleepTime);
    results = dbg.getResults();
    assertEquals(results.size(), 4);
    dbg.exit();
  }

  @Test
  void vmConnectionTest() {
    Debugger dbg = makeDebugger();
    dbg.run(1000);
    dbg.exit();
    ArrayList<DebugResult> results = dbg.getResults();
    for (int i = 0; i < results.size(); i++) {
      DebugResult res = results.get(i);
      System.out.println("-----------------------------");
      System.out.println(res.getLineNumber());
      System.out.println(res.getClassOfResult());
      System.out.println(res.getName());
      System.out.println(res.getLatestValue());
      System.out.println("");
      Assertions.assertEquals(res.getLineNumber(), bps.get(i / 4));
    }
  }

  @Test
  void watchPointSetByVarNamesTest() {
    Debugger dbg = new Debugger("demo.HelloWorld", "-cp bin/");
    ArrayList<Integer> bps = new ArrayList<>();
    ArrayList<String> varNames = new ArrayList<>();
    bps.add(bpln1);
    bps.add(bpln2);
    varNames.add("a");
    bps.forEach(item -> {
      dbg.watch(item, varNames, false);
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
      System.out.println(res.getLatestValue());
      System.out.println("");
      Assertions.assertEquals(res.getName(), "a");
    }
  }

  @Test
  void illegalWatchPointSetTest() {
    Debugger dbg = new Debugger("demo.HelloWorld", "-cp bin/");
    ArrayList<Integer> bps = new ArrayList<>();
    ArrayList<String> varNames = new ArrayList<>();
    bps.add(bpln1);
    bps.add(bpln2);
    bps.add(0);
    varNames.add("a");
    bps.forEach(item -> {
      dbg.watch(item, varNames, false);
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
      System.out.println(res.getLatestValue());
      System.out.println("");
      Assertions.assertEquals(res.getName(), "a");
    }
  }

  @Test
  void breakPointTest() {
    Debugger dbg = new Debugger("demo.HelloWorld", "-cp bin/");
    ArrayList<String> varNames = new ArrayList<>();
    varNames.add("a");
    dbg.stopAt(bpln1, varNames);
    dbg.watch(bpln2, varNames, false);
    dbg.run(1000);
    ArrayList<DebugResult> results = dbg.getResults();
    for (int i = 0; i < results.size(); i++) {
      DebugResult res = results.get(i);
      System.out.println("-----------------------------");
      System.out.println(res.getLineNumber());
      System.out.println(res.getClassOfResult());
      System.out.println(res.getName());
      System.out.println(res.getLatestValue());
      System.out.println("");
      Assertions.assertEquals(res.getLineNumber(), bpln1);
    }
    dbg.cont();
    dbg.exit();
  }

  @Test
  void valueInfoTest() {
    Debugger dbg = new Debugger("demo.HelloWorld", "-cp bin/");
    ArrayList<String> varNames = new ArrayList<>();
    varNames.add("a");
    dbg.watch(34, varNames, false);
    int maxRecords = 200;
    dbg.setMaxRecordNumber(maxRecords);
    dbg.run(2000);
    ArrayList<DebugResult> results = dbg.getResults();
    for (int i = 0; i < results.size(); i++) {
      DebugResult res = results.get(i);
      System.out.println("-----------------------------");
      System.out.println(res.getLineNumber());
      System.out.println(res.getClassOfResult());
      System.out.println(res.getName());
      System.out.println(res.getLatestValue());
      System.out.println("");
      Assertions.assertEquals(res.getValues().length, maxRecords);
    }
    ValueInfo[] values = results.get(0).getValues();
    IntStream.range(0, maxRecords).forEach(i -> {
      System.out.print(values[i].getValue() + " ");
      if (i % 10 == 9) {
        System.out.println("");
      }
    });
    dbg.exit();
  }
  
  @Test 
  void stepTest() {
    Debugger dbg = new Debugger("demo.HelloWorld", "-cp bin/");
    ArrayList<String> varNames = new ArrayList<>();
    varNames.add("a");
    dbg.stopAt(bpln1, varNames).get();
    dbg.stopAt(bpln2, varNames).get();
    dbg.run(1000);
    dbg.step();
    dbg.finish();
    dbg.next();
    dbg.next();
    dbg.locals();
    dbg.getResults().forEach((r)->{System.out.println(r.getLineNumber()+": "+r.getName());});
    dbg.exit();
  }

}
