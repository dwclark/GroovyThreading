import java.util.concurrent.*;
import java.lang.reflect.Constructor;
import groovy.transform.CompileStatic;

@CompileStatic
public abstract class TaskDivider {
  protected final int lower;
  protected final int upper;
  protected final def indexable;
  protected final def closure;

  public TaskDivider(Map args) {
    this((int) args.lower, (int) args.upper, args.indexable, args.closure);
  }

  public TaskDivider(int lower, int upper, Object indexable, Object closure) {
    this.lower = lower;
    this.upper = upper;
    this.indexable = indexable;
    this.closure = closure;
  }

  public static final Class[] conArgs = [ int, int, Object, Object ] as Class[];

  public int getSize() { return upper - lower; }
  public int getMidpoint() { return lower + ((int) (size / 2)); }
  
  public TaskDivider getUpperTask() {
    (TaskDivider) getClass().getDeclaredConstructor(conArgs).newInstance(midpoint, upper, indexable, closure);
  }

  public TaskDivider getLowerTask() {
    (TaskDivider) getClass().getDeclaredConstructor(conArgs).newInstance(lower, midpoint, indexable, closure);
  }

  public abstract void doSequential();
  public void joinTasks(TaskDivider lower, TaskDivider upper) { }
}

public class FJFill extends TaskDivider {
  public FJFill(Map args) { super(args); }
  public FJFill(int lower, int upper, Object indexable, Object closure) {
    super(lower, upper, indexable, closure);
  }

  public void doSequential() {
    for(int i = lower; i < upper; ++i) {
      indexable[i] = closure(i);
    }
  }
}

public class FJMax extends TaskDivider {
  public FJMax(Map args) { super(args); }
  public FJMax(int lower, int upper, Object indexable, Object closure) {
    super(lower, upper, indexable, closure);
  }

  def max;

  public void doSequential() {
    max = indexable[lower];
    for(int i = (lower+1); i < upper; ++i) {
      def toCmp = indexable[i];
      if(max < toCmp) max = toCmp;
    }
  }

  @Override public void joinTasks(TaskDivider lowerTask, TaskDivider upperTask) {
    if(lowerTask.max < upperTask.max) max = upperTask.max;
    else max = lowerTask.max;
  }
}

public class FJFindAll extends TaskDivider {
  public FJFindAll(Map args) { super(args); }
  public FJFindAll(int lower, int upper, Object indexable, Object closure) {
    super(lower, upper, indexable, closure);
  }

  def found;

  public void doSequential() {
    for(int i = lower; i < upper; ++i) {
      def item = indexable[i];
      if(closure(item)) {
	if(found == null) found = [];
	found.add(item);
      }
    }
  }

  @Override public void joinTasks(TaskDivider lowerTask, TaskDivider upperTask) {
    if(!lowerTask.found && !upperTask.found) return;
    else if(lowerTask.found && !upperTask.found) found = lowerTask.found;
    else if(!lowerTask.found && upperTask.found) found = upperTask.found;
    else {
      found = new ArrayList(lowerTask.found);
      found.addAll(upperTask.found);
    }
  }
}

@CompileStatic
public class FJTask extends RecursiveAction {
  private final TaskDivider problem;
  private final int threshold;
  
  public FJTask(final TaskDivider problem, final int threshold) {
    this.problem = problem;
    this.threshold = threshold;
  }

  protected void compute() {
    if(problem.size < threshold) {
      problem.doSequential();
    }
    else {
      TaskDivider lowerTask = problem.lowerTask;
      TaskDivider upperTask = problem.upperTask;
      FJTask lowerFJ = new FJTask(lowerTask, threshold);
      FJTask upperFJ = new FJTask(upperTask, threshold);
      invokeAll(lowerFJ, upperFJ);
      problem.joinTasks(lowerTask, upperTask);
    }
  }
}

public class FJUtils {
  private static final ThreadLocal tlPool = new ThreadLocal() {
    @Override protected Object initialValue() {
      return new ForkJoinPool();
    }; };

  public static ForkJoinPool pool() { return tlPool.get(); }
  public static final int THRESHOLD = 2000;

  public static Object runFill(def indexable, def closure) {
    def fr = new FJFill(0, indexable.size(), indexable, closure);
    pool().invoke(new FJTask(fr, THRESHOLD));
    return indexable;
  }

  public static Object runMax(def indexable) {
    def fmax = new FJMax(0, indexable.size(), indexable, null);
    pool().invoke(new FJTask(fmax, THRESHOLD));
    return fmax.max;
  }

