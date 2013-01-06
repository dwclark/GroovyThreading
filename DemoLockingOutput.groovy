import groovy.transform.WithReadLock as WithReadLock
import groovy.transform.Immutable as Immutable
import groovy.transform.Synchronized as Synchronized
import groovy.transform.WithWriteLock as WithWriteLock

public class script1357505293271 extends groovy.lang.Script { 

    private static org.codehaus.groovy.reflection.ClassInfo $staticClassInfo 
    public static transient boolean __$stMC 
    public static long __timeStamp 
    public static long __timeStamp__239_neverHappen1357505293337 

    public script1357505293271() {
    }

    public script1357505293271(groovy.lang.Binding context) {
        super.setBinding(context)
    }

    public static void main(java.lang.String[] args) {
        org.codehaus.groovy.runtime.InvokerHelper.runScript(script1357505293271, args)
    }

    public java.lang.Object run() {
        java.lang.Integer NUM_PROCS = java.lang.Runtime.getRuntime().availableProcessors()
        java.lang.Long SLEEP_TIME = 10000
        java.lang.Object sMap = new MapSynch()
        sMapTests = (0.. NUM_PROCS * 3).collect({ 
            return new MapTester(sMap)
        })
        java.lang.Object sMapThreads = sMapTests.collect({ 
            return MapTester.run(it)
        })
        this.sleep(SLEEP_TIME)
        sMapThreads.each({ 
            return it.interrupt()
        })
        sMapThreads.each({ 
            return it.join()
        })
        return this.printResults(sMapTests)
    }

    public java.lang.Object printResults(final java.lang.Object tests) {
        this.println('Reads: ' + tests.sum({ 
            return it .readCount
        }))
        return this.println('Writes: ' + tests.sum({ 
            return it .writeCount
        }))
    }

    public java.lang.Object this$dist$invoke$3(java.lang.String name, java.lang.Object args) {
        return this."$name"(* args )
    }

    public void this$dist$set$3(java.lang.String name, java.lang.Object value) {
        this ."$name" = value 
    }

    public java.lang.Object this$dist$get$3(java.lang.String name) {
        return this ."$name"
    }

    protected groovy.lang.MetaClass $getStaticMetaClass() {
    }

    public static void __$swapInit() {
    }

    static static { 
        __timeStamp__239_neverHappen1357505293337 = 0
        __timeStamp = 1357505293337
    }

    public java.lang.Object super$3$getProperty(java.lang.String param0) {
    }

    public java.lang.String super$1$toString() {
    }

    public void super$3$setProperty(java.lang.String param0, java.lang.Object param1) {
    }

    public void super$1$notify() {
    }

    public void super$3$println() {
    }

    public void super$1$notifyAll() {
    }

    public void super$3$print(java.lang.Object param0) {
    }

    public void super$3$printf(java.lang.String param0, java.lang.Object[] param1) {
    }

    public java.lang.Object super$1$clone() {
    }

    public java.lang.Object super$3$evaluate(java.lang.String param0) {
    }

    public void super$1$wait() {
    }

    public groovy.lang.MetaClass super$2$getMetaClass() {
    }

    public void super$1$wait(long param0, int param1) {
    }

    public void super$2$setMetaClass(groovy.lang.MetaClass param0) {
    }

    public groovy.lang.Binding super$3$getBinding() {
    }

    public java.lang.Class<java.lang.Object extends java.lang.Object> super$1$getClass() {
    }

    public void super$1$finalize() {
    }

    public void super$3$printf(java.lang.String param0, java.lang.Object param1) {
    }

    public void super$3$setBinding(groovy.lang.Binding param0) {
    }

    public void super$1$wait(long param0) {
    }

    public void super$3$run(java.io.File param0, java.lang.String[] param1) {
    }

    public java.lang.Object super$3$evaluate(java.io.File param0) {
    }

    public void super$3$println(java.lang.Object param0) {
    }

    public boolean super$1$equals(java.lang.Object param0) {
    }

