package xpetstore.web.taglib;

import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import xpetstore.util.CreditCardNames;


/**
 * This is an example of JSP taglib.
 * This taglib show the list of all the credit card to use for payments
 *
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @jsp.tag
 *         name="creditcard"
 *         body-content="empty"
 *         display-name="xPetStore CreditCard Tag"
 */
public class CreditCardTag
    extends TagSupport
{
    //~ Instance fields --------------------------------------------------------

    private String _name;
    private String _css;
    private String _value;

    //~ Methods ----------------------------------------------------------------

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag(  )
        throws JspException
    {
        try
        {
            StringBuffer buff = new StringBuffer(  );

            /* <select> */
            buff.append( "<select name='" ).append( _name ).append( "'" );

            if ( _css != null )
            {
                buff.append( " class='" ).append( _css ).append( "'" );
            }

            buff.append( ">" );

            Iterator it = CreditCardNames.all(  ).iterator(  );

            while ( it.hasNext(  ) )
            {
                String cc = it.next(  ).toString(  );

                /* <option> */
                buff.append( "<option value='" ).append( cc ).append( "'" );

                if ( cc.equals( _value ) )
                {
                    buff.append( " selected" );
                }

                buff.append( ">" );

                /* value */
                buff.append( cc );

                /* </option> */
                buff.append( "</option>" );
            }

            /* </select> */
            buff.append( "</select>" );

            pageContext.getOut(  ).println( buff.toString(  ) );

            System.out.println( "customerForm=" + pageContext.getAttribute( "customerForm" ) );

            return EVAL_PAGE;
        }
        catch ( Exception e )
        {
            throw new JspException( e );
        }
    }

    /**
     * Returns the css.
     * @return String
     *
     * @jsp.attribute
     */
    public String getCss(  )
    {
        return _css;
    }

    /**
     * Returns the name.
     * @return String
     *
     * @jsp.attribute
     *         required="true"
     */
    public String getName(  )
    {
        return _name;
    }

    /**
     * Returns the value.
     * @return String
     *
     * @jsp.attribute
     *      rtexprvalue="true"
     */
    public String getValue(  )
    {
        return _value;
    }

    /**
     * Sets the css.
     * @param css The css to set
     */
    public void setCss( String css )
    {
        _css = css;
    }

    /**
     * Sets the name.
     * @param name The name to set
     */
    public void setName( String name )
    {
        _name = name;
    }

    /**
     * Sets the value.
     * @param value The value to set
     */
    public void setValue( String value )
    {
        _value = value;
    }
}
