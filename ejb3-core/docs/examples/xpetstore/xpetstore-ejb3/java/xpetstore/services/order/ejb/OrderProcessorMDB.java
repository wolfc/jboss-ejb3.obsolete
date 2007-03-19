package xpetstore.services.order.ejb;

import java.util.Collection;
import java.util.Iterator;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import xpetstore.domain.customer.ejb.Customer;
import xpetstore.domain.order.ejb.Order;
import xpetstore.domain.order.ejb.OrderItem;

import xpetstore.domain.order.model.OrderStatus;

import xpetstore.services.mail.model.Email;
import xpetstore.services.order.exceptions.CreditCardException;

import xpetstore.util.Debug;
import xpetstore.util.JMSUtil;
import xpetstore.util.JNDINames;


/**
 *
 * @ ejb.bean
 *      name="OrderProcessor"
 *      acknowledge-mode="Auto-acknowledge"
 *      destination-type="javax.jms.Queue"
 *      subscription-durability="Durable"
 *      transaction-type="Container"
 * @ ejb.transaction
 *      type="Required"
 * @ ejb.ejb-ref
 *      ejb-name="Order"
 *      view-type="local"
 * 		ref-name="ejb/OrderLocal"
 * @ ejb.resource-ref
 *      res-ref-name="${jndi.queue.ConnectionFactory}"
 *      res-type="javax.jms.QueueConnectionFactory"
 *      res-auth="Container"
 * 		jndi-name="${orion.queue.ConnectionFactory}"
 * @ ejb.resource-ref
 *      res-ref-name="${jndi.queue.mail}"
 *      res-type="javax.jms.Queue"
 *      res-auth="Container"
 * 		jndi-name="${orion.queue.mail}"
 *
 * @ jboss.destination-jndi-name
 *      name="${jboss.queue.order}"
 * @ jboss.resource-ref
 *      res-ref-name="${jndi.queue.ConnectionFactory}"
 *      jndi-name="${jboss.queue.ConnectionFactory}"
 * @ jboss.resource-ref
 *      res-ref-name="${jndi.queue.mail}"
 *      jndi-name="${jboss.queue.mail}"
 *
 * @ weblogic.message-driven
 *      destination-jndi-name="${weblogic.queue.order}"
 * @ weblogic.resource-description
 *      res-ref-name="${jndi.queue.ConnectionFactory}"
 *      jndi-name="${weblogic.queue.ConnectionFactory}"
 * @ weblogic.resource-description
 *      res-ref-name="${jndi.queue.mail}"
 *      jndi-name="${weblogic.queue.mail}"
 *
 * @ orion.bean
 *      connection-factory-location="${orion.queue.ConnectionFactory}"
 *      destination-location="${orion.queue.order}"
 */
