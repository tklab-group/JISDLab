/**
 * 
 */
package probej;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.jupiter.api.Test;

import probej.ProbeJ;

/**
 * @author sugiyama
 *
 */
public class ProbeJTest {
  @Test
  public void basicTest() {
    ProbeJ p = new ProbeJ("127.0.0.1", 39876);
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
        }
        else if (inputLine.equals("on")) {
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
}
