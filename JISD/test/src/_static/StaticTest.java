package _static;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static util.Print.out;

public class StaticTest {
  @Test
  public void basicTest() {
    StaticFile sf = new StaticFile("src", "bin");
    ClassInfo s = new ClassInfo(sf, "demo.HelloWorld");
    Assertions.assertEquals(s.path(), "src\\demo\\HelloWorld.java");
    out(s.path());
    out(s.absPath());
    out(s.src());
  }

  @Test
  public void jsonFileReadTest() {
    StaticFile sf = new StaticFile("src", "bin");
    Assertions.assertNotNull(sf.getCd());
    Assertions.assertNotNull(sf.getPs());
    out(sf.getPs().toString());
  }

  @Test
  public void getStaticTest() {
    StaticFile sf = new StaticFile("src", "bin");
    ClassInfo c = new ClassInfo(sf, "demo.HelloWorld");
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
