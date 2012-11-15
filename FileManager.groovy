import groovyx.gpars.actor.*;

public class FileManager extends DynamicDispatchActor {
  TaskManager processFileRqs = new TaskManager();
  TaskManager dirScanRqs = new TaskManager();
  Stack workers = new Stack();
  
  final def accumulator;

  public void setStartAt(def dir) {
    dirScanRqs.add(new RqDirScan(dir));
  }

  private void runTasks(){
    while(workers && processFileRqs.hasTasks) {
      workers.pop() << processFileRqs.next();
    }

    while(workers && dirScanRqs.hasTasks) {
      workers.pop() << dirScanRqs.next();
    }
    
    if(dirScanRqs.finished && processFileRqs.finished) {
      stopWorkers();
    }
  }
  
  private void stopWorkers() {
    workers.each { it.stop(); };
    stop();
  }

  @Override
  public void afterStart() { runTasks(); }

  public void onMessage(RsDirScan msg) {
    msg.dirs.each { dirScanRqs.add(new RqDirScan(it)); };
    dirScanRqs.completedTask();
    msg.files.each { processFileRqs.add(new RqProcessFile(file: it)); };
    workers.push(sender);
    runTasks();
  }

  public void onMessage(Object obj) {
    accumulator(obj);
    processFileRqs.completedTask();
    workers.push(sender);
    runTasks();
  }

  public static void eachDirParallel(def args) {
    final def dir = args.dir;
    final def numWorkers = args.numWorkers ?: Runtime.getRuntime().availableProcessors() * 3;
    final def fileProcessor = args.fileProcessor;
    final def accumulator = args.accumulator ?: { Object obj -> };

    final def manager = new FileManager(startAt: dir, accumulator: accumulator);
    final def waitOn = [];
    (0..<numWorkers).each {
      final def worker = new FileWorker(fileProcessor: fileProcessor).start();
      waitOn += worker;
      manager.workers.push(worker); };
    waitOn += manager.start();
    waitOn*.join();
  }

  public static void installToFile() {
    java.io.File.metaClass.eachDirParallel = { def args ->
      final dir = delegate;
      if(!theDir.directory) {
	throw new UnsupportedOperationException('eachDirParallel must start in a directory');
      }

      def newArgs = new LinkedHashMap(args);
      newArgs['dir'] = dir;
      FileManager.eachDirParallel(newArgs); };
  }
}