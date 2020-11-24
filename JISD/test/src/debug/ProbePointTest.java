/** */
package debug;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/** @author sugiyama */
public class ProbePointTest {

  void testProbeDebugger(Debugger dbg) {
    ArrayList<String> vars = new ArrayList<>();
    vars.add("var1");
    dbg.run(1000);
    BreakPoint p = dbg.watch("LoopN", 22, vars).get();
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
    Debugger dbg = new Debugger("demo.LoopN", "-cp bin", "./src", true);
    testProbeDebugger(dbg);
  }

  @Test
  void probePointTest() {
    Debugger dbg = new Debugger(39876, true);
    testProbeDebugger(dbg);
  }
}
