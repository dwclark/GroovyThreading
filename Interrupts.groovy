import groovy.transform.ThreadInterrupt;

@ThreadInterrupt
public class LongRunning {
  
  def one() {
    int i = 0;
    for(;;) {
      i = i+1;
    }

    return i;
  }

  def two() {
    double x = 1.0d;
    while(true) {
      x = x * 1.5;
    }

    return x;
  }
}

public class Guard {
  static void interrupts(Closure closure) {
    try {
      closure();
    }
    catch(InterruptedException ie) {
      Thread.currentThread().interrupt();
    }
  }
}