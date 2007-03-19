package xpetstore.services.mail.ejb;

import java.util.Date;

import javax.activation.DataHandler;

import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import javax.naming.InitialContext;

import xpetstore.services.mail.model.Email;

import xpetstore.util.Debug;
import xpetstore.util.JNDINames;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @ejb.bean
 *      name="Mailer"
 *      acknowledge-mode="Auto-acknowledge"
 *      destination-type="javax.jms.Queue"
 *      subscription-durability="Durable"
 *      transaction-type="Container"
 * @ejb.transaction
 *      type="NotSupported"
 * @ejb.resource-ref
 *      res-ref-name="${jndi.mail.session}"
 *      res-type="javax.mail.Session"
 *      res-auth="Container"
 * 		jndi-name="${orion.mail.session}"
 *
 * @jboss.destination-jndi-name
 *      name="${jboss.queue.mail}"
 * @jboss.resource-ref
 *      res-ref-name="${jndi.mail.session}"
 *      jndi-name="${jboss.mail.session}"
 *
 * @weblogic.message-driven
 *      destination-jndi-name="${weblogic.queue.mail}"
 * @weblogic.resource-description
 *      res-ref-name="${jndi.mail.session}"
 *      jndi-name="${weblogic.mail.session}"
 *
 * @orion.bean
 *      connection-factory-location="${orion.queue.ConnectionFactory}"
 *      destination-location="${orion.queue.mail}"
 */
public class MailerMDB
    implements MessageDrivenBean,
                   MessageListener
{
    //~ Methods ----------------------------------------------------------------

    /**
     * This method expect a <code>javax.jms.ObjectMessage</code> that
     * contains a {@link Email} as the message object
     */
    public void onMessage( Message recvMsg )
    {
        try
        {
            Debug.print( "MailerMDB.onMessage(" + recvMsg + ")" );

            ObjectMessage msg = ( ObjectMessage ) recvMsg;
            Email         email = ( Email ) msg.getObject(  );

            send( email.getTo(  ), email.getSubject(  ), email.getBody(  ) );
        }
        catch ( Exception e )
        {
            Debug.print( "Unable to send the email", e );
        }
    }

    public void send( String to,
                      String subject,
                      String body )
        throws Exception
    {
        Debug.print( "MailerMDB.send(" + to + "," + subject + ",...)" );

        InitialContext     ic = new InitialContext(  );
        Session            session = ( Session ) ic.lookup( JNDINames.MAIL_SESSION );
        javax.mail.Message msg = new MimeMessage( session );

        msg.setFrom(  );
        msg.setRecipients( javax.mail.Message.RecipientType.TO, InternetAddress.parse( to, false ) );
        msg.setSubject( subject );

        msg.setDataHandler( new DataHandler( body, "text/html" ) );
        msg.setHeader( "X-Mailer", "JavaMailer" );
        msg.setSentDate( new Date(  ) );

        Transport.send( msg );
    }

    //=============================================
    // EJB callbacks
    //=============================================
    public void ejbCreate(  ) {}

    public void setMessageDrivenContext( MessageDrivenContext mdc ) {}

    public void ejbRemove(  ) {}
}
