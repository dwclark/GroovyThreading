import java.util.concurrent.*;

public class TaskDivider {
  protected final int lower;
  protected final int upper;

  public TaskDivider(Map args) {
    this.lower = args.lower;
    this.upper = args.upper;
  }

  public int getSize() { return upper - lower; }
  public int getMidpoint() { return lower + (size / 2); }
  
  public Map addLowerArgs(Map args) {
    args['lower'] = lower;
    args['upper'] = midpoint,
    return args;
  }

  public Map addUpperArgs(Map args) {
    args.lower = midpoint;
    args.upper = upper;
  }
}

public class FillRandom {
  private final int lower;
  private final int upper;
  private final def generator;
  private def indexable;

  public FillRandom(Map args) {
    lower = args.lower;
    upper = args.upper;
    indexable = args.indexable;
    generator = args.generator;
  }

  public int getSize() { return upper - lower; }
  public int getMidpoint() { return lower + (size / 2); }

  public void doSequential() {
    for(int i = lower; i < upper; ++i) {
      indexable[i] = generator();
    }
  }

  public FillRandom doUpper() {
    return new FillRandom(lower: midpoint, upper: upper,
			  generator: generator, indexable: indexable);
  }

  public FillRandom doLower() {
    return new FillRandom(lower: lower, upper: midpoint,
			  generator: generator, indexable: indexable);
  }
}

public class FillRandomFJ extends RecursiveAction {
  private final def problem;
  private final int threshold;
  
  public FillRandomFJ(FillRandom problem, final int threshold) {
    this.problem = problem;
    this.threshold = threshold;
  }

  protected void compute() {
    if(problem.size < threshold) {
      problem.doSequential();
    }
    else {
      invokeAll(new FillRandomFJ(problem.doLower(), threshold),
		new FillRandomFJ(problem.doUpper(), threshold));
    }
  }
}

def pool = new ForkJoinPool();
def fr = new FillRandom(lower: 0, upper: 300_000, indexable: new int[300_000],
			generator: { -> ThreadLocalRandom.current().nextInt(1, 200_001); });

//Note, the start up times for the fork/join framework are horrendous
//The nature of the speed up suggests that this speedup is due to
//hotspot optimizations (since replacing the generator resets run times
//to very slow).  This suggests that the Fork/Join framework should only
//be used when the following conditions apply
//1) You are dealing with large datasets
//2) The design constraints of Fork/Join are kept (no I/O, no synchronization)
//3) The code is on a hot path in your application (otherwise hotspot will not speed it up)
//4) The code is run in server mode (client mode usually disables hotspot)
println("Timing for random ints");
println(Timing.millis { pool.invoke(new FillRandomFJ(fr, 500)); });
println(Timing.millis { pool.invoke(new FillRandomFJ(fr, 1000)); });
println(Timing.millis { pool.invoke(new FillRandomFJ(fr, 2000)); });
println(Timing.millis { pool.invoke(new FillRandomFJ(fr, 4000)); });
println(Timing.millis { fr.doSequential(); });

def fr2 = new FillRandom(lower: 0, upper: 100_000, indexable: new String[100_000],
			 generator: { -> return RandomString.next(ThreadLocalRandom.current(), 20); });
println("Timing for random strings");
println(Timing.millis { pool.invoke(new FillRandomFJ(fr2, 1000)); });
println(Timing.millis { pool.invoke(new FillRandomFJ(fr2, 1000)); });
println(Timing.millis { pool.invoke(new FillRandomFJ(fr2, 1000)); });
println(Timing.millis { pool.invoke(new FillRandomFJ(fr2, 1000)); });
println(Timing.millis { fr2.doSequential(); });
println(Timing.millis { fr2.doSequential(); });
