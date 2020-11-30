package _static;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static _static.StaticFile.*;
import static util.Print.out;

public class StaticTest {
  @Test
  public void basicTest() {
    init("src", "bin");
    ClassInfo s = new ClassInfo("demo.HelloWorld");
    Assertions.assertEquals(s.path(), "src\\demo\\HelloWorld.java");
    out(s.path());
    out(s.absPath());
    out(s.src());
  }

  @Test
  public void jsonFileReadTest() {
    init("src", "bin");
    Assertions.assertNotNull(getCd());
    Assertions.assertNotNull(getPs());
    out(getPs().toString());
  }

  @Test
  public void getStaticTest() {
    init("src", "bin");
    ClassInfo c = new ClassInfo("demo.HelloWorld");
    out(c.fields());
    out(c.methods());
    var f = c.field("helloTo");
    f.name();
    var m = c.method("main(java.lang.String[])");
    out(m.locals());
    var l = m.local("s");
    l.name();
  }
}
