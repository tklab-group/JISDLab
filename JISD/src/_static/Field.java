package _static;

public class Field extends Static {
  public Field(String srcDir, String className, String fieldName) {
    super(srcDir, className, fieldName);
  }

  public Field(String className, String fieldName) {
    super(StaticFile.getSrcDir(), className, fieldName);
  }
}
