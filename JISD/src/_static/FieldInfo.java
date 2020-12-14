package _static;

import org.json.JSONException;
import org.json.JSONObject;
import util.Print;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class FieldInfo extends StaticInfo {
  Optional<HashMap<String, ArrayList<Integer>>> canSetPoint = Optional.empty();

  public FieldInfo(String className, String fieldName, String path, JSONObject cd, JSONObject ps) {
    super(className, "this." + fieldName, path, cd, ps);
    canSet();
  }

  public HashMap<String, ArrayList<Integer>> canSet() {
    if (canSetPoint.isPresent()) {
      return canSetPoint.get();
    }
    try {
      var classObj = classObjFromPS;
      var canSetPoint = new HashMap<String, ArrayList<Integer>>();
      classObj
          .keySet()
          .forEach(
              methodName -> {
                var methodsObj = classObj.getJSONObject(methodName);
                var fieldsObj = methodsObj.getJSONArray(name);
                var canSetObj = fieldsObj.getJSONArray(0);
                var points = new ArrayList<Integer>();
                for (int i = 0; i < canSetObj.length(); i++) {
                  points.add(canSetObj.getInt(i));
                }
                canSetPoint.put(methodName, points);
              });
      this.canSetPoint = Optional.of(canSetPoint);
      return canSetPoint;
    } catch (JSONException e) {
      Print.out("Data not found");
      return new HashMap<>();
    }
  }

  @Override
  public void clearCache() {
    canSetPoint = Optional.empty();
  }
}
