package _static;

import lombok.AccessLevel;
import lombok.Getter;

public class StaticInfoFactory {
  @Getter(AccessLevel.PACKAGE)
  StaticFile sf;

  public StaticInfoFactory(String srcDir, String binDir) {
    sf = new StaticFile(srcDir, binDir);
  }

  public ClassInfo createClass(String className) {
    return new ClassInfo(sf, className);
  }

  public MethodInfo createMethod(String className, String methodName) {
    return new MethodInfo(sf, className, methodName);
  }

  public FieldInfo createField(String className, String fieldName) {
    return new FieldInfo(sf, className, fieldName);
  }

  public LocalInfo createLocal(String className, String methodName, String localName) {
    return new LocalInfo(sf, className, methodName, localName);
  }
}
