/** */
package probej;

import org.junit.jupiter.api.Test;

import static util.Print.out;

/** @author sugiyama */
public class ProbeJTest {

  @Test
  public void basicTest() {
    ProbeJ p = new ProbeJ("127.0.0.1", 39876, new VirtualMachine("demo.LoopN", "-cp bin", 39876));
    p.run();
    p.requestSetProbePoint("demo.LoopN", "var1", 22);
    out(p.getResults("demo.LoopN", "var1", 22));
    p.exit();
  }
  /*
  @Test
  public void manualTest() {
    ProbeJ p = new ProbeJ("127.0.0.1", 39876, new VirtualMachine("demo.LoopN", "-cp bin", 39876));
    p.run();
    Connector c = p.getConnector();
    while (true) {
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      String inputLine;
      try {
        inputLine = in.readLine();
        if (inputLine.equals("Q")) {
          break;
        }
        if (inputLine.equals("P")) {
          p.getResults();
        } else if (inputLine.equals("P1")) {
          p.getResults("LoopN", "var1", 22);
        } else if (inputLine.equals("on")) {
          c.sendCommand("PrintSocketOn");
        } else if (inputLine.equals("S")) {
          c.sendCommand("Set LoopN.java var1 22");
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    p.exit();
  }
   */
}
