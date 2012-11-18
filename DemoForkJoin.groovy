import java.util.concurrent.*;
import groovy.transform.CompileStatic;

@CompileStatic
public abstract class TaskDivider {
  protected final int lower;
  protected final int upper;
  protected final def indexable;
  protected final def closure;

  public TaskDivider(Map args) {
    this.lower = (int) args.get('lower');
    this.upper = (int) args.get('upper');
    this.indexable = args.get('indexable');
    this.closure = args.get('closure');
  }

  public int getSize() { return upper - lower; }
  public int getMidpoint() { return lower + ((int) (size / 2)); }
  
  public Map getLowerArgs() {
    return [ lower: lower, upper: midpoint, indexable: indexable, closure: closure ];
  }

  public Map getUpperArgs() {
    return [ lower: midpoint, upper: upper, indexable: indexable, closure: closure ];
  }
  
  public TaskDivider getUpperTask() {
    (TaskDivider) getClass().getDeclaredConstructor(Map).newInstance(upperArgs);
  }

  public TaskDivider getLowerTask() {
    (TaskDivider) getClass().getDeclaredConstructor(Map).newInstance(lowerArgs);
  }

  public abstract Object doSequential();
  public void joinTasks(TaskDivider lower, TaskDivider upper) { }
}

public class FillRandom extends TaskDivider {
  public FillRandom(Map args) { super(args); }

  public Object doSequential() {
    for(int i = lower; i < upper; ++i) {
      indexable[i] = closure();
    }

    return null;
  }
}

public class FindMax extends TaskDivider {
  public FindMax(Map args) { super(args); }

  def max;

  public Object doSequential() {
    max = indexable[lower];
    for(int i = (lower+1); i < upper; ++i) {
      def toCmp = indexable[i];
      if(max < toCmp) max = toCmp;
    }
    
    return max;
  }

  @Override public void joinTasks(TaskDivider lowerTask, TaskDivider upperTask) {
    if(lowerTask.max < upperTask.max) max = upperTask.max;
    else max = lowerTask.max;
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

def pool = new ForkJoinPool();
def fr = new FillRandom(lower: 0, upper: 300_000, indexable: new int[300_000],
			closure: { -> ThreadLocalRandom.current().nextInt(1, 500_001); });
def fmax = new FindMax(lower: 0, upper: 300_000, indexable: fr.indexable);

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
assert(fmax.max == fmax.doSequential());
println();

def fr2 = new FillRandom(lower: 0, upper: 100_000, indexable: new String[100_000],
			 closure: { -> return RandomString.next(ThreadLocalRandom.current(), 20); });
println("Timing for random strings");
println('Parallel #1: ' + Timing.millis { pool.invoke(new FJTask(fr2, 1000)); });
println('Parallel #2: ' + Timing.millis { pool.invoke(new FJTask(fr2, 1000)); });
println('Parallel #3: ' + Timing.millis { pool.invoke(new FJTask(fr2, 1000)); });
println('Parallel #4: ' + Timing.millis { pool.invoke(new FJTask(fr2, 1000)); });
println('Sequential #1: ' + Timing.millis { fr2.doSequential(); });
println('Sequential #2: ' + Timing.millis { fr2.doSequential(); });
