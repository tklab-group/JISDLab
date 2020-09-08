package demo;

/**
 * A test class you can use for simple debugging scripts.
 * @author sugiyama
 *
 */
public class HelloWorld {
    private String helloTo;

    public HelloWorld(String helloTo) {
        this.helloTo = helloTo;
    }

    public String sayHello() {
        return "Hello, " + helloTo;
    }

    public void setHelloTo(String helloTo) {
        this.helloTo = helloTo;
    }

    public static void main(String[] args) {
        HelloWorld hello;
        int a = 1;
        String me = "Alice";
        hello = new HelloWorld("Bob");
        System.out.println(hello.sayHello());
        a++;
        hello.setHelloTo(me);
        System.out.println(hello.sayHello());
        while (true) {}
    }
}
