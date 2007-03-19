/*
 * Created on 24-Feb-2003
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code Template
 */
package xpetstore.web.webwork.action.signon;

import xpetstore.web.webwork.action.BaseAction;


/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 *
 * @webwork.action
 *   name="logout"
 *   success="index.action"
 */
public class LogoutAction
    extends BaseAction
{
    //~ Methods ----------------------------------------------------------------

    /**
     * @see webwork.action.ActionSupport#doExecute()
     */
    protected String doExecute(  )
        throws Exception
    {
        clearSession(  );

        return SUCCESS;
    }
}
