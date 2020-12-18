package jisd.info;

import jisd.util.Name;
import lombok.AccessLevel;
import lombok.Getter;
import org.json.JSONObject;

import java.io.File;

public class StaticInfoFactory {
  @Getter(AccessLevel.PACKAGE)
  StaticFile sf;

  public StaticInfoFactory(String srcDir, String binDir) {
    sf = new StaticFile(srcDir, binDir);
  }

  JSONObject[] getClassObj(String className) {
    var cdObj = sf.getClassObjFromCD(className);
    var psObj = sf.getClassObjFromPS(className);
    JSONObject[] objs = {cdObj, psObj};
    return objs;
  }

  String getPath(String className) {
    return sf.getSrcDir() + File.separator + Name.toSourcePathFromClassName(className);
  }

  public ClassInfo createClass(String className) {
    var path = getPath(className);
    var objs = getClassObj(className);
    return new ClassInfo(className, path, objs[0], objs[1]);
  }

  public MethodInfo createMethod(String className, String methodName) {
    var path = getPath(className);
    var objs = getClassObj(className);
    return new MethodInfo(className, methodName, path, objs[0], objs[1]);
  }

  public FieldInfo createField(String className, String fieldName) {
    var path = getPath(className);
    var objs = getClassObj(className);
    return new FieldInfo(className, fieldName, path, objs[0], objs[1]);
  }

  public LocalInfo createLocal(String className, String methodName, String localName) {
    var path = getPath(className);
    var objs = getClassObj(className);
    return new LocalInfo(className, methodName, localName, path, objs[0], objs[1]);
  }
}
