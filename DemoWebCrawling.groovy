import groovyx.gpars.dataflow.*;

final def DONE = new Object();

def announceForProcessing = new DataflowBroadcast();
def readAfterProcessing = announceForProcessing.createReadChannel();

def announceDownloads = new DataflowBroadCast();
def readDownloadsForSave = announceDownloads.createReadChannel();
def readDownloadsForAnalysis = announceDownloads.createReadChannel();

def announceSave = new DataflowBroadcast();
def readSave = announceSave.createReadChannel();

def announceAnalysis = new DataflowBroadcast();
def readAnalysis = announceAnalysis.createReadChannel();

def outputFolder = new File('/home/david/tmp/ProcessUrls');

//process the configuration file
task {
  new File(args[0]).eachLine { line ->
    announceForProcessing << line; };
  announceForProcessing << DONE;
};

//download the files
task {
  def toProcess = readAfterProcessing.val;
  def allDownloads = [];
  while(!toProcess.is(DONE)) {
    allDownloads += task { announceDownloads << new MapPair(toProcess, toProcess.toURL().text); };
    toProcess = readAfterProcessing.val;
  }

  allDownloads*.join();
  announceDownloads << DONE; };

//save the files
task { 
  def toSave = readDownloadsForSave.val;
  while(!toSave.is(DONE)) {
    def target = File.createTempFile('', '.html', outputFolder);
    target.withWriter { writer << toSave.value; };
    announceSave << new MapPair(toSave.key, target);
    toSave = readDownloadsForSave.val;
  }

  announceSave << DONE; };

//analyze the files
task {
  def toAnalyze = readDownloadsForAnalysis.val;
  while(!toAnalyze.is(DONE)) {
    def txt = toAnalyze.value.toLowerCase();
    def indexes = [];
    def nextIndex = txt.indexOf('groovy');
    while(nextIndex != -1) {
      indexes += nextIndex;
      nextIndex = txt.indexOf('groovy', nextIndex+1);
    }

    announceAnalysis << new MapPair(toAnalyze.key, indexes);
  }

  announceAnalysis << DONE; };

def finish = task {
  def allAnalysis = [];
  def allFiles = [];

  def analysis = readAnalysis.val;
  while(!analysis.is(DONE)) {
    allAnalysis += analysis;
    analysis = readAnalysis.val;
  }
  
  //analysis is complete, write everything
  def analysisFile = new File(outputFolder, 'analysis.csv');
  def newline = System.getProperty('line.separator');
  analysisFile.withWriter { writer ->
    def savedFile = readSave.val;
    while(!savedFile.is(DONE)) {
      analysis = allAnalysis.find { a -> a.key == savedFile.key; };
      def entries = [ savedFile.key, savedFile.value, a.value ];
      def line = entries.collect { entry -> "\"${entry}\""; }.join(',');
      writer << line + newline;
      savedFile = readSave.val;
    } };
};

finish.join();