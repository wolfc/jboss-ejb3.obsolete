package org.jboss.tutorial.entity.security.bean;

import org.jboss.tutorial.entity.security.bean.AllEntity;
import org.jboss.tutorial.entity.security.bean.SomeEntity;
import org.jboss.tutorial.entity.security.bean.StarEntity;

/**
 *
 * @author <a href="mailto:kabir.khan@jboss.org">Kabir Khan</a>
 * @version $Revision$
 */
public interface Stateless
{
   int unchecked(int i);

   int checked(int i);

   AllEntity insertAllEntity();

   AllEntity readAllEntity(int key);

   void updateAllEntity(AllEntity e);

   void deleteAllEntity(AllEntity e);

   StarEntity insertStarEntity();

   StarEntity readStarEntity(int key);

   void updateStarEntity(StarEntity e);

   void deleteStarEntity(StarEntity e);

   SomeEntity insertSomeEntity();

   SomeEntity readSomeEntity(int key);

   void updateSomeEntity(SomeEntity e);

   void deleteSomeEntity(SomeEntity e);

}
