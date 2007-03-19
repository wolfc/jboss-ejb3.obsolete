package xpetstore.util;


/**
 * This class contains the JNDI names of all the resources uses by
 * the components of the service layer
 *
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 */
public abstract class JNDINames
{
    //~ Static fields/initializers ---------------------------------------------

    public static final String QUEUE_CONNECTION_FACTORY = "QueueConnectionFactory";
    public static final String QUEUE_ORDER = "queue/order";
    public static final String QUEUE_MAIL = "queue/mail";
    public static final String MAIL_SESSION = "java:comp/env/mail/xpetstore/MailSession";
    public static final String JDBC_DATASOURCE = "java:comp/env/jdbc/xpetstore";
}
