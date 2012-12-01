import groovyx.gpars.*;

//create pool and stick some place globally accessible
final def pool = GParsExecutorsPool.createPool();

//later, use pool to execute long running "fire and forget" tasks
def collector = [];
GParsExecutorsPool.withExistingPool(pool) {
  10.times {
    collector += {
      println("I'm going to sleep now...");
      sleep(1000);
      println("I'm awake now");
      return null; }.callAsync(); }; };

//make sure they have all shutdown before exiting
collector*.get();
pool.shutdown();