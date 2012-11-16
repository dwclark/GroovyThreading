import groovyx.gpars.GParsPool;
import groovyx.gpars.ParallelEnhancer;

def waitTimesSerial = (0..<9).collect { 100; };
def waitTimesParallel = (0..<9).collect { 100; };

//set up the parallel framework so that measurements only
//reflect the algorithm, not set up and tear down times
//call eachParallel once to give the pool time to start up
//pool start up time appears to take around 100ms
ParallelEnhancer.enhanceInstance(waitTimesParallel);
waitTimesParallel.eachParallel { sleep(it); };

long serialTime = Timing.millis {
  waitTimesSerial.each { sleep(it); }; };

long parallelTime = Timing.millis {
  waitTimesParallel.eachParallel { sleep(it); }; };

println("Serial Time: ${serialTime}");
println("Parallel Time: ${parallelTime}");