    public java.lang.Object super$3$invokeMethod(java.lang.String param0, java.lang.Object param1) {
    }

    public int super$1$hashCode() {
    }

}
import groovy.transform.WithReadLock as WithReadLock
import groovy.transform.Immutable as Immutable
import groovy.transform.Synchronized as Synchronized
import groovy.transform.WithWriteLock as WithWriteLock

public class BaseMap implements groovy.lang.GroovyObject extends java.lang.Object { 

    private java.util.List list 
    private static org.codehaus.groovy.reflection.ClassInfo $staticClassInfo 
    public static transient boolean __$stMC 
    private transient groovy.lang.MetaClass metaClass 
    public static long __timeStamp 
    public static long __timeStamp__239_neverHappen1357505293324 

    public BaseMap() {
        list = []
        metaClass = /*BytecodeExpression*/
    }

    public java.lang.Object getAt(java.lang.String key) {
        return list.find({ java.lang.Object pair ->
            return pair .key == key 
        })?.value
    }

    public void putAt(java.lang.String key, java.lang.Object value) {
        for (java.lang.Integer i = 0; i < list.size();++( i )) {
            if ( list [ i ].key == key ) {
                list.remove(i)
                break
            }
        }
        list.add(new MapPair(['key': key , 'value': value ]))
    }

    public java.lang.Object this$dist$invoke$1(java.lang.String name, java.lang.Object args) {
        return this."$name"(* args )
    }

    public void this$dist$set$1(java.lang.String name, java.lang.Object value) {
        this ."$name" = value 
    }

    public java.lang.Object this$dist$get$1(java.lang.String name) {
        return this ."$name"
    }

    protected groovy.lang.MetaClass $getStaticMetaClass() {
    }

    public groovy.lang.MetaClass getMetaClass() {
    }

    public void setMetaClass(groovy.lang.MetaClass mc) {
    }

    public java.lang.Object invokeMethod(java.lang.String method, java.lang.Object arguments) {
    }

    public java.lang.Object getProperty(java.lang.String property) {
    }

    public void setProperty(java.lang.String property, java.lang.Object value) {
    }

    public static void __$swapInit() {
    }

    static static { 
        __timeStamp__239_neverHappen1357505293324 = 0
        __timeStamp = 1357505293324
    }

    public void super$1$wait() {
    }

    public java.lang.String super$1$toString() {
    }

    public void super$1$wait(long param0) {
    }

    public void super$1$wait(long param0, int param1) {
    }

    public void super$1$notify() {
    }

    public void super$1$notifyAll() {
    }

    public java.lang.Class<java.lang.Object extends java.lang.Object> super$1$getClass() {
    }

    public java.lang.Object super$1$clone() {
    }

    public boolean super$1$equals(java.lang.Object param0) {
    }

    public int super$1$hashCode() {
    }

    public void super$1$finalize() {
    }

}
import groovy.transform.WithReadLock as WithReadLock
import groovy.transform.Immutable as Immutable
import groovy.transform.Synchronized as Synchronized
import groovy.transform.WithWriteLock as WithWriteLock

public class MapTester implements groovy.lang.GroovyObject extends java.lang.Object { 

    private java.lang.Object theMap 
    public long readCount 
    public long writeCount 
    private java.util.Random random 
    private static int MAX 
    private static int READ_MAX 
    private static org.codehaus.groovy.reflection.ClassInfo $staticClassInfo 
    public static transient boolean __$stMC 
    private transient groovy.lang.MetaClass metaClass 
    public static long __timeStamp 
    public static long __timeStamp__239_neverHappen1357505293328 

    public MapTester(java.lang.Object theMap) {
        readCount = 0
        writeCount = 0
        random = new java.util.Random()
        metaClass = /*BytecodeExpression*/
        this .theMap = theMap 
    }

    public java.util.Map getCounts() {
        return ['read': readCount , 'wrote': writeCount ]
    }

