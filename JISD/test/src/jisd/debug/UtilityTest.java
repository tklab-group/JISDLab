/** */
package jisd.debug;

import org.junit.jupiter.api.Test;

import static jisd.debug.Utility.exec;

/** @author sugiyama */
public class UtilityTest {

  @Test
  void execTest() {
    exec("pwd");
  }
}
