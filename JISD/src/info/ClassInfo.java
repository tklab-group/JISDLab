package info;

import org.json.JSONException;
import org.json.JSONObject;
import util.Print;
import util.Stream;

import java.util.ArrayList;
import java.util.Optional;

public class ClassInfo extends StaticInfo {
  private Optional<ArrayList<String>> methods = Optional.empty();
  private Optional<ArrayList<String>> fields = Optional.empty();
  private Optional<String> superClass = Optional.empty();
  private Optional<ArrayList<String>> interfaces = Optional.empty();

  ClassInfo(String className, String path, JSONObject cd, JSONObject ps) {
    super(className, className, path, cd, ps);
    methodNames();
    fieldNames();
  }

  public FieldInfo field(String name) {
    if (fields.isEmpty()) {
      throw new RuntimeException("No such field");
    }
    if (!fields.get().contains(name)) {
      throw new RuntimeException("No such field");
    }
    return new FieldInfo(className, name, path, classObjFromCD, classObjFromPS);
  }

  public MethodInfo method(String name) {
    if (methods.isEmpty()) {
      throw new RuntimeException("No such method");
    }
    if (!methods.get().contains(name)) {
      throw new RuntimeException("No such method");
    }
    return new MethodInfo(className, name, path, classObjFromCD, classObjFromPS);
  }

  public ArrayList<String> methodNames() {
    if (methods.isPresent()) {
      return methods.get();
    }
    try {
      var methodsObj = classObjFromCD.getJSONObject("methods");
      var names = (ArrayList<String>) methodsObj.keySet().stream().collect(Stream.toArrayList());
      methods = Optional.of(names);
      return names;
    } catch (JSONException e) {
      Print.out("Data not found");
      return new ArrayList<>();
    }
  }

  public ArrayList<String> fieldNames() {
    if (fields.isPresent()) {
      return fields.get();
    }
    try {
      var fieldsObj = classObjFromCD.getJSONObject("fields");
      var names = (ArrayList<String>) fieldsObj.keySet().stream().collect(Stream.toArrayList());
      fields = Optional.of(names);
      return names;
    } catch (JSONException e) {
      Print.out("Data not found");
      return new ArrayList<>();
    }
  }

  public String superName() {
    if (superClass.isPresent()) {
      return superClass.get();
    }
    try {
      var superName = classObjFromCD.getString("super");
      superClass = Optional.of(superName);
      return superName;
    } catch (JSONException e) {
      Print.out("Data not found");
      return "";
    }
  }

  public ArrayList<String> interfaceNames() {
    if (interfaces.isPresent()) {
      return interfaces.get();
    }
    try {
      var interfacesObj = classObjFromCD.getJSONArray("interfaces");
      var names = new ArrayList<String>();
      for (int i = 0; i < interfacesObj.length(); i++) {
        names.add(interfacesObj.getString(i));
      }
      interfaces = Optional.of(names);
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