    public void runTest() {
        java.lang.Integer nextVal = random.nextInt(MAX)
        if (random.nextInt( READ_MAX + 1) == READ_MAX ) {
            theMap [ nextVal.toString()] = nextVal 
            ++( writeCount )
        } else {
            java.lang.Object val = theMap [ nextVal.toString()]
            if ( val && val == 1000) {
                this.println(' ')
            }
            ++( readCount )
        }
    }

    public static java.lang.Thread run(MapTester theMap) {
        java.lang.Object t = new java.lang.Thread(Interrupts.runnable(['work': { 
            return theMap.runTest()
        }]))
        t.start()
        return t 
    }

    public java.lang.Object this$dist$invoke$1(java.lang.String name, java.lang.Object args) {
        return this."$name"(* args )
    }

    public void this$dist$set$1(java.lang.String name, java.lang.Object value) {
        this ."$name" = value 
    }

    public java.lang.Object this$dist$get$1(java.lang.String name) {
        return this ."$name"
    }

    protected groovy.lang.MetaClass $getStaticMetaClass() {
    }

    public groovy.lang.MetaClass getMetaClass() {
    }

    public void setMetaClass(groovy.lang.MetaClass mc) {
    }

    public java.lang.Object invokeMethod(java.lang.String method, java.lang.Object arguments) {
    }

    public java.lang.Object getProperty(java.lang.String property) {
    }

    public void setProperty(java.lang.String property, java.lang.Object value) {
    }

    public static void __$swapInit() {
    }

    static static { 
        __timeStamp__239_neverHappen1357505293328 = 0
        __timeStamp = 1357505293328
        READ_MAX = 19
        MAX = 500
    }

    public void super$1$wait() {
    }

    public java.lang.String super$1$toString() {
    }

    public void super$1$wait(long param0) {
    }

    public void super$1$wait(long param0, int param1) {
    }

    public void super$1$notify() {
    }

    public void super$1$notifyAll() {
    }

    public java.lang.Class<java.lang.Object extends java.lang.Object> super$1$getClass() {
    }

    public java.lang.Object super$1$clone() {
    }

    public boolean super$1$equals(java.lang.Object param0) {
    }

    public int super$1$hashCode() {
    }

    public void super$1$finalize() {
    }

}
import groovy.transform.WithReadLock as WithReadLock
import groovy.transform.Immutable as Immutable
import groovy.transform.Synchronized as Synchronized
import groovy.transform.WithWriteLock as WithWriteLock

public class MapSynch extends BaseMap { 

    final private java.lang.Object $lock 
    private static org.codehaus.groovy.reflection.ClassInfo $staticClassInfo 
    public static transient boolean __$stMC 
    public static long __timeStamp 
    public static long __timeStamp__239_neverHappen1357505293333 

    public MapSynch() {
        $lock = new java.lang.Object[0]
    }

    @groovy.transform.Synchronized
    public java.lang.Object getAt(java.lang.String key) {
        synchronized ( $lock ) {
            return super.getAt(key)
        }
    }

    @groovy.transform.Synchronized
    public void putAt(java.lang.String key, java.lang.Object value) {
        synchronized ( $lock ) {
            super.putAt(key, value)
        }
    }

    public java.lang.Object this$dist$invoke$2(java.lang.String name, java.lang.Object args) {
        return this."$name"(* args )
    }

    public void this$dist$set$2(java.lang.String name, java.lang.Object value) {
        this ."$name" = value 
    }

    public java.lang.Object this$dist$get$2(java.lang.String name) {
        return this ."$name"
    }

    protected groovy.lang.MetaClass $getStaticMetaClass() {
    }

    public static void __$swapInit() {
    }

    static static { 
        __timeStamp__239_neverHappen1357505293333 = 0
        __timeStamp = 1357505293333
    }

    public java.lang.Object super$2$getProperty(java.lang.String property) {
    }

    public java.lang.String super$1$toString() {
    }

    public void super$2$setProperty(java.lang.String property, java.lang.Object value) {
    }

    public void super$1$notify() {
    }

    public void super$1$notifyAll() {
    }

