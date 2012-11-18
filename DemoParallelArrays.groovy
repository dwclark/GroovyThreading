import groovyx.gpars.*;
import groovyx.gpars.extra166y.Ops;

def randomString = new RandomString();
def ary = new ArrayList(200_000);

for(int i = 0; i < 200_000; ++i) {
  ary[i] = randomString.next(20);
}

def zzzListP, zzzListPA;
def filter = { it.startsWith('zzz'); };
final def o = System.out;
int pSize, paSize;
GParsPool.withPool {
  o.format("Parallel Cols #1: %,21d\n", Timing.nanos { zzzListP = ary.findAllParallel(filter); });
  o.format("Parallel Cols #2: %,21d\n", Timing.nanos { zzzListP = ary.findAllParallel(filter); });
  o.format("Parallel Cols #3: %,21d\n", Timing.nanos { zzzListP = ary.findAllParallel(filter); });
  o.format("Parallel Cols #4: %,21d\n", Timing.nanos { zzzListP = ary.findAllParallel(filter); });

  def parallel = ary.parallel
  o.format("GPars parallel #1: %,20d\n", Timing.nanos { zzzListP = parallel.filter(filter); });
  o.format("GPars parallel #2: %,20d\n", Timing.nanos { zzzListP = parallel.filter(filter); });
  o.format("GPars parallel #3: %,20d\n", Timing.nanos { zzzListP = parallel.filter(filter); });
  o.format("GPars parallel #4: %,20d\n", Timing.nanos { zzzListP = parallel.filter(filter); });
  pSize = zzzListP.size();

  def pary = ary.parallelArray;
  def opsFilter = filter as Ops.Predicate;
  o.format("Parallel Arrays #1: %,19d\n", Timing.nanos { zzzListPA = pary.withFilter(opsFilter); });
  o.format("Parallel Arrays #2: %,19d\n", Timing.nanos { zzzListPA = pary.withFilter(opsFilter); });
  o.format("Parallel Arrays #3: %,19d\n", Timing.nanos { zzzListPA = pary.withFilter(opsFilter); });
  o.format("Parallel Arrays #1: %,19d\n", Timing.nanos { zzzListPA = pary.withFilter(opsFilter); });
  paSize = zzzListPA.size(); };
  


def zzzListS;
o.format("Sequential #1: %,24d\n", Timing.nanos { zzzListS = ary.findAll(filter); });
o.format("Sequential #2: %,24d\n", Timing.nanos { zzzListS = ary.findAll(filter); });
o.format("Sequential #3: %,24d\n", Timing.nanos { zzzListS = ary.findAll(filter); });
o.format("Sequential #4: %,24d\n", Timing.nanos { zzzListS = ary.findAll(filter); });

assert(pSize == paSize);
assert(paSize == zzzListS.size());

