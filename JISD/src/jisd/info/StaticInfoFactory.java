package jisd.info;

import jisd.util.Name;
import lombok.AccessLevel;
import lombok.Getter;
import org.json.JSONObject;

import java.io.File;

/** Creates StaticInfo. */
public class StaticInfoFactory {
  @Getter(AccessLevel.PACKAGE)
  StaticFile sf;

  public StaticInfoFactory(String srcDir, String... bins) {
    sf = new StaticFile(srcDir, bins);
  }

  JSONObject[] getClassObj(String className) {
    var cdObj = sf.getClassObjFromCD(className);
    var psObj = sf.getClassObjFromPS(className);
    JSONObject[] objs = {cdObj, psObj};
    return objs;
  }

  String getPath(String className) {
    var srcDir = sf.getSrcDir();
    srcDir = (srcDir.endsWith(File.separator)) ? srcDir : srcDir + File.separator;
    return srcDir + Name.toSourcePathFromClassName(className);
  }

  /** Create ClassInfo. */
  public ClassInfo createClass(String className) {
    var path = getPath(className);
    var objs = getClassObj(className);
    return new ClassInfo(className, path, objs[0], objs[1]);
  }
  /** Create MethodInfo. */
  public MethodInfo createMethod(String className, String methodName) {
    var path = getPath(className);
    var objs = getClassObj(className);
    return new MethodInfo(className, methodName, path, objs[0], objs[1]);
  }

  /** Create FieldInfo. */
  public FieldInfo createField(String className, String fieldName) {
    var path = getPath(className);
    var objs = getClassObj(className);
    return new FieldInfo(className, fieldName, path, objs[0], objs[1]);
  }
  /** Create LocalInfo. */
  public LocalInfo createLocal(String className, String methodName, String localName) {
    var path = getPath(className);
    var objs = getClassObj(className);
    return new LocalInfo(className, methodName, localName, path, objs[0], objs[1]);
  }
}
