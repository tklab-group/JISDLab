package jisd.demo;

/**
 * A test class you can use for simple debugging scripts.
 *
 * @author sugiyama
 */
public class HelloWorld {
  int a = 1;
  private String helloTo;

  public HelloWorld(String helloTo) {
    this.helloTo = helloTo;
  }

  public static void main(String[] args) {
    int a = 0;
    String me = "Alice";
    var hello = new HelloWorld("Bob");
    System.out.println(hello.sayHello());
    a++;
    hello.setHelloTo(me);
    System.out.println(hello.sayHello());
    while (true) { 
      a += 10000;
    }
  }

  public String sayHello() {
    return "Hello, " + helloTo;
  }

  public void setHelloTo(String helloTo) {
    this.helloTo = helloTo;
  }
}
