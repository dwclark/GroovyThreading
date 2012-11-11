import groovy.transform.Immutable;
import groovy.transform.WithReadLock;
import groovy.transform.WithWriteLock;

@Immutable
public class MapPair {
  private final Object key;
  public Object getKey() { return key; }
  
  private final Object value;
  public Object getValue() { return value; }
}

public class MapNoSynch {
  private List list = [];
  
  public Object getAt(String key) {
    return list.find { pair -> pair.key == key; }?.value;
  }

  public Object putAt(String key, Object value) {
    for(int i = 0; i < list.size(); ++i) {
      if(list[i].key == key) {
	list.remove(i);
	break;
      }
    }

    list.add(new MapPair(key: key, value: value));
  }
}

public class MapSynch extends MapNoSynch {
  
  public synchronized Object getAt(String key) {
    return super.getAt(key);
  } 

  public synchronized Object putAt(String key, Object value) {
    return super.putAt(key, value);
  }
}

public class MapRwSynch extends MapNoSynch {
  
  @WithReadLock
  public Object getAt(String key) {
    return super.getAt(key);
  }

  @WithWriteLock
  public Object putAt(String key, Object value) {
    return super.putAt(key, value);x
  }


def testIt = { map ->
  map['1'] = 1;
  map['2'] = 2;
  map['3'] = 3;
  assert(map['3'] == 3); };

testIt(new MapNoSynch());
testIt(new MapSynch());
testIt(new MapRwSynch());

