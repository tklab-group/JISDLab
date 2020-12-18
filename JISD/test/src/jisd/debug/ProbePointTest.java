/** */
package jisd.debug;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** @author sugiyama */
public class ProbePointTest {
  String[] vars = new String[] {"var1"};

  void testProbeDebugger(Debugger dbg, String[] vars) {
    dbg.run(1000);
    Point p = dbg.watch("LoopN", 39, vars).get();
    dbg.sleep(1000);
    var results = p.getResults();
    var dr = results.get(vars[0]);
    DebuggerTest.showResult(dr);
    dr.getValues()
        .forEach(
            val -> {
              System.out.println(val.getValue());
            });
    dbg.exit();
  }

  @Test
  void basicTest() {
    Debugger dbg = new Debugger("jisd.demo.LoopN", "-cp bin", true);
    testProbeDebugger(dbg, vars);
  }

  @Test
  void fieldTest() {
    Debugger dbg = new Debugger("jisd.demo.LoopN", "-cp bin", true);
    String[] vars = {"hello.a"};
    testProbeDebugger(dbg, vars);
  }

  @Test
  void fieldTest2() {
    Debugger dbg = new Debugger("localhost", 39876, true);
    String[] vars = {"a"};
    testProbeDebugger(dbg, vars);
  }

  // remote vm with probej required
  @Test
  void probePointTest() {
    Debugger dbg = new Debugger("localhost", 39876, true);
    testProbeDebugger(dbg, vars);
  }

  @Test
  void probePointClearTest() {
    Debugger dbg = new Debugger("jisd.demo.LoopN", "-cp bin", true);
    dbg.run(1000);
    Point p = dbg.watch("LoopN", 39, vars).get();
    dbg.sleep(1000);
    Assertions.assertTrue(p.getVarNames().get(0).equals(vars[0]));
    p.remove(vars[0]);
    Assertions.assertTrue(p.getVarNames().isEmpty());
    dbg.sleep(1000);
    dbg.exit();
  }
}
