package org.jboss.ejb3.test.regression.ejbthree440.session.i;

import javax.ejb.Remote;

import org.jboss.ejb3.test.regression.ejbthree440.model.Resource;
import org.jboss.serial.io.MarshalledObject;


@Remote
public interface IInheritanceDemo {
  void create();
  Resource read();
  void remove();

   MarshalledObject readFromMO();
}
