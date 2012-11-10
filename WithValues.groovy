import groovy.transform.Immutable;

@Immutable public class Name {
  String first, middle, last;
}

@Immutable public class Address {
  String street, city, state, zip;
}

//Note, can aggregate other Immutable classes.
//Also, any JDK collection will be wrapped in an 
//unmodifiable version of that class inside the
//generated constructor.
@Immutable public class Person {
  Name name;
  Address address;
  Date birthDate;
  List children;
}

//Nulls are handled correctly in value classes
assert(new Name([:]) == new Name([:]));

//equals() works as expected
assert(new Name(first: 'Sponge', middle: 'Bob', last: 'Squarepants') ==
       new Name(first: 'Sponge', middle: 'Bob', last: 'Squarepants'))

//toString() is given a sensible default
assert(new Address(street: '123 Main Street', city: 'Frisco', state: 'TX').toString() ==
       'Address(123 Main Street, Frisco, TX, null)');

//hashCode() also works as expected, immutable values make good key
//candidates for maps
final fmt = 'MM/dd/yyyy';
final mcenroe = new Person(new Name('John', null, 'McEnroe'), null,
			   Date.parse(fmt, '02/16/1959'), null)
def map = [ (mcenroe): 'Rugby', (mcenroe): 'Tennis' ];
assert(map.size() == 1);
assert(map[mcenroe] == 'Tennis');

def kids = [ new Person(new Name('Sally', 'L', 'Bloggs'), null, Date.parse(fmt, '1/1/1980'), null),
	     new Person(new Name('Joe', 'P', 'Bloggs'), null, Date.parse(fmt, '1/1/1982'), null) ];
def mom = new Person(new Name('Mrs', null, 'Bloggs'), null, null, kids);
assert(kids == mom.children);

//Uncomment to see error
//mom.children.add(new Person(new Name('Sam', 'J', 'Bloggs'), null, Date.parse(fmt, '1/1/1984'), null));