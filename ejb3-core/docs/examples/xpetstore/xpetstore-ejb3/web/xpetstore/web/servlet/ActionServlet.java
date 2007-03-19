/*
 * Created on Apr 28, 2003
 */
package xpetstore.web.servlet;

/**
 *
 * @web.servlet
 *      name="action"
 *      display-name="xPetstore Struts Action Servlet"
 *      load-on-startup="1"
 * @web.servlet-mapping
 * 		url-pattern="*.jspa"
 * @web.servlet-init-param
 * 		name="application"
 * 		value="Resources"
 * 		descriptiorn="Application resource bundle"
 * @web.servlet-init-param
 * 		name="config"
 * 		value="/WEB-INF/struts-config.xml"
 * 		descriptiorn="Struts configuration file"
 * @ web.ejb-local-ref
 * 		name="ejb/Petstore"
 * 		type="Session"
 * 		local="xpetstore.services.petstore.ejb.Petstore"
 * 		link="Petstore"
 * @ web.ejb-local-ref
 * 		name="ejb/Cart"
 * 		type="Session"
 * 		local="xpetstore.services.cart.ejb.Cart"
 * 		link="Cart"
 */
public class ActionServlet extends org.apache.struts.action.ActionServlet {

}
