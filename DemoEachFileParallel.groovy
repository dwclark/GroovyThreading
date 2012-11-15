FileManager.installToFile();

def dir = new File('/home/david/Development');

long size1 = 0;
long time1 = Timing.millis { dir.eachFileParallel(fileProcessor: { File file -> return file.length(); },
						  accumulator: { def file, def result -> 
						    size1 += result; return size1; }); };
println("Multi Threaded: size: ${size1}, time: ${time1}");

long size2 = 0;
long time2 = Timing.millis { dir.eachFileRecurse { File file -> if(file.file) size2 += file.length(); }; };
println("Single Threaded size: ${size2}, time: ${time2}");


int hasGroovy1 = 0;
long time;

def fileProcessor = { File file -> 
  if(file.text.indexOf('groovy') != -1) return 1;
  else return 0; };
long time1 = Timing.millis { dir.eachFileParallel(fileProcessor: fileProcessor,
						  accumulator: { def file, def result -> hasGroovy1 += result; }) };
println("Multi Threaded: time: ${time1}, hasGroovy: ${hasGroovy1}");

int hasGroovy2 = 0;
long time2 = Timing.millis { dir.eachFileRecurse { File file ->
  if(file.file && file.text.indexOf('groovy') != -1) ++hasGroovy2; }; };
println("Single Threaded: time: ${time2}, hasGroovy: ${hasGroovy2}");
