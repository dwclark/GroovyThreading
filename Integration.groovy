import groovy.transform.CompileStatic;
import groovyx.gpars.*;
import groovyx.gpars.dataflow.DataflowVariable;
import groovyx.gpars.group.*;
import static groovyx.gpars.dataflow.Dataflow.task;

public interface SingleFunc {
  double call(double arg);
}

public enum IntegrationStrategy { RECTANGULAR, TRAPEZOIDAL };

public class Integrate {

  @CompileStatic
  public static double rectangular(double lower, double delta, SingleFunc func) {
    return func.call(lower) * delta;
  }

  @CompileStatic
  public static double trapezoidal(double lowerHeight, double upperHeight, double delta) {
    return delta * ((lowerHeight + upperHeight) / 2)
  }

  @CompileStatic
  public static double execute(double lower, double upper, int steps,
			       SingleFunc func,
			       IntegrationStrategy strategy = IntegrationStrategy.RECTANGULAR) {
    double delta = (upper - lower) / steps;
    double sum = 0.0d;
    double nextLower = lower;

    if(strategy == IntegrationStrategy.RECTANGULAR) {
      for(int i = 0; i < steps; ++i) {
	sum += rectangular(nextLower, delta, func);
	nextLower += delta;
      }

      return sum;
    }
    
    if(strategy == IntegrationStrategy.TRAPEZOIDAL) {
      double lowerHeight = func.call(nextLower);
      double upperHeight = func.call(nextLower + delta);
      for(int i = 0; i < steps; ++i) {
	sum += trapezoidal(lowerHeight, upperHeight, delta);
	nextLower += delta;
	lowerHeight = upperHeight;
	upperHeight = func.call(nextLower + delta);
      }

      return sum;
    }
  }

  public static double integrate(def args) {
    return execute(args.lower, args.upper, args.steps, args.func,
		   args.strategy ?: IntegrationStrategy.RECTANGULAR);
  }

  public static double integrateParallel(def args) {
    //final def pool = args.pool;
    final int numTasks = 8; //pool.parallelism;

    double lower = args.lower;
    double upper = args.upper;
    int totalSteps = args.steps;
    SingleFunc func = args.func;
    IntegrationStrategy strategy = args.strategy ?: IntegrationStrategy.RECTANGULAR;
    double intervalSize = (upper - lower) / numTasks;
    int numSteps = totalSteps / numTasks;
    double myLower = lower;
    double sum = 0.0d;

    int stepsSoFar = 0;
    def actual = [];
    def listVars = (0..<(numTasks-1)).collect {
      //This next line took me about 2 hours to figure out
      //Just using myLower will result on random results.
      //The problem is that if myLower is used, the task task may
      //not get around to bind the variables to the execute call 
      //until AFTER myLower has changed.  To prevent this, add
      //localMyOwner and the lexical scoping will guarantee that
      //the task task will always have access to the correct variable
      double localMyLower = myLower;
      def var = task {
	execute(localMyLower, localMyLower + intervalSize, numSteps, func, strategy); };
      myLower = myLower + intervalSize;
      stepsSoFar += numSteps; 
      return var; };
    
    listVars.add(task { execute(myLower, upper, totalSteps - stepsSoFar, func, strategy); });
    return listVars.sum { it.val; };
  }
}

final int NUM_PROCS = Runtime.getRuntime().availableProcessors();
final def pool = GParsPool.createPool(NUM_PROCS);
println(Integrate.integrateParallel(lower: 0.0d, upper: Math.PI,
				    steps: 2000, func: Math.&sin as SingleFunc));

