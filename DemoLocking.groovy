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

public class BaseMap {
  private List list = [];
  
  public Object getAt(String key) {
    return list.find { pair -> pair.key == key; }?.value;
  }

  public void putAt(String key, Object value) {
    for(int i = 0; i < list.size(); ++i) {
      if(list[i].key == key) {
	list.remove(i);
	break;
      }
    }

    list.add(new MapPair(key: key, value: value));
  }
}

public class MapSynch extends BaseMap {
  
  public synchronized Object getAt(String key) {
    return super.getAt(key);
  } 

  public synchronized void putAt(String key, Object value) {
    super.putAt(key, value);
  }
}

public class MapRwSynch extends BaseMap {
  
  @WithReadLock
  public Object getAt(String key) {
    return super.getAt(key);
  }

  @WithWriteLock
  public void putAt(String key, Object value) {
    super.putAt(key, value);
  } 
}

public class MapTester {
  private def theMap;
  public long readCount = 0;
  public long writeCount = 0;
  private Random random = new Random();
  private static int MAX = 500;
  private static int READ_MAX = 19;

  public Map getCounts() {
    return [ read: readCount, wrote: writeCount ];
  }
  
  public MapTester(def theMap) { this.theMap = theMap; }
  
  public void runTest() {
    int nextVal = random.nextInt(MAX);
    if(random.nextInt(READ_MAX+1) == READ_MAX) {
      //do the write
      theMap[nextVal.toString()] = nextVal;
      ++writeCount;
    }
    else {
      //do the read
      def val = theMap[nextVal.toString()];
      //The next line is mainly to prevent attempts by the JVM
      //to optimize away the previous line.
      if(val && val == 1000) println(' ');
      ++readCount;
    }
  }

  public static Thread run(MapTester theMap) {
    def t = new Thread(Interrupts.runnable(work: { -> theMap.runTest(); }));
    t.start();
    return t;
  }
}

def printResults(final def tests) {
  println("Reads: " + tests.sum { it.readCount; });
  println("Writes: " + tests.sum { it.writeCount; });
}

final int NUM_PROCS = Runtime.getRuntime().availableProcessors();
long SLEEP_TIME = 10_000;
def sMap = new MapSynch();
def sMapTests = (0..<(NUM_PROCS*3)).collect { new MapTester(sMap); };
def sMapThreads = sMapTests.collect { MapTester.run(it); };
sleep(SLEEP_TIME);
sMapThreads.each { it.interrupt(); };
sMapThreads.each { it.join(); };
printResults(sMapTests);
