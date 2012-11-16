import groovyx.gpars.*;

def randomString = new RandomString();
def ary = new ArrayList(3_000_000);

long time1;
for(int i = 0; i < 3_000_000; ++i) {
  ary[i] = randomString.next(20);
}

def daveList1;
GParsPool.withPool {
  time2 = Timing.millis { daveList1 = ary.findAllParallel { it.startsWith('dave'); }; }; };

println("Parallel took ${time2} millis, ${daveList1.size()} found");

def daveList2;
long time2 = Timing.millis { daveList2 = ary.findAll { it.startsWith('dave'); }; };
println("Sequential took ${time2} millis, ${daveList2.size()} found");

