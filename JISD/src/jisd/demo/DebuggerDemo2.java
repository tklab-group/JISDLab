/** */
package jisd.demo;

import jisd.debug.Debugger;

/** @author sugiyama */
class DebuggerDemo2 {
  boolean a;

  /** @param args */
  public static void main(String[] args) {
    DebuggerDemo2 d2 = new DebuggerDemo2();
    DebuggerDemo2 d22 = new DebuggerDemo22();
    DebuggerDemo2 e2 = new DebuggerDemo2();
    DebuggerDemo2 e22 = new DebuggerDemo22();
    System.out.println(!(d2 instanceof DebuggerDemo22));
    System.out.println(d2.equals(e2));
    System.out.println(d2.equals(e22));
    System.out.println(e2.equals(e22));
    Debugger dbg = new Debugger("demo.LoopN", "-cp bin");
    dbg.stopAt("main");
    dbg.run(1000);
    dbg.where();
    dbg.cont();
    dbg.sleep(1000);
    dbg.where();
    dbg.exit();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this instanceof DebuggerDemo22) ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      System.out.print(getClass() + " " + obj.getClass());
      return false;
    }
    return true;
  }
}
