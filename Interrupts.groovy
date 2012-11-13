public class Interrupts {
  
  static final defaultCondition = { -> true; };
  static final defaultOnInterrupt = { -> };
  static final defaultOnEnd = { -> };
  
  static guarded(final def args) {
    final def work = args.work;
    final def condition = args.condition ?: defaultCondition;
    final def onInterrupt = args.onInterrupt ?: defaultOnInterrupt;
    final def onEnd = args.onEnd ?: defaultOnEnd;

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

  static Runnable runnable(final def args) {
    return { -> guarded(args); } as Runnable;
  }
}
