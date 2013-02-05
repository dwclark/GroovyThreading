import groovy.transform.CompileStatic;
import groovyx.gpars.*;
import groovyx.gpars.dataflow.DataflowVariable;
import static groovyx.gpars.dataflow.Dataflow.task;

public class Integrate {
  
  @CompileStatic
  public static double calculateDelta(final double lower, final double upper,
				      final int steps) {
    return (upper - lower) / steps;
  }

  @CompileStatic
  public static double rectangular(final double lower, final double upper,
				   final int steps, final SingleFunc func) {
    final double delta = calculateDelta(lower, upper, steps);
    double nextLower = lower;
    double sum = 0.0d;
    for(int i = 0; i < steps; ++i) {
      sum += (func.call(nextLower) * delta);
      nextLower += delta;
    }

    return sum;
  }

  @CompileStatic
  public static double trapezoidal(final double lower, final double upper,
				   final int steps, final SingleFunc func) {
    final double delta = calculateDelta(lower, upper, steps);
    double nextLower = lower;
    double sum = 0.0d;

    double lowerHeight = func.call(nextLower);
    double upperHeight = func.call(nextLower + delta);
    for(int i = 0; i < steps; ++i) {
      sum += ((upperHeight + lowerHeight) * delta) / 2
      nextLower += delta;
      lowerHeight = upperHeight;
      upperHeight = func.call(nextLower + delta);
    }

    return sum;
  }

  @CompileStatic
  public static double execute(final double lower, final double upper, final int steps,
			       final SingleFunc func,
			       final IntegrationStrategy strategy = IntegrationStrategy.RECTANGULAR) {

    if(strategy == IntegrationStrategy.RECTANGULAR) {
      return rectangular(lower, upper, steps, func);
    }
    
    if(strategy == IntegrationStrategy.TRAPEZOIDAL) {
      return trapezoidal(lower, upper, steps, func);
    }

    throw new UnsupportedOperationException('Did not receive a valid strategy');
  }

  public static double integrate(def args) {
    return execute(args.lower, args.upper, args.steps, args.func,
		   args.strategy ?: IntegrationStrategy.RECTANGULAR);
  }

  public static double integrateParallel(def args) {
    final def pool = args.pool ?: GParsPool.retrieveCurrentPool();
    final int numTasks = Runtime.getRuntime().availableProcessors();

    final double lower = args.lower;
    final double upper = args.upper;
    final int totalSteps = args.steps;
    final SingleFunc func = args.func;
    final IntegrationStrategy strategy = args.strategy ?: IntegrationStrategy.RECTANGULAR;
    final double intervalSize = (upper - lower) / numTasks;
    final int intervalSteps = totalSteps / numTasks;
    
    GParsPool.withExistingPool pool, {
      def listVars = (0..<(numTasks-1)).inject([]) { list, index ->
	list += task {
	  final double myLower = lower + (index * intervalSize);
	  execute(myLower, myLower + intervalSize, intervalSteps, func, strategy); };
	return list; };
      
      //Handle last interval separately.  The final interval may be a different
      //size than the previous intervals because of rounding errors.  Make sure
      //intregration ends exactly at upper boundary.
      listVars += task {
	final double myLower = lower + ((numTasks - 1) * intervalSize);
	final int myNumSteps = totalSteps - (intervalSteps * (numTasks - 1));
	execute(myLower, upper, myNumSteps, func, strategy); };

      return listVars.sum { it.val; }; };
  }

  public static void installToMath() {
    java.lang.Math.metaClass.static.integrate = Integrate.&integrate;
    java.lang.Math.metaClass.static.integrateParallel = Integrate.&integrateParallel;
  }
}
