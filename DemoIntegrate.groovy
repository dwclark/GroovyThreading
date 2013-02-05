import groovyx.gpars.*;

Integrate.installToMath();

//demonstrate integration strategies
println("sin(x), [x: 0 -> PI]: " +
	Integrate.rectangular(0.0d, Math.PI, 2000, Math.&sin as SingleFunc));
println("sin(x), [x: 0 -> PI]: " +
	Integrate.trapezoidal(0.0d, Math.PI, 2000, Math.&sin as SingleFunc));

//demonstrate parallel integration
final int PROCESSORS = Runtime.getRuntime().availableProcessors();
def pool = GParsPool.createPool(PROCESSORS + 1);

println("sin(x), [x: 0 -> PI]: " +
	Integrate.integrateParallel(lower: 0.0d, upper: Math.PI, pool: pool,
				    steps: 2000, func: Math.&sin as SingleFunc));
println("cos(x), [x: -PI/2 -> PI/2]: " +
	Integrate.integrateParallel(lower: -(Math.PI/2), upper: Math.PI/2, pool: pool,
				    steps: 2000, func: Math.&cos as SingleFunc));

//demonstrate different function on a different interval
println("tan(x), [x: -PI/4 -> PI/4]: " +
	Integrate.integrateParallel(lower: -(Math.PI/4), upper: Math.PI/4, pool: pool,
				    steps: 2000, func: Math.&tan as SingleFunc));

//Note, even though SingleFunc is defined as @CompileStatic,
//we can still be lazy and use groovy closures
println("x*x, [x: 3 -> 7]: " +
	Integrate.integrateParallel(lower: 3, upper: 7, pool: pool,
				    steps: 2000, func: { double x -> return x * x } as SingleFunc));

//use enhanced java.util.Math class
println("sin(x), [x: 0 -> PI]: " +
	Math.integrate(lower: 0.0d, upper: Math.PI, steps: 2000,
		       func: Math.&sin as SingleFunc));
println("x*x, [x: 3 -> 7]: " +
	Math.integrateParallel(lower: 3, upper: 7, pool: pool,
			       steps: 2000, func: { double x -> return x * x } as SingleFunc));

