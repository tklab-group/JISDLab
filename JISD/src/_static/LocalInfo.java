package _static;

import org.json.JSONException;
import org.json.JSONObject;
import util.Print;

import java.util.ArrayList;
import java.util.Optional;

public class LocalInfo extends StaticInfo {
  String methodName;
  Optional<ArrayList<Integer>> canSetPoint = Optional.empty();

  LocalInfo(
      String className, String methodName, String name, String path, JSONObject cd, JSONObject ps) {
    super(className, name, path, cd, ps);
    this.methodName = methodName;
    canSet();
  }

  public String methodName() {
    return methodName;
  }

  public ArrayList<Integer> canSet() {
    if (canSetPoint.isPresent()) {
      return canSetPoint.get();
    }
    try {
      var methodsObj = classObjFromPS.getJSONObject(methodName);
      var localsObj = methodsObj.getJSONArray(name);
      var canSetObj = localsObj.getJSONArray(0);
      var canSetPoint = new ArrayList<Integer>();
      for (int i = 0; i < canSetObj.length(); i++) {
        canSetPoint.add(canSetObj.getInt(i));
      }
      this.canSetPoint = Optional.of(canSetPoint);
      return canSetPoint;
    } catch (JSONException e) {
      Print.out("Data not found");
      return new ArrayList<>();
    }
  }

  @Override
  public void clearCache() {
    canSetPoint = Optional.empty();
  }
}
