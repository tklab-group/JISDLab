/** */
package probej;

import debug.Location;
import org.junit.jupiter.api.Test;

import static util.Print.out;

/** @author sugiyama */
public class ProbeJTest {

  @Test
  public void basicTest() {
    ProbeJ p = new ProbeJ("127.0.0.1", 39876, new VirtualMachine("demo.LoopN", "-cp bin", 39876));
    p.run();
    var loc = new Location("demo.LoopN", "main", 22, "var1");
    p.requestSetProbePoint(loc);
    out(p.getResults(loc));
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
          c.sendCommand("Set LoopN.java hello.a 39");
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    p.exit();
  }
   */
}
