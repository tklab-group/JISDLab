package jisd.util;

import java.nio.ByteBuffer;

/**
 * Provides print utility.
 *
 * @author sugiyama
 */
public final class Print {
  private Print() {}

  public static void out(Object o) {
    System.out.println(o);
  }

  public static void err(Object o) {
    System.err.println(o);
  }

  public static String byteToString(ByteBuffer b) {
    byte[] array = new byte[b.remaining()];
    b.get(array);
    return new String(array);
  }
}
