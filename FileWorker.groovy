import groovyx.gpars.actor.*;

public class FileWorker extends DynamicDispatchActor {
  final def fileProcessor;

  public void onMessage(RqDirScan rq) {
    final dirs = rq.dir.listFiles().findAll { it.directory; };
    final files = rq.dir.listFiles().findAll { it.file; };
    sender.send(new RsDirScan(dirs: dirs, files: files));
  }

  public void onMessage(RqProcessFile rq) {
    sender.send(fileProcessor(rq.file));
  }
}