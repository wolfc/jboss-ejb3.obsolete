package xpetstore.domain.order.model;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 */
public abstract class OrderStatus
{
    //~ Static fields/initializers ---------------------------------------------

    public static final String PENDING = "pending";
    public static final String TO_DELIVER = "to-deliver";
    public static final String WAITING = "waiting";
    public static final String DELIVERED = "delivered";
    public static final String CANCELLED = "cancelled";
}
