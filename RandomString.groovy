public class RandomString {
  Random random;

  public RandomString(final Random random = new Random()) {
    this.random = random;
  }
  
  public String next(int size) {
    return next(random, size);
  }

  public static String next(Random random, int size) {
    StringBuilder sb = new StringBuilder(size);
    size.times { sb.append((char) random.nextInt(26) + 97); };
    return sb.toString();
  }
}