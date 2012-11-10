import groovy.transform.Immutable;

@Immutable public class Cell {
  private final data;
  public getData() { this.data; }

  Cell next;
}
