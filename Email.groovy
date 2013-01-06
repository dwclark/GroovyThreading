import groovy.transform.Immutable;
import groovyx.gpars.*;

@Immutable
public class Email {
  private static final File dir = new File('/home/david/tmp/Emails');
  private static final String sep = System.getProperty('line.separator');

  String to, from, subject, body;

  public void send() {
    File.createTempFile('eml', '.txt', dir).withWriter { writer ->
      writer << 'To: ' << to << sep;
      writer << 'From: ' << from << sep;
      writer << 'Subject: ' << subject << sep;
      writer << 'Body: ' << sep + sep << body << sep; };
  }

  public static Email random() {
    def rs = new RandomString();
    final def body = (0..4).inject('') { ret, i -> ret += (rs.next(40) + sep); }
    final def from = 'us@gmail.com';
    final def subject = rs.next(20);
    final def to = rs.next(10) + '@' + rs.next(10) + '.com';
    return new Email(to: to, from: from, subject: subject, body: body);
  }

  public static List fireAndTrack(List emails) {
    def tracking;
    GParsExecutorsPool.withPool {
      tracking = emails.collect { email -> email.&send.callAsync(); }; };
    return tracking;
  }

  public static List fireAndTrack(final def pool, List emails) {
    def tracking;
    GParsExecutorsPool.withExistingPool(pool) {
      tracking = emails.collect { email -> email.&send.callAsync(); }; };
    return tracking;
  }

  public static void fireAndForget(List emails) {
    GParsExecutorsPool.withPool {
      emails.each { email -> email.&send.callAsync(); }; };
  }

  public static List fireAndForget(final def pool, List emails) {
    GParsExecutorsPool.withExistingPool(pool) {
      emails.each { email -> email.&send.callAsync(); }; };
  }
  
  public static void main(String[] args) {
    final def emails = (0..<50).inject([]) { list, i -> list += random(); list; };

    //send without specifying pool
    fireAndForget(emails);
    final def tracking = fireAndTrack(emails);
    final def results = tracking*.get();

    final def pool = GParsExecutorsPool.createPool();
    fireAndForget(pool, emails);
    final def pooledTracking = fireAndTrack(pool, emails);
    final def pooledResults = pooledTracking*.get();
    pool.shutdown();
  }
}