import groovy.transform.Immutable;

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
  
  public static void main(String[] args) {
    final def body = "Fiona requests that you shower." +
      sep + sep + "Right now!";
    new Email(to: 'shrek@apple.com', from: 'me@gmail.com',
	      subject: 'Please Shower', body: body).send();
    random().send();
  }
}