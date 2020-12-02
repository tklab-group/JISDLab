package util;

import java.nio.ByteBuffer;

public class Print {
  public static void out(Object o) {
    System.out.println(o);
  }

  public static void err(Object o) {
    System.err.println(o);
  }

  public static String ByteToString(ByteBuffer b) {
    StringBuilder buf = new StringBuilder(1024);
    while (b.hasRemaining()) {
      char c = (char) b.get();
      buf.append(c);
    }
    return buf.toString();
  }
}
