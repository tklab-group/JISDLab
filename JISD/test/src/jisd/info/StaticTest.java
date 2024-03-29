package jisd.info;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

import static jisd.util.Print.out;
import static org.junit.jupiter.api.Assertions.fail;

public class StaticTest {
  @Test
  public void basicTest() {
    var sif = new StaticInfoFactory("src", "bin");
    ClassInfo s = sif.createClass("jisd.demo.HelloWorld");
    var sep = File.separator;
    Assertions.assertEquals(s.path(), "src"+sep+"jisd"+sep+"demo"+sep+"HelloWorld.java");
    out(s.path());
    out(s.absPath());
    out(s.src());
  }

  @Test
  public void jsonFileReadTest() {
    var sif = new StaticInfoFactory("src", "bin");
    var sf = sif.getSf();
    Assertions.assertNotNull(sf.getCd());
    Assertions.assertNotNull(sf.getPs());
    out(sf.getPs().toString());
  }

  @Test
  public void getStaticTest() {
    var sif = new StaticInfoFactory("src", "bin");
    ClassInfo ci = sif.createClass("jisd.debug.VMManager");
    out(ci.superName());
    out(ci.interfaceNames());
    ClassInfo c = sif.createClass("jisd.demo.HelloWorld");
    out(c.fieldNames());
    out(c.methodNames());
    var f = c.field("helloTo");
    f.name();
    var m = c.method("main(java.lang.String[])");
    out(m.localNames());
    var l = m.local("a");
    l.name();
    out(l.canSet());
  }

  @Test
  public void getStaticErrorTest() {
    var sif = new StaticInfoFactory("src", "bin");
    ClassInfo c = sif.createClass("jisd.demo.HelloWorld");
    out(c.fieldNames());
    out(c.methodNames());
    FieldInfo f;
    try {
      f = c.field("hello");
      fail();
    } catch (RuntimeException e) {
      f = c.field("helloTo");
      out(f.canSet());
    }
    MethodInfo m;
    try {
      m = c.method("main");
      fail();
    } catch (RuntimeException e) {
      m = c.method("main(java.lang.String[])");
      out(m.localNames());
    }
    LocalInfo l;
    try {
      l = m.local("b");
      fail();
    } catch (RuntimeException e) {
      l = m.local("a");
      out(l.canSet());
    }
  }

  @Test
  public void getStaticFromJarTest() {
    var sif = new StaticInfoFactory("src", "test/src/jisd/analysis/jisd.jar");
    ClassInfo ci = sif.createClass("jisd.debug.VMManager");
    out(ci.superName());
    out(ci.interfaceNames());
    ClassInfo c = sif.createClass("jisd.demo.HelloWorld");
    out(c.fieldNames());
    out(c.methodNames());
    var f = c.field("helloTo");
    f.name();
    var m = c.method("main(java.lang.String[])");
    out(m.localNames());
    var l = m.local("a");
    l.name();
    out(l.canSet());
  }

  @Test
  public void getStaticFromMultipleFilesTest() {
    var sif =
        new StaticInfoFactory(
            "src", "test/src/jisd/analysis/jisd.jar", "bin", "lib/lombok-1.18.16.jar");
    ClassInfo ci = sif.createClass("jisd.debug.VMManager");
    out(ci.superName());
    out(ci.interfaceNames());
    ClassInfo c = sif.createClass("lombok.launch.Agent");
    out(c.fieldNames());
    out(c.methodNames());
  }
}
