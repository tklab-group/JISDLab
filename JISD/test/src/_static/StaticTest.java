package _static;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static util.Print.out;

public class StaticTest {
  @Test
  public void basicTest() {
    Class s = new Class("src", "demo.HelloWorld");
    Assertions.assertEquals(s.path(), "src\\demo\\HelloWorld.java");
    out(s.path());
    out(s.absPath());
    out(s.src());
  }

  @Test
  public void jsonFileReadTest() {
    StaticFile s = new StaticFile("src", "bin/");
    Assertions.assertNotNull(s.getCd());
    Assertions.assertNotNull(s.getPs());
    out(StaticFile.getPs().toString());
  }

  @Test
  public void getStaticTest() {
    StaticFile s = new StaticFile("src", "bin/");
    Class c = new Class("demo.HelloWorld");
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
