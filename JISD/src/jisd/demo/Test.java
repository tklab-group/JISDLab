/** */
package jisd.demo;

/** @author sugiyama */
public class Test {

  /** @param args */
  public static void main(String[] args) {
    // TODO Auto-generated method stub
    new Thread(
            () -> {
              for (int i = 0; i < 100; i++) {
                System.out.println("a" + i);
              }
            })
        .start();
    new Thread(
            () -> {
              for (int i = 0; i < 100; i++) {
                System.out.println("b" + i);
              }
            })
        .start();
    System.out.println("------------------");
  }
}
