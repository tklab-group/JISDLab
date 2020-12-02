/** */
package debug;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/** @author sugiyama */
public class ProbePointTest {

  void testProbeDebugger(Debugger dbg) {
    ArrayList<String> vars = new ArrayList<>();
    vars.add("var1");
    dbg.run(1000);
    Point p = dbg.watch("LoopN", 22, vars).get();
    dbg.sleep(1000);
    var results = p.getResults();
    var dr = results.get("var1");
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
    Debugger dbg = new Debugger("demo.LoopN", "-cp bin", true);
    testProbeDebugger(dbg);
  }

  // remote vm with probej required
  @Test
  void probePointTest() {
    Debugger dbg = new Debugger("localhost", 39876, true);
    testProbeDebugger(dbg);
  }

  @Test
  void probePointClearTest() {
    Debugger dbg = new Debugger("demo.LoopN", "-cp bin", true);
    ArrayList<String> vars = new ArrayList<>();
    vars.add("var1");
    dbg.run(1000);
    Point p = dbg.watch("LoopN", 22, vars).get();
    dbg.sleep(1000);
    Assertions.assertTrue(p.getVarNames().get(0).equals("var1"));
    p.remove("var1");
    Assertions.assertTrue(p.getVarNames().isEmpty());
    dbg.sleep(1000);
    dbg.exit();
  }
}
