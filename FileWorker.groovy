import groovyx.gpars.actor.*;

public class FileWorker extends DynamicDispatchActor {
  final Closure fileProcessor;

  public FileWorker(Closure fileProcessor) {
    this.fileProcessor = fileProcessor;
  }

  public void onMessage(RqProcessDir rq) {
    final dirs = rq.dir.listFiles().findAll { it.directory; };
    if(dirs) {
      sender << new FoundSubDirs(dirs: dirs);
    }
    
    def list = rq.dir.listFiles().findAll { it.file; }.inject([]) { list, file ->
      list += new MapPair(key: file, value: fileProcessor(file)); list; };
    sender << new RsProcessDir(results: list);
  }
}