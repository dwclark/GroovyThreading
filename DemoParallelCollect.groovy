import java.util.concurrent.*;

final def NUM_PROCS = Runtime.getRuntime().availableProcessors();
final def IO_NUM_PROCS = NUM_PROCS * 3;
final def CPU_NUM_PROCS = NUM_PROCS;

ArrayList.metaClass.static.parallelCollect = { pool, closure ->
  def theList = delegate;
  def futureList = theList.collect { pool.submit(closure.curry(it) as Callable); };
  futureList.collect { it.get(); }; };

final def myPool = Executors.newFixedThreadPool(IO_NUM_PROCS);
final def myList = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ];
println(myList.parallelCollect(myPool, { sleep(1000); it + 10; }));

myPool.shutdown();