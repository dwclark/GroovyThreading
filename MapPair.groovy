import groovy.transform.Immutable;

@Immutable
public class MapPair {
  private final Object key;
  public Object getKey() { return key; }
  
  private final Object value;
  public Object getValue() { return value; }
}
