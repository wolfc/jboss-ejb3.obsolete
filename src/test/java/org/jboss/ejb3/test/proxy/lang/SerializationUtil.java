package org.jboss.ejb3.test.proxy.lang;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 
 * SerializationUtil - Utility class for creating a copy of {@link Serializable} object
 * by serializing and de-serializing the object.
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class SerializationUtil
{

   /**
    * Creates a copy of the <code>originalObject</code> by serializing/de-serializing
    * the object.
    *  
    * @param originalObject The object whose copy will be created 
    * @return Returns a copy of the <code>originalObject</code>
    * 
    * @throws Exception
    */
   public static Serializable getCopy(Serializable originalObject) throws Exception
   {

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
      objectOutputStream.writeObject(originalObject);

      ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
      ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
      Object copyOfObject = objectInputStream.readObject();

      return (Serializable) copyOfObject;
   }
}
