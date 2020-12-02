package _static;

public class FieldInfo extends StaticInfo {
  public FieldInfo(String className, String fieldName) {
    super(className, fieldName);
  }

  @Override
  public void clearCache() {
    return;
  }
}
