package _static;

import org.json.JSONException;
import util.Name;
import util.Print;

import java.util.ArrayList;
import java.util.Optional;

public class LocalInfo extends StaticInfo {
  String methodName;
  Optional<ArrayList<Integer>> canSetPoint = Optional.empty();

  LocalInfo(StaticFile sf, String className, String methodName, String name) {
    super(sf, className, name);
    this.methodName = methodName;
  }

  public String methodName() {
    return methodName;
  }

  public ArrayList<Integer> canSet() {
    if (canSetPoint.isPresent()) {
      return canSetPoint.get();
    }
    var ps = sf.getPs();
    var packageAndClassName = Name.splitClassName(className);
    if (ps.isEmpty()) {
      return new ArrayList<>();
    }
    var psObj = ps.get();
    try {
      var packageObj = psObj.getJSONObject(packageAndClassName.get("package"));
      var classObj = packageObj.getJSONObject(packageAndClassName.get("class"));
      var methodsObj = classObj.getJSONObject(methodName);
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
