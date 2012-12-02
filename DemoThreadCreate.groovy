final def t = Thread.start {
  println("Going to sleep...");
  sleep(1000);
  println("I'm awake now.") };

t.join();