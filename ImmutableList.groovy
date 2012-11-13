import groovy.transform.Immutable;

@Immutable public class ImmutableList {

  private final data;
  public getData() { return data; }

  ImmutableList next;
  private static final ImmutableList nullList = new ImmutableList(data: null);

  public boolean equals(Object list) {
    if(!(list instanceof ImmutableList)) return false;
    if(this.size() != list.size()) return false;
    
    ImmutableList current = this;
    while(!current.is(nullList)) {
      if(!list.any { item -> item == current.data; }) return false;
      else current = current.next;
    }

    return true;
  }

  public static ImmutableList newList() { return nullList; }

  public static ImmutableList newList(final data) {
    new ImmutableList(data: data, next: nullList);
  }

  public static ImmutableList newList(Collection col) {
    if(!col) return nullList;

    ImmutableList next = nullList;
    col.each { obj -> next = new ImmutableList(data: obj, next: next); };
    return next;
  }

  public ImmutableList add(final def data) {
    return new ImmutableList(data: data, next: this);
  }
  
  public ImmutableList addAll(final def collection) {
    ImmutableList next = this;
    collection.each { obj -> next = new ImmutableList(data: obj, next: next); };
    return next;
  }
  
  public ImmutableList reverse() {
    return nullList.addAll(this);
  }

  public ImmutableList remove(final Object obj) {
    ImmutableList found = findList { it == obj; };
    if(found.is(nullList)) return this;
    
    ImmutableList head = found.next;
    ImmutableList current = this;
    while(!current.is(found)) {
      head = new ImmutableList(next: head, data: current.data);
      current = current.next;
    }
    
    return head;
  }

  public ImmutableList removeAll(final def collection) {
    ImmutableList found = lastListMatching(collection);
    if(found.is(nullList)) return this;

    ImmutableList head = found.next;
    ImmutableList current = this;
    while(!current.is(found)) {
      if(!collection.any { item -> item == current.data; }) {
	head = new ImmutableList(next: head, data: current.data);
      }
      
      current = current.next;
    }

    return head;
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
    ImmutableList current = this;
    int index = 0;
    while(!current.is(nullList)) {
      closure(current.data, index++);
      current = current.next;
    }
  }

  private void eachList(Closure closure) {
    ImmutableList current = this;
    while(!current.is(nullList)) {
      closure(current);
      current = current.next;
    }
  }

  private ImmutableList lastListMatching(final def collection) {
    ImmutableList last = nullList;
    eachList { list ->
      if(collection.any { item -> item == list.data; }) {
	last = list;
      } };
    
    return last;
  }

  public boolean any(Closure closure) {
    return find(closure);
  }

  public boolean every(Closure closure) {
    boolean ret = true;
    ImmutableList current = this;
    while(!current.is(nullList)) {
      ret = ret && closure(current.data);
      if(!ret) return false;
      else current = current.next;
    }

    return true;
  }

  public int size() {
    int ret = 0;
    ImmutableList current = this;
    while(!current.is(nullList)) {
      current = current.next;
      ++ret;
    }

    return ret;
  }

  public ImmutableList sort() {
    return newList(toMutableList().sort().reverse());
  }

  public ImmutableList sort(Closure closure) {
    return newList(toMutableList().sort(closure).reverse());
  }

  public ImmutableList findList(Closure closure) {
    ImmutableList current = this;
    while(!current.is(nullList)) {
      if(closure(current.data)) return current;
      current = current.next;
    }

    return nullList;
  }

  public Object find(Closure closure) {
    return findList(closure).data;
  }

  public ImmutableList findAll(Closure closure) {
    ImmutableList current = nullList;
    each { obj ->
      if(closure(obj)) {
	current = new ImmutableList(data: obj, next: current);
      } };
    
    return current;
  }

  public String join(final String separator) {
    StringBuilder sb = new StringBuilder();
    eachList { list ->
      sb.append(list.data.toString());
      if(!list.next.is(nullList)) sb.append(separator); };
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
    assert(ImmutableList.newList() == ImmutableList.newList());

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
    assert(list2.removeAll([1,2,3,4,5,6,7,8,9,10]) == ImmutableList.newList());

    //test join
    assert(listNo567.join(',') == '8,9,10,4,3,2,1');
  }
}