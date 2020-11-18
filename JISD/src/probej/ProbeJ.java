/**
 * 
 */
package probej;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import debug.ProbePoint;
import debug.value.ValueInfo;

/**
 * @author sugiyama
 *
 */
public class ProbeJ {
  String host;
  int port;
  Connector connector;
  
  public ProbeJ(String host, int port) {
    this.host = host;
    this.port = port;
    connector = new Connector(host, port);
  }
  
  public void run() {
      try {
        connector.openConnection();
        connector.sendCommand("PrintSocketOn");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  
  public void requestSetProbePoint(String className, String varName, int lineNumber) {
    String cmd = "Set " + className + ".java " + varName + " " + lineNumber; 
    connector.sendCommand(cmd);
  }
  
  public HashMap<Location, ArrayList<ValueInfo>> getResults() {
    return getResults("", "", 0);
  }
  
  public HashMap<Location, ArrayList<ValueInfo>> getResults(String className, String varName, int lineNumber) { 
    return connector.getResults(className, varName, lineNumber);
  }
  
  Connector getConnector() {
    return connector;
  }
  
  public void exit() {
    connector.close();
  }
  
}
