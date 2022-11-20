package jisd.analysis;

import jisd.util.Print;
import org.junit.jupiter.api.Test;

public class FlacocoTest {
  @Test
  public void basicTest() {
    FaultFinder.setJisdCmdPath("./test/src/jisd/analysis/jisd-test");
    FaultFinder.usingProbeByVariableCount= true;
    //FaultFinder.usingProbeByStackTrace = true;
    //FaultFinder.usingProbeByProgramSlice = true;
    var ff = new FaultFinder("/Users/saku/Workspace/2020/JISDLab/flacoco/examples/exampleFL1/FLtest1");
    ff.setSrcDirs("/Users/saku/Workspace/2020/gzoltar/com.gzoltar.cli.examples/src");
    ff.setFlRankingPath("/Users/saku/Workspace/2020/gzoltar/com.gzoltar.cli.examples/build/sfl/txt/ochiai.ranking.csv");
    ff.setContextGranularity(FaultFinder.Granularity.METHOD);
    ff.run();
    Print.out(ff.uri(1));
    ff.remove(1);
    ff.susp(1);
    ff.reset(2);
  }

  @Test
  public void probeTest() {
    FaultFinder.setJisdCmdPath("./test/src/jisd/analysis/jisd-test");
    var ff = new FaultFinder("/Users/saku/Workspace/2020/gzoltar/com.gzoltar.cli.examples");
    ff.setSrcDirs("/Users/saku/Workspace/2020/gzoltar/com.gzoltar.cli.examples/src");
    ff.setFlRankingPath("/Users/saku/Workspace/2020/gzoltar/com.gzoltar.cli.examples/build/sfl/txt/ochiai.ranking.csv");
    ff.setContextGranularity(FaultFinder.Granularity.METHOD);
    ff.run();
    ff.probeVariable(1);
  }
}
