package jisd.demo;

import java.util.stream.IntStream;

class BinarySearch {
  public static void main(String[] args) {
    int[] a = IntStream.rangeClosed(0, 10000000).toArray();
    var bs = new BinarySearch();
    bs.binarySearch(a, 7654321);
  }
  int binarySearch(int[] a, int key) {
    int left = -1; // 条件を満たさない最大の値
    int right = a.length; // 条件を満たす最小の値
    while(right - left >= 1) {
      int mid = left + (right - left)/2;
      if (isOk(a, mid, key)) {
        right = mid;
      } else {
        left = mid;
      }
    }
    return right;
  }

  private boolean isOk(int[] a, int index, int key) {
    return a[index] >= key;
  }
}
