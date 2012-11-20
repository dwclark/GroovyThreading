import groovyx.gpars.dataflow.*;
import static groovyx.gpars.dataflow.Dataflow.task;
import static groovyx.gpars.dataflow.operator.PoisonPill.instance as DONE;

def announceForProcessing = new DataflowBroadcast();
def readAfterProcessing = announceForProcessing.createReadChannel();

def announceDownloads = new DataflowBroadcast();
def readDownloadsForSave = announceDownloads.createReadChannel();
def readDownloadsForAnalysis = announceDownloads.createReadChannel();

def announceSave = new DataflowBroadcast();
def readSave = announceSave.createReadChannel();

def announceAnalysis = new DataflowBroadcast();
def readAnalysis = announceAnalysis.createReadChannel();

def outputFolder = new File('/home/david/tmp/ProcessUrls');

//utilities
//abstracts common idiom of waiting until DONE is received
static whileNotDone(def channel, def closure) {
  def var;
  while(!(var = channel.val).is(DONE)) {
    closure(var);
  }
}

//can be used as a substitute for task calls if debugging is needed
static l_task(def closure) {
  task {
    try {
      closure();
    }
    catch(Throwable t) {
      println(t);
    } };
}

//process the configuration file
task {
  new File(args[0]).eachLine { line ->
    announceForProcessing << line.trim(); };
  announceForProcessing << DONE; };

//Separate download task made because nesting tasks inside of other
//tasks can lead to variables clobbering each other.  Breaking nested
//task creation into separate functions solves this problem by making
//variables immutable (since they are now bound to the function parameters).
def makeDownloadTask(final def announceChannel, final def url) {
  return task {
    def download = url.toURL().text;
    announceChannel << new MapPair(key: url, value: download); }; };

//download the files
task {
  //def url;
  def all = [];
  def doStuff = { url -> all.add(makeDownloadTask(announceDownloads, url)); };

  whileNotDone(readAfterProcessing, doStuff);
  all.each { def val = it.val; };
  announceDownloads << DONE; };

//save the files
task {
  def doStuff = { toSave ->
    def target = File.createTempFile('groovy', '.html', outputFolder);
    target.withWriter { writer -> writer.write(toSave.value); };
    announceSave << new MapPair(key: toSave.key, value: target); };

  whileNotDone(readDownloadsForSave, doStuff);
  announceSave << DONE; };

//analyze the files
task {
  def doStuff = { toAnalyze ->
    def txt = toAnalyze.value.toLowerCase();
    def indexes = [];
    def nextIndex = txt.indexOf('groovy');
    while(nextIndex != -1) {
      indexes += nextIndex;
      nextIndex = txt.indexOf('groovy', nextIndex+1);
    }
    
    announceAnalysis << new MapPair(key: toAnalyze.key, value: indexes); };

  whileNotDone(readDownloadsForAnalysis, doStuff);
  announceAnalysis << DONE; };

def finish = task {
  def allAnalysisPairs = [];
  whileNotDone(readAnalysis, { pair -> allAnalysisPairs += pair; });

  def analysisFile = new File(outputFolder, 'analysis.csv');
  def newline = System.getProperty('line.separator');

  analysisFile.withWriter { writer ->
    def doStuff = { filePair ->
      def analysisPair = allAnalysisPairs.find { analysisPair -> analysisPair.key == filePair.key; };
      def entries = [ filePair.key, filePair.value.path, analysisPair.value ];
      def line = entries.collect { entry -> "\"${entry}\""; }.join(',');
      writer << line + newline; };
    whileNotDone(readSave, doStuff); }; };

finish.join();