@MessageDriven(name="OrderProcessor", activationConfig =
{
      @ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
      @ActivationConfigProperty(propertyName="destination", propertyValue="queue/order")
})
public class OrderProcessorMDB
   implements MessageListener
{
   @PersistenceContext
   private EntityManager manager;

    /**
     * This method expect a <code>javax.jms.ObjectMessage</code> that
     * contains the orderUId as the message object
     */
    public void onMessage( Message recvMsg )
    {
        Integer    orderUId = null;
        Order order = null;

        try
        {
            Debug.print( "OrderProcessorMDB.onMessage(" + recvMsg + ")" );

            /* Get the order to proceed */
            ObjectMessage msg = ( ObjectMessage ) recvMsg;
            orderUId = ( Integer ) msg.getObject(  );
            order    = manager.find( Order.class, orderUId );

            /* Proceed the order */
            proceedPayment( order );
            proceedOrder( order );
        }
        catch ( CreditCardException c )
        {
            try
            {
                cancelOrder( order );
            }
            catch ( Exception e )
            {
                Debug.print( "Unable to cancel the order[" + orderUId + "]", e );
            }
        }
        catch ( Exception e )
        {
            Debug.print( "Unable to proceed the order[" + orderUId + "]", e );
        }
    }

    private void proceedPayment( Order order )
        throws CreditCardException {}

    private void proceedOrder( Order order )
        throws Exception
    {
        order.changeStatus( OrderStatus.TO_DELIVER );
        notifyCustomer( order );
    }

    private void cancelOrder( Order order )
        throws Exception
    {
        order.changeStatus( OrderStatus.CANCELLED );
        notifyCustomer( order );
    }

    private void notifyCustomer( Order order )
        throws Exception
    {
    	Customer customer = order.getCustomer(  );
    	if ( customer == null)
    	{
    		Debug.print( "No customer to notify" );
    		return;
    	}
    	 
        String to = customer.getEmail(  );
        String subject = "[Petstore] Order Confirmation";
        String body = toHtml( order );
        JMSUtil.sendToJMSQueue( JNDINames.QUEUE_MAIL, new Email( to, subject, body ), false );
    }

    private String toHtml( Order order )
    {
        StringBuffer buff = new StringBuffer(  );

        /* Id/Status */
        buff.append( "<table border='1' width='100%'>" );
        buff.append( "<tr><td width='10%'><b>Order ID:</b></td><td>" + order.getOrderUId(  ) + "</td></tr>" );
        buff.append( "<tr><td width='10%'><b>Status:</b></td><td>" + order.getStatus(  ) + "</td></tr>" );
        buff.append( "</table>" );

        /* Shipping/Bill address Address */
        buff.append( "<table border='1' width='100%'><tr>" );
        buff.append( "<td width='10%' valign='top'><b>Address:</b></td>" );
        buff.append( "<td>" );
        buff.append( order.getStreet1(  ) + "<br>" );
        buff.append( order.getStreet2(  ) + "<br>" );
        buff.append( order.getCity(  ) + "," + order.getState(  ) + "<br>" );
        buff.append( order.getZipcode(  ) + "<br>" );
        buff.append( order.getCountry(  ) );
        buff.append( "</td>" );
        buff.append( "</tr></table>" );

        /* Items */
        Collection orderItems = order.getOrderItems(  );
        buff.append( "<table border='1' width='100%'>" );
        buff.append( "<tr>" );
        buff.append( "<th bgcolor='#c0c0c0'>ID</td>" );
        buff.append( "<th bgcolor='#c0c0c0'>Description</th>" );
        buff.append( "<th bgcolor='#c0c0c0'>Unit Price</th>" );
        buff.append( "<th bgcolor='#c0c0c0'>Quantity</th>" );
        buff.append( "<th bgcolor='#c0c0c0'>&nbsp;</th>" );
        buff.append( "</tr>" );

        for ( Iterator it = orderItems.iterator(  ); it.hasNext(  ); )
        {
            OrderItem orderItem = ( OrderItem ) it.next(  );
            buff.append( toHtml( orderItem ) );
        }

        buff.append( "<tr>" );
        buff.append( "<td colspan=4 align=right bgcolor='#c0c0c0'>TOTAL:</td>" );
        buff.append( "<td bgcolor='#c0c0c0'><b>" + order.calculateTotal(  ) + "<b></td>" );
        buff.append( "</tr>" );
        buff.append( "</table>" );

        return buff.toString(  );
    }

    private String toHtml( OrderItem orderItem )
    {
        StringBuffer   buff = new StringBuffer(  );

        buff.append( "<tr>" );
        buff.append( "<td>" + orderItem.getItem(  ).getItemId(  ) + "</td>>" );
        buff.append( "<td>" + orderItem.getItem(  ).getDescription(  ) + "<br>" );
        buff.append( "<td>" + orderItem + "</td>" );
        buff.append( "<td>" + orderItem + "</td>" );
        buff.append( "<td>" + orderItem.calculateSubTotal(  ) + "</td>" );
        buff.append( "</tr>" );

        return buff.toString(  );
    }
}
