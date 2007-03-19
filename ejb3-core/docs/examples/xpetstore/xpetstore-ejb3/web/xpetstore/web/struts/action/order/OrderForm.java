package xpetstore.web.struts.action.order;

import xpetstore.domain.order.ejb.Order;

import xpetstore.web.struts.action.cart.*;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @struts.form
 *      name="orderForm"
 */
public class OrderForm
    extends CartForm
{
   private Order _order = new Order(  );
   
    public OrderForm()
    {
       
    }

    public Order getOrder(  )
    {
        return _order;
    }

    public void setOrder( Order order )
    {
        _order = order;
    }
}
