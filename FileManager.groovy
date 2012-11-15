import groovyx.gpars.actor.*;
import java.util.concurrent.CountDownLatch;

public class FileManager extends DynamicDispatchActor {
  TaskManager processDirRqs = new TaskManager();
  Stack workers = new Stack();
  
  final def accumulator;
  final CountDownLatch terminationSignal = new CountDownLatch(1);
  
  public FileManager(Map args) {
    this.accumulator = args.accumulator;
    processDirRqs.add(new RqProcessDir(dir: args.startAt));
    (0..<args.numWorkers).each { 
      workers.push(new FileWorker(this, args.fileProcessor).start()); };
  }

  private void runTasks(boolean canStop = false){
    while(workers && processDirRqs.hasTasks) {
      workers.pop() << processDirRqs.next();
    }
    
    if(processDirRqs.finished && canStop) {
      stopWorkers();
    }
  }
  
  private void stopWorkers() {
    terminationSignal.countDown();
    workers.each { it.stop(); };
    stop();
  }

  @Override
  public void afterStart() { runTasks(); }

  public void onMessage(FoundSubDirs msg) {
    msg.dirs.each { processDirRqs.add(new RqProcessDir(it)); };
    runTasks();
  }

  public void onMessage(RsProcessDir msg) {
    workers.push(sender);
    runTasks();

    msg.results.each { def pair ->
      accumulator(pair.key, pair.value); };
    processDirRqs.completedTask();

    runTasks(true);
  }

  public static Object eachFileParallel(def args) {
    final def numWorkers = args.numWorkers ?: Runtime.getRuntime().availableProcessors() * 10;
    final def accumulator = args.accumulator ?: { Object obj -> };

    final def manager = new FileManager(startAt: args.dir, accumulator: accumulator,
					numWorkers: numWorkers, 
					fileProcessor: args.fileProcessor).start();
    manager.terminationSignal.await();
  }

  public static void installToFile() {
    java.io.File.metaClass.eachFileParallel = { def args ->
      final dir = delegate;
      if(!dir.directory) {
	throw new UnsupportedOperationException('eachFileParallel must start in a directory');
      }

      def newArgs = new LinkedHashMap(args);
      newArgs['dir'] = dir;
      FileManager.eachFileParallel(newArgs); };
  }
}