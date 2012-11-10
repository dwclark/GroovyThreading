import groovy.transform.ThreadInterrupt;

@ThreadInterrupt
public class LongRunning {
  
  static def one() {
    int i = 0;
    for(;;) {
      i = i+1;
    }

    return i;
  }

  static def two() {
    double x = 1.0d;
    while(true) {
      x = x * 1.5;
    }

    return x;
  }
}

public class Guard {
  static void interrupts(Closure closure) {
    boolean interrupted = false;
    try {
      closure();
    }
    catch(InterruptedException ie) {
      Thread.currentThread().interrupt();
    }
  }

  static Runnable interruptible(def closure) {
    return { -> Guard.interrupts(closure); } as Runnable;
  }
}

//set up a long running task that is interruptible,
//but make sure to guard the interruption
def t1 = new Thread(Guard.interruptible(LongRunning.&one));

//wait for signal to stop thread t1
def console = System.console();
def nextInput = {
  print("Please enter the next command, or 'stop' to terminate: ");
  return console.readLine(); };
while(nextInput() != 'stop') { }

//interrupt t1 and wait for it to complete
t1.interrupt();
t1.join();

