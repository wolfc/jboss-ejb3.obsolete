package xpetstore.services.cart.model;

import java.io.Serializable;

import java.util.Comparator;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 */
public class CartItem
    implements Serializable
{
    //~ Instance fields --------------------------------------------------------

    private String itemId;
    private String productId;
    private String name;
    private String description;
    private int    quantity;
    private double unitCost;

    //~ Constructors -----------------------------------------------------------

    public CartItem( String itemId,
                     String productId,
                     String name,
                     String description,
                     int    quantity,
                     double unitCost )
    {
        this.itemId      = itemId;
        this.productId   = productId;
        this.name        = name;
        this.description = description;
        this.quantity    = quantity;
        this.unitCost    = unitCost;
    }

    //~ Methods ----------------------------------------------------------------

    public String getItemId(  )
    {
        return itemId;
    }

    public String getProductId(  )
    {
        return productId;
    }

    public String getName(  )
    {
        return name;
    }

    public String getDescription(  )
    {
        return description;
    }

    public int getQuantity(  )
    {
        return quantity;
    }

    public double getUnitCost(  )
    {
        return unitCost;
    }

    public double getTotalCost(  )
    {
        return quantity * unitCost;
    }

    //~ Inner Classes ----------------------------------------------------------

    public static class ItemIdComparator
        implements Comparator
    {
        //~ Methods ------------------------------------------------------------

        public int compare( Object o1,
                            Object o2 )
        {
            if ( ( o1 instanceof CartItem ) && ( o2 instanceof CartItem ) )
            {
                return ( ( CartItem ) o1 ).getItemId(  ).compareTo( ( ( CartItem ) o2 ).getItemId(  ) );
            }
            else
            {
                return 0;
            }
        }
    }
}
