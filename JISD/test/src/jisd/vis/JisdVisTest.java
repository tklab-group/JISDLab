package jisd.vis;

import jisd.debug.Debugger;
import jisd.debug.DebuggerTest;
import org.junit.jupiter.api.Test;

public class JisdVisTest {
  @Test
  void prometheusTest() {
    var dbgTest = new DebuggerTest();
    Debugger dbg = dbgTest.makeDebugger();
    PrometheusExporter prometheusExporter = new PrometheusExporter("localhost", 21520);
    dbg.setExporter(prometheusExporter);
    prometheusExporter.run();
    dbg.watch(25);
    dbg.run();
    while (true) {
    }
  }

  @Test
  void elasticSearchTest() {
    var dbgTest = new DebuggerTest();
    Debugger dbg = dbgTest.makeDebugger();
    ElasticSearchExporter esExporter = new ElasticSearchExporter("http://localhost", 9200, "sample");
    dbg.setExporter(esExporter);
    dbg.watch(25, new String[]{"a"});
    esExporter.run();
    dbg.run(2000);
    esExporter.stop();
  }

  @Test
  void elasticSearchWithBinarySearchTest() {
    var dbg = new Debugger("jisd.demo.BinarySearch", "-cp bin");
    ElasticSearchExporter esExporter = new ElasticSearchExporter("http://localhost", 9200, "sample");
    dbg.setExporter(esExporter);
    dbg.watch(12, new String[]{"bs"});
    dbg.watch(19, new String[]{"left", "right"});
    dbg.watch(25, new String[]{"left", "right"});
    esExporter.run(10);
    dbg.run(5000);
    esExporter.stop();
  }
}