    public void super$2$putAt(java.lang.String key, java.lang.Object value) {
    }

    public java.lang.Object super$2$this$dist$invoke$1(java.lang.String name, java.lang.Object args) {
    }

    public void super$1$wait() {
    }

    public groovy.lang.MetaClass super$2$getMetaClass() {
    }

    public void super$1$wait(long param0, int param1) {
    }

    public void super$2$setMetaClass(groovy.lang.MetaClass mc) {
    }

    public java.lang.Object super$2$this$dist$get$1(java.lang.String name) {
    }

    public java.lang.Object super$2$getAt(java.lang.String key) {
    }

    public groovy.lang.MetaClass super$2$$getStaticMetaClass() {
    }

    public void super$2$this$dist$set$1(java.lang.String name, java.lang.Object value) {
    }

    public java.lang.Object super$2$invokeMethod(java.lang.String method, java.lang.Object arguments) {
    }

}
import groovy.transform.WithReadLock as WithReadLock
import groovy.transform.Immutable as Immutable
import groovy.transform.Synchronized as Synchronized
import groovy.transform.WithWriteLock as WithWriteLock

public class MapRwSynch extends BaseMap { 

    final private java.util.concurrent.locks.ReentrantReadWriteLock $reentrantlock 
    private static org.codehaus.groovy.reflection.ClassInfo $staticClassInfo 
    public static transient boolean __$stMC 
    public static long __timeStamp 
    public static long __timeStamp__239_neverHappen1357505293334 

    public MapRwSynch() {
        $reentrantlock = new java.util.concurrent.locks.ReentrantReadWriteLock()
    }

    @groovy.transform.WithReadLock
    public java.lang.Object getAt(java.lang.String key) {
        $reentrantlock.readLock().lock()
        try {
            return super.getAt(key)
        } 
        finally { 
            $reentrantlock.readLock().unlock()} 
    }

    @groovy.transform.WithWriteLock
    public void putAt(java.lang.String key, java.lang.Object value) {
        $reentrantlock.writeLock().lock()
        try {
            super.putAt(key, value)
        } 
        finally { 
            $reentrantlock.writeLock().unlock()} 
    }

    public java.lang.Object this$dist$invoke$2(java.lang.String name, java.lang.Object args) {
        return this."$name"(* args )
    }

    public void this$dist$set$2(java.lang.String name, java.lang.Object value) {
        this ."$name" = value 
    }

    public java.lang.Object this$dist$get$2(java.lang.String name) {
        return this ."$name"
    }

    protected groovy.lang.MetaClass $getStaticMetaClass() {
    }

    public static void __$swapInit() {
    }

    static static { 
        __timeStamp__239_neverHappen1357505293334 = 0
        __timeStamp = 1357505293334
    }

    public java.lang.Object super$2$getProperty(java.lang.String property) {
    }

    public java.lang.String super$1$toString() {
    }

    public void super$2$setProperty(java.lang.String property, java.lang.Object value) {
    }

    public void super$1$notify() {
    }

    public void super$1$notifyAll() {
    }

    public void super$2$putAt(java.lang.String key, java.lang.Object value) {
    }

    public java.lang.Object super$2$this$dist$invoke$1(java.lang.String name, java.lang.Object args) {
    }

    public void super$1$wait() {
    }

    public groovy.lang.MetaClass super$2$getMetaClass() {
    }

    public void super$1$wait(long param0, int param1) {
    }

    public void super$2$setMetaClass(groovy.lang.MetaClass mc) {
    }

    public java.lang.Object super$2$this$dist$get$1(java.lang.String name) {
    }

    public java.lang.Object super$2$getAt(java.lang.String key) {
    }

    public groovy.lang.MetaClass super$2$$getStaticMetaClass() {
    }

    public void super$2$this$dist$set$1(java.lang.String name, java.lang.Object value) {
    }

    public java.lang.Object super$2$invokeMethod(java.lang.String method, java.lang.Object arguments) {
    }

}
