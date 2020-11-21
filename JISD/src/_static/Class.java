package _static;

import org.json.JSONException;
import util.Name;
import util.Print;
import util.Stream;

import java.util.ArrayList;

public class Class extends Static {
  public Class(String srcDir, String className) {
    super(srcDir, className, className);
  }

  public Field field(String name) {
    return new Field(srcDir, className, name);
  }

  public Method method(String name) {
    return new Method(srcDir, className, name);
  }

  public ArrayList<String> methods() {
    var ps = StaticFile.getPs();
    var packageAndClassName = Name.splitClassName(className);
    if (ps.isEmpty()) {
      return new ArrayList<>();
    }
    var psObj = ps.get();
    try {
      var packageObj = psObj.getJSONObject(packageAndClassName.get("package"));
      var classObj = packageObj.getJSONObject("class");
      var names = (ArrayList<String>) classObj.keySet().stream().collect(Stream.toArrayList());
      return names;
    } catch (JSONException e) {
      Print.out("Class not found");
      return new ArrayList<>();
    }
  }

  public ArrayList<String> fields() {
    // Todo: get data from field_data.json
    return null;
  }
}
