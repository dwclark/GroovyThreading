import groovy.transform.CompileStatic;

public class RandomString {
  Random random;

  public RandomString(final Random random = new Random()) {
    this.random = random;
  }
  
  public String next(int size) {
    return next(random, size);
  }

  @CompileStatic
  public static String next(Random random, int size) {
    char[] ary = new char[size];
    for(int i = 0; i < ary.length; ++i) {
      Character.toChars(random.nextInt(26) + 97, ary, i);
    }
    
    return new String(ary);
  }
}