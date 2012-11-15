public class Timing {
  
  public static long nanos(Closure closure) {
    long begin = System.nanoTime();
    closure();
    return System.nanoTime() - begin;
  }

  public static long millis(Closure closure) {
    long begin = System.currentTimeMillis();
    closure();
    return System.currentTimeMillis() - begin;
  }
  
}