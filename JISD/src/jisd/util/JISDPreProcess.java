package jisd.util;

import org.json.JSONArray;
import org.json.JSONException;

import static jisd.util.Name.cpSeparator;

/**
 * A process which needs before JISDLab is available.
 *
 * @author sugiyama
 */
public final class JISDPreProcess {
  private JISDPreProcess() {}

  public static void main(String[] args) {
    if (args.length != 3) {
      return;
    }
    String inputFilePath = args[0];
    String outputFilePath = args[1];
    String classpath = args[2];
    var jsonObj = Json.readJsonFile(inputFilePath);
    if (jsonObj.isEmpty()) {
      return;
    }
    var kernelObj = jsonObj.get();
    try {
      var originalArgvObj = kernelObj.getJSONArray("argv");
      var argvObj = new JSONArray();
      if (originalArgvObj.length() != 4 && originalArgvObj.get(1).equals("-cp")) {
        return;
      }
      argvObj.put(originalArgvObj.get(0)); // java
      argvObj.put("-cp"); // -cp
      String allClasspath = originalArgvObj.get(2) + cpSeparator() + classpath;
      argvObj.put(allClasspath); // classpath
      argvObj.put("io.github.spencerpark.ijava.IJava"); // main class
      argvObj.put("{connection_file}"); // jupyter connection file
      kernelObj.put("argv", argvObj);
      Json.writeJsonFile(outputFilePath, kernelObj);
    } catch (JSONException e) {
      e.printStackTrace();
      return;
    }
  }
}
