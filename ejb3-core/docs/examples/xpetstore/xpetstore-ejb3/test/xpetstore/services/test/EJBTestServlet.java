/*
 * Created on May 1, 2003
 */
package xpetstore.services.test;

import org.junitee.servlet.JUnitEEServlet;

/**
 * @author <a href="mailto:tchbansi@sourceforge.net">Herve Tchepannou</a>
 * 
 * @web.servlet
 *      name="EJBTestServlet"
 *      display-name="Servlet for testing EJB"
 *      load-on-startup="1"
 * @web.servlet-mapping
 * 		url-pattern="/TestServlet/*"
 * @ web.ejb-local-ref
 * 		name="ejb/Petstore"
 * 		type="Session"
 * 		local="xpetstore.services.petstore.ejb.Petstore"
 * 		link="Petstore"
 * @ web.ejb-local-ref
 * 		name="ejb/Cart"
 * 		type="Session"
 *      home="xpetstore.services.cart.interfaces.CartLocalHome"
 * 		local="xpetstore.services.cart.ejb.Cart"
 * 		link="Cart"
 */
public class EJBTestServlet extends JUnitEEServlet {

}
