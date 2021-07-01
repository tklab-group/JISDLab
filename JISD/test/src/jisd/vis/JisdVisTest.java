package jisd.vis;

import jisd.debug.Debugger;
import jisd.debug.DebuggerTest;
import org.junit.jupiter.api.Test;

public class JisdVisTest {
    @Test
    void basicTest() {
        var dbgTest = new DebuggerTest();
        Debugger dbg = dbgTest.makeDebugger();
        Exporter exporter = new Exporter("localhost", 21520);
        dbg.setExporter(exporter);
        exporter.run();
        dbg.watch(25);
        dbg.run();
        while (true) { }
    }
}
