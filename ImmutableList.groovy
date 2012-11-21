import groovy.transform.Immutable;

//Note, this is not a true value type.  The data field 
//will not be used to compute the hash code, nor in the equals method.
//I'm mainly using the @Immutable annotation to be lazy.

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
}