/*
 * Created on 27-Feb-2003
 */
package xpetstore.util;

import java.util.Date;

import javax.activation.DataHandler;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import javax.naming.InitialContext;
import javax.naming.NamingException;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 */
public class MailUtil
{
    //~ Static fields/initializers ---------------------------------------------

    public static final String MAIL_SESSION = "java:comp/env/mail/xpetstore/MailSession";

    //~ Methods ----------------------------------------------------------------

    public static void send( String to,
                             String subject,
                             String body )
        throws NamingException, 
                   AddressException, 
                   MessagingException
    {
        InitialContext     ic = new InitialContext(  );
        Session            session = ( Session ) ic.lookup( MAIL_SESSION );
        javax.mail.Message msg = new MimeMessage( session );

        msg.setFrom(  );
        msg.setRecipients( javax.mail.Message.RecipientType.TO, InternetAddress.parse( to, false ) );
        msg.setSubject( subject );

        msg.setDataHandler( new DataHandler( body, "text/plain" ) );
        msg.setHeader( "X-Mailer", "JavaMailer" );
        msg.setSentDate( new Date(  ) );

        Transport.send( msg );
    }
}
