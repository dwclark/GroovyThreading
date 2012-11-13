public class Interrupts {
  
  static final defaultCondition = { -> true; };
  static final defaultOnInterrupt = { -> };
  static final defaultOnEnd = { -> };
  
  static guarded(final def work,
		 final def condition = defaultCondition,
		 final def onInterrupt = defaultOnInterrupt,
		 final def onEnd = defaultOnEnd) {
    while(!Thread.currentThread().interrupted && condition()) {
      try {
	work();
      }
      catch(InterruptedException ie) {
	Thread.currentThread().interrupt();
      }
    }

    if(Thread.currentThread().interrupted) {
      onInterrupt();
    }

    onEnd();
  }

  static Runnable runnable(final def work, 
			   final def condition = defaultCondition,
			   final def onInterrupt = defaultOnInterrupt,
			   final def onEnd = defaultOnEnd) {
    return { -> guarded(work, condition, onInterrupt, onEnd); } as Runnable;
  }
}
