package xpetstore.util;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 */
public class Page
    implements Serializable
{
    //~ Static fields/initializers ---------------------------------------------

    public static final Page EMPTY_PAGE = new Page( Collections.EMPTY_LIST, 0, false );

    //~ Instance fields --------------------------------------------------------

    Collection list;
    int        start;
    boolean    hasNext;

    //~ Constructors -----------------------------------------------------------

    public Page( Collection col,
                 int        start,
                 boolean    hasNext )
    {
        this.list    = new ArrayList( col );
        this.start   = start;
        this.hasNext = hasNext;
    }

    //~ Methods ----------------------------------------------------------------

    public Collection getList(  )
    {
        return list;
    }

    public boolean isNextPageAvailable(  )
    {
        return hasNext;
    }

    public boolean isPreviousPageAvailable(  )
    {
        return start > 0;
    }

    public int getStartOfNextPage(  )
    {
        return start + list.size(  );
    }

    public int getStartOfPreviousPage(  )
    {
        return Math.max( start - list.size(  ), 0 );
    }

    public int getSize(  )
    {
        return list.size(  );
    }
}
