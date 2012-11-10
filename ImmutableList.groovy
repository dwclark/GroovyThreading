import groovy.transform.Immutable;

@Immutable public class ImmutableList {

  Cell head;

  public static ImmutableList newList(final data) {
    new ImmutableList(new Cell(data: data, next: null));
  }

  public static ImmutableList newList(Collection objs) {
    def iter = objs.iterator();
    def newHead = new Cell(data: iter.next(), next: null);
    while(iter.hasNext()) {
      def tmp = new Cell(data: iter.next(), next: newHead);
      newHead = tmp;
    }

    new ImmutableList(newHead);
  }

  public ImmutableList add(final data) {
    final newHead = new Cell(data: data, next: head);
    new ImmutableList(newHead);
  }

  public ImmutableList addAll(Collection collection) {
    Cell newHead = head;
    collection.each { data -> 
      Cell tmp = new Cell(next: newHead, data: data);
      newHead = tmp; };
    new ImmutableList(newHead);
  }

  public List toMutableList() {
    def list = new ArrayList();
    each { list.add(it); }
    return list;
  }

  public void each(Closure closure) {
    Cell current = head;
    while(current.next) {
      closure(current.data);
      current = current.next;
    }

    closure(current.data);
  }

  public void eachWithIndex(Closure closure) {
    Cell current = head;
    int index = 0;
    while(current.next) {
      closure(current.data, index++);
      current = current.next;
    }

    closure(current.data, index++);
  }

  public int size() {
    int ret;
    eachWithIndex { item, i -> ret = i; };
    return ret + 1;
  }

  public ImmutableList sort() {
    return newList(toMutableList().sort().reverse());
  }

  public ImmutableList sort(Closure closure) {
    return newList(toMutableList().sort(closure).reverse());
  }

  public Object find(Closure closure) {
    Cell current = head;
    while(current.next) {
      if(closure(current.data)) return current.data;
      current = current.next;
    }

    if(closure(current.data)) return current.data;
    return null;
  }

  public ImmutableList findAll(Closure closure) {
    Cell current = null;
    each { obj ->
      if(closure(obj)) {
	if(current) {
	  Cell newCell = new Cell(data: obj, next: current);
	  current = newCell;
	}
	else current = new Cell(data: obj, next: null);
      } };

    new ImmutableList(head: current);
  }

  public Object pop() { head.next; }
  public ImmutableList push(final data) { add(data); }

  public static void main(String[] args) {
    //test basic creation
    final def list1 = ImmutableList.newList([1, 2, 3, 4, 5]);
    final def list1Cmp = [ 5, 4, 3, 2, 1 ];
    list1.eachWithIndex { item, i -> assert(item == list1Cmp[i]); };

    //test adding to the list
    final def list2 = list1.addAll([6, 7, 8, 9, 10]);
    final def list2Cmp = [ 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 ];
    list2.eachWithIndex { item, i -> assert(item == list2Cmp[i]); };

    //test converstion to standard JDK mutable list
    assert(list2Cmp == list2.toMutableList());

    //test default sort
    list2.sort().eachWithIndex { item, i -> assert(item == (i+1)); };

    //test sorting in reverse order to make sure sort with closure is correct
    final def list4 = list2.sort({ one, two -> two <=> one; });
    list4.eachWithIndex { item, i -> assert(item == list2Cmp[i]); };

    //test find method
    assert(5 == list2.find { it == 5; });

    //test findAll and size methods
    assert(6 == list2.findAll { it >= 5; }.size());
  }
}