  public static Object runFindAll(def indexable, def closure) {
    def fall = new FJFindAll(0, indexable.size(), indexable, closure);
    pool().invoke(new FJTask(fall, THRESHOLD));
    return fall.found;
  }

  public static void installEnhanced() {
    java.lang.Object.metaClass.myParallelFill = { def closure ->
      return runFill(delegate, closure); };

    java.lang.Object.metaClass.myParallelMax = { ->
      return runMax(delegate); };

    java.lang.Object.metaClass.myParallelFindAll = { def closure ->
      return runFindAll(delegate, closure); };
  }
}

FJUtils.installEnhanced();

def intArray = new int[300_000];
def pool = new ForkJoinPool();
final def randomInt = { int i -> ThreadLocalRandom.current().nextInt(1, 500_001); };
def fr = new FJFill(lower: 0, upper: intArray.length, indexable: intArray,
		    closure: randomInt);
def fmax = new FJMax(lower: 0, upper: intArray.length, indexable: intArray);

//Note, the start up times for the fork/join framework are horrendous
//The nature of the speed up suggests that this speedup is due to
//hotspot optimizations (since replacing the generator resets run times
//to very slow).  This suggests that the Fork/Join framework should only
//be used when the following conditions apply:
//1) You are dealing with large datasets
//2) The design constraints of Fork/Join are kept (no I/O, no synchronization)
//3) The code is on a hot path in your application (otherwise hotspot will not speed it up)
//4) The code is run in server mode (client mode usually disables hotspot)
//Total speedup is 5-10X on subsequent runs
println("***** Timing for random ints *****");
println('Parallel #1: ' + Timing.millis { pool.invoke(new FJTask(fr, 2000)); });
println('Parallel #2: ' + Timing.millis { pool.invoke(new FJTask(fr, 2000)); });
println('Parallel #3: ' + Timing.millis { pool.invoke(new FJTask(fr, 2000)); });
println('Parallel #4: ' + Timing.millis { pool.invoke(new FJTask(fr, 2000)); });
println('Sequential #1: ' + Timing.millis { fr.doSequential(); });
println('Sequential #2: ' + Timing.millis { fr.doSequential(); });
println();

println("***** Timing for max *****");
println('Parallel #1: ' + Timing.millis { pool.invoke(new FJTask(fmax, 2000)); });
println('Parallel #2: ' + Timing.millis { pool.invoke(new FJTask(fmax, 2000)); });
println('Parallel #3: ' + Timing.millis { pool.invoke(new FJTask(fmax, 2000)); });
println('Parallel #4: ' + Timing.millis { pool.invoke(new FJTask(fmax, 2000)); });
println('Sequential #1: ' + Timing.millis { fmax.doSequential(); });
println('Sequential #2: ' + Timing.millis { fmax.doSequential(); });
println();

def strArray = new String[200_000];
final def randomStr = { int i -> return RandomString.next(ThreadLocalRandom.current(), 20) };
def fr2 = new FJFill(lower: 0, upper: strArray.length, indexable: strArray,
		     closure: randomStr);
println("***** Timing for random strings *****");
println('Parallel #1: ' + Timing.millis { pool.invoke(new FJTask(fr2, 1000)); });
println('Parallel #2: ' + Timing.millis { pool.invoke(new FJTask(fr2, 1000)); });
println('Parallel #3: ' + Timing.millis { pool.invoke(new FJTask(fr2, 1000)); });
println('Parallel #4: ' + Timing.millis { pool.invoke(new FJTask(fr2, 1000)); });
println('Sequential #1: ' + Timing.millis { fr2.doSequential(); });
println('Sequential #2: ' + Timing.millis { fr2.doSequential(); });

//Now use our versions of parallel arrays, groovy style
println();
println("***** Now try a groovier version *****");
int max;
long myTime = Timing.millis {
  max = intArray.myParallelFill(randomInt).myParallelMax(); };
myTime = Timing.millis {
  max = intArray.myParallelFill(randomInt).myParallelMax(); };
myTime = Timing.millis {
  max = intArray.myParallelFill(randomInt).myParallelMax(); };
println("Populated array and found ${max} as max in ${myTime} millis");
println("Max string: " + strArray.myParallelFill(randomStr).myParallelMax());

println('Parallel Find All #1: ' + Timing.millis { strArray.myParallelFindAll { it.startsWith('zzz'); }; });
println('Parallel Find All #2: ' + Timing.millis { strArray.myParallelFindAll { it.startsWith('zzz'); }; });
println('Parallel Find All #3: ' + Timing.millis { strArray.myParallelFindAll { it.startsWith('zzz'); }; });
println('Parallel Find All #4: ' + Timing.millis { strArray.myParallelFindAll { it.startsWith('zzz'); }; });