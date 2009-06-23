/**
 *
 */
package org.jboss.ejb3.metadata.spi.javaee;

/**
 * IdMetaData
 *
 * Most of the metadata have ids. This interface
 * represents the metadata of such elements.
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public interface IdMetaData
{

   /**
    * Returns the id
    *
    */
   java.lang.String getId();

   /**
    * Sets the id
    *
    *
    */
   void setId(java.lang.String id);
}
