/** */
package jisd.debug;

import jisd.util.Print;
import org.junit.jupiter.api.Test;

import static jisd.debug.DebuggerTest.bpln1;
import static jisd.debug.Utility.exec;
import static jisd.debug.Utility.uri;

/** @author sugiyama */
public class UtilityTest {

  @Test
  void execTest() {
    exec("pwd");
  }

  @Test
  void uriTest() {
    Debugger dbg = new Debugger("jisd.demo.HelloWorld", "-cp bin");
    dbg.setSrcDir("./src");
    dbg.stopAt(bpln1);
    dbg.run(1000);
    Print.out(dbg.uri());
    Utility.setVscodeWorkspaceDir("./src/");
    Print.out(dbg.uri());
    dbg.cont();
    dbg.exit();
  }
}
