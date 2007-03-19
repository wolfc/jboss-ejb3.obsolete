package xpetstore.web.struts.action.item;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import xpetstore.domain.catalog.ejb.Item;
import xpetstore.domain.catalog.ejb.Product;

import xpetstore.services.petstore.ejb.Petstore;

import xpetstore.web.struts.action.BaseAction;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @struts.action
 *      name="itemForm"
 *      path="/item"
 *      scope="request"
 *      validate="false"
 *
 * @struts.action-forward
 *      name="success"
 *      path="/item.jsp"
 */
public class ItemAction
    extends BaseAction
{
    //~ Methods ----------------------------------------------------------------

    /**
     * @see xpetstore.web.struts.action.BaseAction#doExecute(ActionMapping, ActionForm, HttpServletRequest, HttpServletResponse)
     */
    protected ActionForward doExecute( ActionMapping       mapping,
                                       ActionForm          form,
                                       HttpServletRequest  request,
                                       HttpServletResponse response )
        throws Exception
    {
        ItemForm      frm = ( ItemForm ) form;
        String        itemId = frm.getItemId(  );
        Petstore petstore = getPetstore(  );

        /* Item */
        Item item = petstore.getItem( itemId );
        frm.setItem( item );

        /* Product */
        Product product = petstore.getProductByItem( itemId );
        frm.setProduct( product );

        return mapping.findForward( SUCCESS );
    }
}
