/** */
package jisd.demo;

/** @author sugiyama */
public class LoopN {

  /** @param args */
  public static void main(String[] args) {
    int var1 = 0,
        a = 1,
        b = 2,
        c = 3,
        d = 4,
        ee = 5,
        f = 6,
        g = 7,
        h = 8,
        ii = 9,
        j = 10,
        k = 11,
        l = 12,
        m = 13,
        n = 14,
        o = 15,
        p = 16,
        q = 17,
        r = 18,
        s = 19,
        t = 20,
        u = 21,
        v = 22,
        w = 23,
        x = 24,
        y = 25; // ,z = 26;
    int N = 1000;
    var hello = new HelloWorld("Bob");
    long start = System.nanoTime();
    for (int i = 0; i < N; i++) {
      var1++;
      if (i == 500) {
        i = 0;
      }
      // System.out.println(var1);
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    hello.sayHello();
    long end = System.nanoTime();
    System.out.println((end - start) / 1000000.0 / N + "ms");
  }
}
