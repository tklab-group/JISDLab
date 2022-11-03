package jisd.analysis;

import jisd.debug.Debugger;
import jisd.util.Print;
import org.junit.jupiter.api.Test;

public class FlacocoTest {
  @Test
  public void basicTest() {
    FaultFinder.setJisdCmdPath("./test/src/jisd/analysis/jisd-test");
    var ff = new FaultFinder("./test/src/jisd/analysis/FLtest1");
    ff.setSrcDirs("./test/src/jisd/analysis/FLtest1/src/main/java");
    var res = ff.run();
    Print.out(ff.uri(1));
    ff.remove(1);
    ff.susp(1);
  }

  @Test
  public void probeTest() {
    FaultFinder.setJisdCmdPath("./test/src/jisd/analysis/jisd-test");
    var ff = new FaultFinder("./test/src/jisd/analysis/FLtest1");
    //ff.setSrcDirs("");
    var res = ff.run();
    Debugger.setDefaultSleepTime(1000);
    ff.probe();
  }
}
