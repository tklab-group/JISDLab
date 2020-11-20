package analysis;

import org.junit.jupiter.api.Test;

public class AnalysisTest {
  @Test
  public void basicTest() {
    String[] args = new String[2];
    args[0] = "data/";
    args[1] = "bin/";
    LysMain.main(args);
  }
}
