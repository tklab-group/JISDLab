/**
 * 
 */
package debug;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

/**
 * @author sugiyama
 *
 */
public class ProbePointTest {

  @Test
  void probePointTest() {
    Debugger dbg = new Debugger(39876, true);
    ArrayList<String> vars = new ArrayList<>();
    vars.add("var1");
    dbg.run(1000);
    BreakPoint p = dbg.watch("LoopN", 22, vars).get();
    dbg.sleep(1000);
    var results = p.getResults();
    var dr = results.get("var1");
    DebuggerTest.showResult(dr);
    dr.getValues().forEach(val->{
      System.out.println(val.getValue());
    });
    dbg.exit();
  }

}
