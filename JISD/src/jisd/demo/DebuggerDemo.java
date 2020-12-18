package jisd.demo;

/**
 * A debugger demo class you can use for simple debugging scripts.
 *
 * @author sugiyama
 */
class DebuggerDemo {

  /*static void showResult(DebugResult res) {
    System.out.println("-----------------------------");
    var loc = res.getLoc();
    System.out.println(loc.getClassName());
    System.out.println(loc.getLineNumber());
    System.out.println(res.getLatestValue());
    System.out.println("");
  }*/

  static int sample() {
    int i = 0;
    i++;
    i++;
    i++;
    return i;
  }

  public static void main(String[] args) {
    /*
    // Debugger dbg = new Debugger(39876, true);
    Debugger dbg = new Debugger("demo.LoopN", "-cp bin");
    ArrayList<String> vars = new ArrayList<>();
    vars.add("var1");
    dbg.run(1000);
    Point p = dbg.watch("LoopN", 22, vars).get();
    dbg.sleep(1000);
    var results = p.getResults();
    var dr = results.get("var1");
    showResult(dr);
    dr.getValues()
        .forEach(
            val -> {
              System.out.println(val.getValue());
            });
    dbg.exit();
    /*
    // Debugger dbg = new Debugger("demo.HelloWorld", "-cp bin", "src", 39876);
    // Debugger dbg = new Debugger(39876);

    // Debugger dbg = new Debugger("demo.HelloWorld", "-cp bin", "./src");
    // Debugger dbg = new Debugger("demo.LoopN", "-cp bin", "./src", true);
    // ArrayList<String> vars = new ArrayList<>();
    //  vars.add("var1");
    //  dbg.setMaxNoOfExpand(1);
    dbg.watch(22);
    // dbg.stop(31, vars);
    dbg.run(1000);
    // dbg.cont();
    // dbg.sleep(1000);
    // dbg.cont();
    // dbg.setBreakPoint("sayHello");
    // dbg.setBreakPoint("java.io.PrintStream", "println");
    /*
     * dbg.sleep(10000); dbg.run(5000);
     */
    /*
    ArrayList<DebugResult> results = dbg.getResults();
    results.forEach(
        res -> {
          System.out.println("-----------------------------");
          System.out.println(res.getLineNumber());
          System.out.println(res.getName());
          System.out.println(res.getLatestValue());
          System.out.println(res.getLatestValue().ch());
          System.out.println("");
        });
    */
    // System.out.println(bp1.get().getDebugResult("a").get().getLineNumber());
    // System.out.println(bp2.get().getDebugResult("a").get().getLineNumber());
    /*
     * long start = System.nanoTime(); ValueInfo[] vals =
     * results.get(0).getValues(); for (int i = 0; i < vals.length; i++) {
     * System.out.println(vals[i].getValue()); } long end = System.nanoTime();
     * System.out.println((double) (end - start) / 1000000.0 + "ms");
     */
    /*  dbg.locals();
    dbg.next();
    dbg.locals();
    dbg.next();
    dbg.locals();
    dbg.step();
    dbg.step();
    dbg.where();*/

    // dbg.exit();
  }
}
