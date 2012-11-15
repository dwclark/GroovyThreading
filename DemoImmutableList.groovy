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