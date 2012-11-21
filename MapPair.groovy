import groovy.transform.Immutable;

//Note, this is not a true value type.  Neither the key nor the value
//will be used to compute the hash code, nor in the equals method.
//I'm mainly using the @Immutable annotation to be lazy 

@Immutable
public class MapPair {
  private final Object key;
  public Object getKey() { return key; }
  
  private final Object value;
  public Object getValue() { return value; }
}