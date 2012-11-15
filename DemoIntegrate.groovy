import groovyx.gpars.*;

Integrate.installToMath();

//demonstrate integration strategies
println(Integrate.rectangular(0.0d, Math.PI, 2000, Math.&sin as SingleFunc));
println(Integrate.trapezoidal(0.0d, Math.PI, 2000, Math.&sin as SingleFunc));

//demonstrate parallel integration
final int PROCESSORS = Runtime.getRuntime().availableProcessors();
def pool = GParsPool.createPool(PROCESSORS + 1);
println(Integrate.integrateParallel(lower: 0.0d, upper: Math.PI, pool: pool,
				    steps: 2000, func: Math.&sin as SingleFunc));
println(Integrate.integrateParallel(lower: -(Math.PI/2), upper: Math.PI/2, pool: pool,
				    steps: 2000, func: Math.&cos as SingleFunc));
println(Integrate.integrateParallel(lower: -(Math.PI/4), upper: Math.PI/4, pool: pool,
				    steps: 2000, func: Math.&tan as SingleFunc));
println(Integrate.integrateParallel(lower: 3, upper: 7, pool: pool,
				    steps: 2000, func: { double x -> return x * x } as SingleFunc));

//use enhanced java.util.Math class
println(Math.integrate(lower: 0.0d, upper: Math.PI, steps: 2000,
		       func: Math.&sin as SingleFunc));
println(Math.integrateParallel(lower: 3, upper: 7, pool: pool,
			       steps: 2000, func: { double x -> return x * x } as SingleFunc));

