package _static;

import org.json.JSONException;
import util.Name;
import util.Print;
import util.Stream;

import java.util.ArrayList;
import java.util.Optional;

public class Method extends Static {
  private Optional<ArrayList<String>> locals = Optional.empty();

  public Method(String srcDir, String className, String methodName) {
    super(srcDir, className, methodName);
  }

  public Method(String className, String methodName) {
    super(StaticFile.getSrcDir(), className, methodName);
  }

  public Local local(String localName) {
    return new Local(srcDir, className, name, localName);
  }

  public ArrayList<String> locals() {
    if (locals.isPresent()) {
      return locals.get();
    }
    var ps = StaticFile.getPs();
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
}
