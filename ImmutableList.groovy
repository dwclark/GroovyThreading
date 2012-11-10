import groovy.transform.Immutable;

@Immutable public class ImmutableList {

  Cell head;

  public boolean equals(Object list) {
    if(!(list instanceof ImmutableList)) return false;
    if(this.size() != list.size()) return false;
    
    Cell current = head;
    while(current.next) {
      if(!list.any { item -> item == current.data; }) return false;
      else current = current.next;
    }

    return list.any { item -> item == current.data; };
  }

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

  public ImmutableList add(final def data) {
    final newHead = new Cell(data: data, next: head);
    new ImmutableList(newHead);
  }
  
  public ImmutableList addAll(final def collection) {
    Cell newHead = head;
    collection.each { data -> 
      Cell tmp = new Cell(next: newHead, data: data);
      newHead = tmp; };
    new ImmutableList(newHead);
  }
  
  public Object pop() { head.next; }
  public ImmutableList push(final data) { add(data); }

  public ImmutableList reverse() {
    new ImmutableList(null).addAll(this);
  }

  public ImmutableList remove(final Object obj) {
    Cell found = findCell { it == obj; };
    if(!found) return this;
    
    Cell newLast = found.next;
    Cell current = head;
    while(!current.is(found)) {
      newLast = new Cell(next: newLast, data: current.data);
      current = current.next;
    }
    
    return new ImmutableList(newLast);
  }

  public ImmutableList removeAll(final def collection) {
    Cell found = lastCellMatching(collection);
    if(!found) return this;

    Cell newLast = found.next;
    Cell current = head;
    while(!current.is(found)) {
      if(!collection.any { item -> item == current.data; }) {
	newLast = new Cell(next: newLast, data: current.data);
      }
      
      current = current.next;
    }

    return new ImmutableList(newLast);
  }

  public List toMutableList() {
    def list = new ArrayList();
    each { list.add(it); }
    return list;
  }

  public void each(Closure closure) {
    eachWithIndex({ item, index -> closure(item); });
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

  private void eachCell(Closure closure) {
    Cell current = head;
    int index = 0;
    while(current.next) {
      closure(current);
      current = current.next;
    }

    closure(current);
  }

  private Cell lastCellMatching(final def collection) {
    Cell last = null;
    eachCell { cell ->
      if(collection.any { item -> item == cell.data; }) {
	last = cell;
      } };

    return last;
  }

  public boolean any(Closure closure) {
    return find(closure);
  }

  public boolean every(Closure closure) {
    boolean ret = true;
    Cell current = head;
    while(current.next) {
      ret = ret && closure(current.data);
      if(!ret) return false;
      else current = current.next;
    }

    return closure(current.data);
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

  public Cell findCell(Closure closure) {
    Cell current = head;
    while(current.next) {
      if(closure(current.data)) return current;
      current = current.next;
    }
    
    if(closure(current.data)) return current;
    return null;
  }

  public Object find(Closure closure) {
    return findCell(closure)?.data;
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

  public String join(final String separator) {
    StringBuilder sb = new StringBuilder();
    eachCell { cell ->
      sb.append(cell.data.toString());
      if(cell.next) sb.append(separator); };
    return sb.toString();
  }

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
    assert(!list2.find { it == 100; });

    //test findAll and size methods
    assert(6 == list2.findAll { it >= 5; }.size());

    //test any method
    assert(list1.any { it == 4; });
    assert(!list1.any { it == 20; });

    //test every method
    assert(list1.every { it < 20; });
    assert(!list1.every { it < 5; });

    //test equals
    assert(ImmutableList.newList([ 1, 2, 3, 4, 5, 6 ]) ==
	   ImmutableList.newList([ 6, 5, 4, 3, 1, 2 ]));
    assert(ImmutableList.newList([ 1, 2, 3, 4, 5 ]) !=
	   ImmutableList.newList([ 6, 5, 4, 3, 1, 2 ]));

    //test remove
    final listNo9 = list2.remove(9);
    assert(listNo9.size() == 9);
    assert(!listNo9.any { it == 9; });
    assert(list2.size() == 10);
    assert(list2.remove(25).size() == 10);

    //test reverse
    final def reverseCmp = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ];
    list2.reverse().eachWithIndex { item, i -> assert(item == reverseCmp[i]); };

    //test removeAll
    final def listNo567 = list2.removeAll([5,6,7]);
    assert(listNo567.size() == 7);
    assert(listNo567.join(',') == '8,9,10,4,3,2,1');
  }
}