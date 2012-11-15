public class TaskManager {
  Stack tasks = new Stack();
  int pending = 0;

  public void add(def task) {
    tasks.push(task);
    ++pending;
  }

  public boolean getHasTasks() { tasks.size() > 0; }
  public void addAll(def all) { all.each { add(it); }; }
  public Object next() { tasks.pop(); }
  public boolean getFinished() { pending == 0; }
  public void completedTask() { --pending; }
}
