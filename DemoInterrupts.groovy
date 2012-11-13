long i = 0;
def t1 = new Thread(Interrupts.runnable({ -> ++i; }, Interrupts.defaultCondition,
					{ -> println('I got interrupted'); }));
t1.start();
	
//It is now unsafe for any thread, other than t1 to access i	    
def console = System.console();
def nextInput = {
  print("Please enter the next command, or 'stop' to terminate: ");
  return console.readLine(); };
while(nextInput() != 'stop') { }

//interrupt t1 and wait for it to complete
t1.interrupt();
t1.join();

//it is now safe to access i from this thread 
println(i);

short s = 0;
boolean completed = false;
def t2 = new Thread(Interrupts.runnable({ -> ++s; }, { -> s >= 0 },
					{ -> println('I got interrupted'); },
					{ -> completed = true; }));
t2.start();

//it is now unsafe for any thread, other than t2 to access s and completed
while(nextInput() != 'stop') { }

t2.interrupt(); //should have no effect
t2.join();

//it is now safe to access s and completed
println("s: ${s}, completed: ${completed}");
