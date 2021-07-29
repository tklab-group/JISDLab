/** */
package jisd.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;

/**
 * Provides stream utility.
 *
 * @author sugiyama
 */
public class Stream {
  private Stream() {}

  public static <T> Collector<T, ?, List<T>> toArrayList() {
    Collector<T, ?, List<T>> toArrayList =
        Collector.of(
            ArrayList::new,
            List::add,
            (l1, l2) -> {
              l1.addAll(l2);
              return l1;
            },
            Characteristics.IDENTITY_FINISH);
    return toArrayList;
  }
}
