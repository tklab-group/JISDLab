package _static;

import org.json.JSONException;
import util.Name;
import util.Print;
import util.Stream;

import java.util.ArrayList;
import java.util.Optional;

public class ClassInfo extends StaticInfo {
  private Optional<ArrayList<String>> methods = Optional.empty();
  private Optional<ArrayList<String>> fields = Optional.empty();

  public ClassInfo(String className) {
    super(className, className);
  }

  public FieldInfo field(String name) {
    return new FieldInfo(className, name);
  }

  public MethodInfo method(String name) {
    return new MethodInfo(className, name);
  }

  public ArrayList<String> methods() {
    if (methods.isPresent()) {
      return methods.get();
    }
    var cd = StaticFile.getCd();
    var packageAndClassName = Name.splitClassName(className);
    if (cd.isEmpty()) {
      return new ArrayList<>();
    }
    var cdObj = cd.get();
    try {
      var packageObj = cdObj.getJSONObject(packageAndClassName.get("package"));
      var classObj = packageObj.getJSONObject(packageAndClassName.get("class"));
      var methodsObj = classObj.getJSONObject("methods");
      var names = (ArrayList<String>) methodsObj.keySet().stream().collect(Stream.toArrayList());
      methods = Optional.of(names);
      return names;
    } catch (JSONException e) {
      Print.out("Data not found");
      return new ArrayList<>();
    }
  }

  public ArrayList<String> fields() {
    if (fields.isPresent()) {
      return fields.get();
    }
    var cd = StaticFile.getCd();
    var packageAndClassName = Name.splitClassName(className);
    if (cd.isEmpty()) {
      return new ArrayList<>();
    }
    var cdObj = cd.get();
    try {
      var packageObj = cdObj.getJSONObject(packageAndClassName.get("package"));
      var classObj = packageObj.getJSONObject(packageAndClassName.get("class"));
      var methodsObj = classObj.getJSONObject("fields");
      var names = (ArrayList<String>) methodsObj.keySet().stream().collect(Stream.toArrayList());
      fields = Optional.of(names);
      return names;
    } catch (JSONException e) {
      Print.out("Data not found");
      return new ArrayList<>();
    }
  }

  @Override
  public void clearCache() {
    methods = Optional.empty();
    fields = Optional.empty();
  }
}