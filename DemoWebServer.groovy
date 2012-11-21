import java.net.*;
import groovy.transform.Immutable;
import groovyx.gpars.actor.*;

//messages for communication between manager and workers
class SendThread {
  public SendThread(final Thread thread) {
    this.thread = thread;
  }

  final Thread thread;
}

class HandleRequest {
  public HandleRequest(final Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  final Socket clientSocket;
}

@Immutable class FinishedRequest { }
@Immutable class Shutdown { }

//WorkerManager class.  Manages processing of requests and
//allocation of actors.  Leaves all request handling logic
//to the workers
class WorkerManager extends DynamicDispatchActor {
  final Stack freeWorkers = new Stack();
  final List inUseWorkers = [];
  Thread socketThread;
  final File baseDir;

  public WorkerManager(File baseDir) {
    this.baseDir = baseDir;
  }

  public void onMessage(SendThread st) {
    socketThread = st.thread;
  }

  public void onMessage(HandleRequest request) {
    RequestHandler handler;
    if(freeWorkers.size() > 0) {
      handler = freeWorkers.pop();
    }
    else {
      handler = new RequestHandler(baseDir);
      handler.start();
    }
    
    inUseWorkers.add(handler);
    handler << request;
  }

  public void onMessage(FinishedRequest fr) {
    int i;
    for(i = 0; i < inUseWorkers.size(); ++i) {
      if(inUseWorkers[i].is(sender)) break;
    }

    inUseWorkers.remove(i);
    freeWorkers.push(sender);
  }

  public void onMessage(Shutdown req) {
    socketThread.interrupt();
    freeWorkers.each { it.stop(); it.join(); };
    inUseWorkers.each { it.stop(); it.join(); }
    stop();
  }
}

//Handles a request for a file.  Obviously should be
//made much more robust in returning proper response
//headers, mime/types, etc., but the basic idea should be
//clear enough
class RequestHandler extends DefaultActor {
  final File baseDir;
  
  public RequestHandler(File baseDir) {
    this.baseDir = baseDir;
  }

  String request(Socket sock) {
    def is = sock.inputStream;
    def ary = new byte[4096];
    def strBuilder = new StringBuilder();
    int read = is.read(ary);
    def str = new String(ary, 0, read, 'US-ASCII');
    return str;
  }
  
  String requestedFile(String str) {
    def lines = str.split('\r');
    def getLine = lines[0].trim();
    return getLine.split(' ')[1].trim().substring(1);
  }

  File locateFile(String filePath) {
    def file = new File(baseDir, filePath);
    file.exists() ? file : null;
  }

  void dispatch(Socket sock, File file) {
    sock.outputStream << new FileInputStream(file);
  }

  void sendError(Socket sock) {
    def file = locateFile('error.html');
    dispatch(sock, file);
  }

  boolean isShutdown(String filePath) {
    return filePath == 'shutdown';
  }

  void act() {
    loop {
      react { message ->
	Socket sock = message.clientSocket;
	String req = request(sock);
	String filePath = requestedFile(req);
	if(isShutdown(filePath)) {
	  sender << new Shutdown();
	}
	else {
	  File file = locateFile(filePath);
	  if(!file) sendError(sock);
	  else dispatch(sock, file);
	  sender << new FinishedRequest();
	}

	sock.close();
      }
    }
  }
}

//Sets up the main server thread.  The main server only
//listens for connections and then passes the socket off
//to the workers and managers.  Shut down is handled via
//interruption of the main server thread.  accept() is not
//subject to interruption, so we put a timeout to check
//for interruption before resuming wait in accept().
def runServer(int port, WorkerManager manager) {
  def serverSocket = new ServerSocket(port);
  serverSocket.soTimeout = 500;
  def onEnd = { -> println('Shutting down server'); serverSocket.close(); };
  
  def serverAction = {
    try {
      def clientSocket = serverSocket.accept();
      manager << new HandleRequest(clientSocket);
    }
    catch(SocketTimeoutException stoe) { } };
  
  def runnable = Interrupts.runnable(work: serverAction, onEnd: onEnd);
  def thread = new Thread(runnable);
  manager << new SendThread(thread);
  thread.start();
  return thread;
}

//Set up main server and then wait for clean shutdown
WorkerManager manager = new WorkerManager(new File(args[0]));
manager.start();
runServer(8080, manager).join();
manager.join();xs