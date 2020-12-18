package jisd.info;

import jisd.util.Print;
import jisd.util.Stream;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Optional;

public class MethodInfo extends StaticInfo {
  private Optional<ArrayList<String>> locals = Optional.empty();

  MethodInfo(String className, String methodName, String path, JSONObject cd, JSONObject ps) {
    super(className, methodName, path, cd, ps);
    localNames();
  }

  public LocalInfo local(String localName) {
    if (locals.isEmpty()) {
      throw new RuntimeException("No such local variable");
    }
    if (!locals.get().contains(localName)) {
      throw new RuntimeException("No such local variable");
    }
    return new LocalInfo(className, name, localName, path, classObjFromCD, classObjFromPS);
  }

  public ArrayList<String> localNames() {
    if (locals.isPresent()) {
      return locals.get();
    }
    try {
      var methodsObj = classObjFromPS.getJSONObject(name);
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
