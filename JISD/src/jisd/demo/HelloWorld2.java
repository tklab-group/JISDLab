package jisd.demo;

/**
 * A test class you can use for simple debugging scripts.
 *
 * @author sugiyama
 */
public class HelloWorld2 {
  public String helloTo;

  public HelloWorld2(String helloTo) {
    this.helloTo = helloTo;
  }

  public static void main(String[] args) {
    HelloWorld2 hello;
    int[] a = {1, 2, 3};
    String me = "Alice";
    var helloTo = "from main";
    hello = new HelloWorld2("Bob");
    System.out.println(hello.sayHello());
    a[2]++;
    hello.setHelloTo(me);
    System.out.println(hello.sayHello());
    while (true) {
      a[0]++;
    }
  }

  public String sayHello() {
    return "Hello, " + helloTo;
  }

  public void setHelloTo(String helloTo) {
    this.helloTo = helloTo;
  }
}
