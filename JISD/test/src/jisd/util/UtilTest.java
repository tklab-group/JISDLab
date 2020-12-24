package jisd.util;

import org.junit.jupiter.api.Test;

public class UtilTest {
  @Test
  void nameTest() {
    Print.out(Name.toClassNameFromSourcePath("aaa/ss///"));
  }

  @Test
  void IJavaKernelJsonCreateTest() {
    String[] args = new String[3];
    args[0] = "test/src/jisd/util/kernel.json";
    args[1] = "test/src/jisd/util/output_kernel.json";
    args[2] = "/workspaces/sample";
    JISDPreProcess.main(args);
  }
}
