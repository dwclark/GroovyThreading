import java.util.concurrent.locks.*;

ExpandoMetaClass.enableGlobally();

//The main point of the with* enhancements is to make sure
//that a scope always releases a lock.  In Java (and Groovy) this
//is always done when using the synchronized keyword, but is
//not automatic for Lock objects.  These enhancements allow
//you to use Lock objects in exactly the same way as you
//would use synchronized, but also offer the advantages of Lock
//objects (timeouts, encapsulation, lock sharing, and enhanced Conditions
//which are easier to use in signalling situations, etc.)
Lock.metaClass.withLock = { def closure ->
  final lock = delegate;
  lock.lock();
  try {
    return closure();
  }
  finally { lock.unlock(); } };

Lock.metaClass.withLockInterruptibly = { def closure ->
  final lock = delegate;
  lock.lockInterruptibly();
  try {
    return closure();
  }
  finally { lock.unlock(); } };

Lock.metaClass.withTryLock = { def closure ->
  final lock = delegate;
  def locked = lock.tryLock();
  if(locked) {
    try {
      return closure();
    }
    finally { lock.unlock(); }
  }
  else return false; };

Lock.metaClass.withTryLock = { 
  long time, java.util.concurrent.TimeUnit unit, def closure ->
    final lock = delegate;
    def locked = lock.tryLock(time, unit);
    if(locked) {
      try {
	return closure();
      }
      finally { lock.unlock(); }
    }
    else return false; };

Lock.metaClass.withTryLockFail = { def onFail, def closure ->
  final lock = delegate;
  final locked = lock.tryLock();
  if(locked) {
    try {
      return closure();
    }
    finally { lock.unlock(); }
  }
  else {
    onFail();
    return false;
  } };

//The point of the Condition enhancments is to make sure that the
//Java object methods wait, notify, and notifyAll are NOT used.
//When using conditions you need to use await, signal, and 
//signalAll methods.  Lock objects really should disable these methods,
//but there is no way to guarantee this with Java.  With Groovy, it
//is easy to have the methods throw Exceptions and fail early in
//testing.
Condition.metaClass.wait = { ->
  def msg = 'Do not use the wait() method when using a Condition object, ' +
    'use the await() method instead';
    throw new UnsupportedOperationException(msg); };

Condition.metaClass.wait = { long timeout ->
  def msg = 'Do not use the wait(long) method when using a Condition object, ' +
    'use the await(long) method instead';
    throw new UnsupportedOperationException(msg); };

Condition.metaClass.wait = { long timeout, int nanos ->
  def msg = 'Do not use the wait(long, int) method when using a Condition object, ' +
    'use the await(long, TimeUnit) method instead';
    throw new UnsupportedOperationException(msg); };

Condition.metaClass.notify = { ->
  def msg = 'Do not use the notify() method when using a Condition object, ' +
    'use the signal() method instead';
    throw new UnsupportedOperationException(msg); };

Condition.metaClass.notifyAll = { ->
  def msg = 'Do not use the notifyAll() method when using a Condition object, ' +
    'use the signalAll() method instead';
    throw new UnsupportedOperationException(msg); };

//tests to make sure that the lock enhancments
//still allow for reentrant locking.
public static testWithLock() {
  def counter = 0;
  def lock = new ReentrantLock();
  def t1 = Thread.start {
    def t2;
    lock.withLock {
      assert(0 == counter);
      t2 = Thread.start {
	lock.withLock {
	  assert(1 == counter); }; };
      ++counter; };
    t2.join(); };
  t1.join();
}

//tests to make sure that withTryLock fails when a
//lock is held by another thread.
public static testWithTryLock() {
  def counter = 0;
  def lock = new ReentrantLock();
  def t1 = Thread.start {
    lock.withTryLock {
      def t2 = Thread.start {
	assert(!lock.withTryLock({-> Assert.isTrue(false); })); };
      t2.join();
      ++counter; }; };
  
  t1.join();
  
  lock.withTryLock { 
    ++counter;
    assert(2 == counter); };
}

//Calls the wrong methods on a Condition object to make
//sure that this fails.
public static testConditions() {
  def lock = new ReentrantLock();
  def condition = lock.newCondition();
  lock.withLock { println "Holding lock..."; };
  condition.wait(100,100);
}

testWithLock();
testWithTryLock();
//testConditions();