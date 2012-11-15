import java.security.MessageDigest;

FileManager.installToFile();

def dir = new File('/home/david/Development');

long size1 = 0;
long time1 = Timing.millis { 
  dir.eachFileParallel(fileProcessor: { File file -> return file.length(); },
		       accumulator: { def file, def result -> 
			 size1 += result; return size1; }); };
println("Multi Threaded: size: ${size1}, time: ${time1}");

long size2 = 0;
long time2 = Timing.millis { 
  dir.eachFileRecurse { File file -> if(file.file) size2 += file.length(); }; };
println("Single Threaded size: ${size2}, time: ${time2}");


def hashFile(File file) {
  def shaDigester = MessageDigest.getInstance('SHA-1');
  shaDigester.update(file.bytes)
  return shaDigester.digest().encodeHex().toString().toUpperCase();
}

def map1 = [:];
def time3 = Timing.millis {
  dir.eachFileParallel(fileProcessor: { File file -> return hashFile(file); },
		       accumulator: { def file, def result -> map1[file.path] = result }); };
println("Multi Threaded SHA-1 hash in ${time3} millis");

def map2 = [:];
def time4 = Timing.millis {
  dir.eachFileRecurse { File file -> if(file.file) map2[file.path] = hashFile(file); }; };
println("Single Threaded SHA-1 hash in ${time4} millis");

def hasNewLines(File file) {
  def is = file.newInputStream();
  int data = 0;
  while((data = is.read()) != -1) {
    if(data == 10) {
      is.close();
      return true;
    }
  }
  
  is.close();
  return false;
}

def numNewLines1 = 0;
def time5 = Timing.millis {
  dir.eachFileParallel(fileProcessor: { File file -> return hasNewLines(file); },
		       accumulator: { def file, def result -> if(result) ++numNewLines1; }) };
println("Multi Threaded Total files with new lines: ${numNewLines1}, time: ${time5}");

def numNewLines2 = 0;
def time6 = Timing.millis {
  dir.eachFileRecurse { File file -> if(file.file && hasNewLines(file)) ++numNewLines2; }; };
println("Single Threaded Total files with new lines: ${numNewLines2}, time: ${time6}");
