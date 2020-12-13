package _static;

import org.json.JSONException;
import util.Name;
import util.Print;
import util.Stream;

import java.util.ArrayList;
import java.util.Optional;

public class MethodInfo extends StaticInfo {
  private Optional<ArrayList<String>> locals = Optional.empty();

  MethodInfo(StaticFile sf, String className, String methodName) {
    super(sf, className, methodName);
  }

  public LocalInfo local(String localName) {
    return new LocalInfo(sf, className, name, localName);
  }

  public ArrayList<String> locals() {
    if (locals.isPresent()) {
      return locals.get();
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
      var methodsObj = classObj.getJSONObject(name);
      var names = (ArrayList<String>) methodsObj.keySet().stream().collect(Stream.toArrayList());
      locals = Optional.of(names);
      return names;
    } catch (JSONException e) {
      Print.out("Data not found");
      return new ArrayList<>();
    }
  }

  @Override
  public void clearCache() {
    locals = Optional.empty();
  }
}
