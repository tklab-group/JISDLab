package jisd.analysis;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Customized version of LinkedHashSet. You can access the elements randomly to get an specific
 * element or subset. <br>
 *
 * @param <E> The element type of this set
 * @author khiranouchi
 */
@SuppressWarnings("serial")
class LinkedHashSetEx<E> extends LinkedHashSet<E> {

  /**
   * Get the value of the first element. (when this method is called, the instance should have at
   * least one element)
   *
   * @return first value
   */
  public E getFirstValue() {
    Iterator<E> itr = this.iterator();
    return itr.next();
  }

  /**
   * Get the value of the last element. (when this method is called, the instance should have at
   * least one element)
   *
   * @return last value
   */
  public E getLastValue() {
    E tmp = null;
    Iterator<E> itr = this.iterator();
    while (itr.hasNext()) {
      tmp = itr.next();
    }
    return tmp;
  }

  /**
   * Get the index of the element which equals to the specified value. When the element not exist,
   * the next index of the last element is returned.
   *
   * @param value value you are searching
   * @return index of element
   */
  public int getIndexOfValue(E value) {
    int idx = 0;
    Iterator<E> itr = this.iterator();
    while (itr.hasNext()) {
      if (itr.next().equals(value)) {
        break;
      }
      idx++;
    }
    return idx;
  }

  /**
   * Get subset. (start and end must be within the range of this set)
   *
   * @param start start index of subset
   * @param end end index of subset (the end element will not be included)
   * @param buf subset is returned here
   */
  public void getSubset(int start, int end, Set<Object> buf) {
    int idx = 0;
    Iterator<E> itr = this.iterator();
    for (; idx < start; idx++) {
      if (itr.hasNext()) {
        itr.next();
      }
    }
    for (; idx < end; idx++) {
      if (itr.hasNext()) {
        buf.add(itr.next().toString());
      }
    }
  }
}
