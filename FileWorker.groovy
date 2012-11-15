import groovyx.gpars.actor.*;

public class FileWorker extends DynamicDispatchActor {
  final Closure fileProcessor;
  final FileManager manager;

  public FileWorker(FileManager manager, Closure fileProcessor) {
    this.manager = manager;
    this.fileProcessor = fileProcessor;
  }

  public void onMessage(RqProcessDir rq) {
    final dirs = rq.dir.listFiles().findAll { it.directory; };
    if(dirs) {
      manager << new FoundSubDirs(dirs: dirs);
    }
    
    def list = rq.dir.listFiles().findAll { it.file; }.inject([]) { list, file ->
      list += new MapPair(key: file, value: fileProcessor(file)); list; };
    manager << new RsProcessDir(results: list);
  }
}