package jisd.util;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/** Provides Json utility. */
public class Json {
  public static Optional<JSONObject> readJsonFile(String jsonFilePath) {
    Path path = Paths.get(jsonFilePath);
    String jsonStr;
    try {
      jsonStr = Files.readString(path);
    } catch (IOException e) {
      e.printStackTrace();
      return Optional.empty();
    }
    JSONObject jsonObj = new JSONObject(jsonStr);
    return Optional.ofNullable(jsonObj);
  }

  public static void writeJsonFile(String jsonFilePath, JSONObject jsonObj) {
    Path path = Paths.get(jsonFilePath);
    try {
      Files.writeString(
          path,
          jsonObj.toString(),
          Charset.forName("UTF-8"),
          StandardOpenOption.WRITE,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
  }
}
