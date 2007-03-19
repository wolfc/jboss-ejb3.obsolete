/***********************************************************************************************************************************
**
** Research In Motion Proprietary. Do not reproduce without permission in writing.
**         Copyright (c) 2006 Research In Motion Limited
**                 All rights reserved
**
************************************************************************************************************************************
*/
package org.jboss.ejb3.test.timestampentity;

import javax.ejb.Stateless;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.ejb3.test.timestampentity.entity.Table1IntegerEntity;
import org.jboss.ejb3.test.timestampentity.entity.Table1Key;
import org.jboss.ejb3.test.timestampentity.entity.Table1TimestampEntity;
import java.util.Date;

/***********************************************************************************************************************************
**
** Implements the Test Manager session bean.
** <p>
**
** @author  Jonathan Grills
** @version $Id: TestManagerBean.java
**
************************************************************************************************************************************
*/
@Stateless
//@RemoteBinding(clientBindUrl="sslsocket://0.0.0.0:3843", jndiBinding=TestManagerConstants.JNDI_BINDING)
@RemoteBinding(jndiBinding=TestManagerConstants.JNDI_BINDING)
//@SecurityDomain("bas")
public class TestManagerBean implements TestManager, TestManagerLocal {

/***********************************************************************************************************************************
**
** EntityManager.
**
************************************************************************************************************************************
*/
  @PersistenceContext EntityManager _entityManager;

/***********************************************************************************************************************************
**
** @see TestManagerCommon#findTestInteger(String, int, String)
**
************************************************************************************************************************************
*/
  public boolean findTestInteger (String keyField1, int keyField2, String keyField3) {

    final String METHOD_NAME = "findTestInteger";
    System.out.println(METHOD_NAME + ": Enter.");
    Table1Key table1Id = new Table1Key(keyField1, keyField2, keyField3);
    boolean isRecordFound = false;
    try {
      Table1IntegerEntity queryResult = _entityManager.find(Table1IntegerEntity.class, table1Id);
      isRecordFound = queryResult.equals(null);
    }
    catch (Exception e) {
      System.out.println(e.toString());
    }
    System.out.println(METHOD_NAME + ": Enter.");
    return isRecordFound;
  }

/***********************************************************************************************************************************
**
** @see TestManagerCommon#findTestTimestamp(String, int, String)
**
************************************************************************************************************************************
*/
  public boolean findTestTimestamp (String keyField1, int keyField2, String keyField3) {

    final String METHOD_NAME = "findTestTimestamp";
    System.out.println(METHOD_NAME + ": Enter.");
    Table1Key table1Id = new Table1Key(keyField1, keyField2, keyField3);
    boolean isRecordFound = false;
    try {
      Table1TimestampEntity queryResult = _entityManager.find(Table1TimestampEntity.class, table1Id);
      isRecordFound = queryResult.equals(null);
    }
    catch (Exception e) {
      System.out.println(e.toString());
    }
    System.out.println(METHOD_NAME + ": Enter.");
    return isRecordFound;
  }

/***********************************************************************************************************************************
**
** Create a table1IntegerEntity object
**
** @param      keyField1   the table1IntegerEntity object
** @param      keyField2   the table1IntegerEntity object
** @param      keyField3   the table1IntegerEntity object
** @param      field1   the table1IntegerEntity object
** @param      field2   the table1IntegerEntity object
** @param      field3   the table1IntegerEntity object
**
** @return     the primary key
**
************************************************************************************************************************************
*/
  public Table1Key createTestInteger (
  String keyField1, int keyField2, String keyField3, String field1, String field2, Date field3) {

    final String METHOD_NAME = "createTestInteger";
    System.out.println(METHOD_NAME + ": Enter.");
    Table1Key table1Key = new Table1Key(keyField1, keyField2, keyField3);
    Table1IntegerEntity table1Integer = new Table1IntegerEntity();
    table1Integer.setId(table1Key);
    table1Integer.setField1(field1);
    table1Integer.setField2(field2);
    table1Integer.setField3(field3);
    _entityManager.persist(table1Integer);
    System.out.println(METHOD_NAME + ": Exit.");
    return table1Key;
  }

/***********************************************************************************************************************************
**
** Updates a table1IntegerEntity object
**
** @param      keyField1   the table1IntegerEntity object
** @param      keyField2   the table1IntegerEntity object
** @param      keyField3   the table1IntegerEntity object
** @param      field1   the table1IntegerEntity object
** @param      field2   the table1IntegerEntity object
** @param      field3   the table1IntegerEntity object
**
** @return     the primary key
**
************************************************************************************************************************************
*/
  public Table1Key updateTestInteger (
  String keyField1, int keyField2, String keyField3, String field1, String field2, Date field3) {

    final String METHOD_NAME = "updateTestInteger";
    System.out.println(METHOD_NAME + ": Enter.");
    Table1IntegerEntity table1Integer;
    Table1Key table1Key = new Table1Key(keyField1, keyField2, keyField3);
    try {
      table1Integer = _entityManager.find(Table1IntegerEntity.class, table1Key);
      table1Integer.setField1(field1);
      table1Integer.setField2(field2);
      table1Integer.setField3(field3);
      _entityManager.merge(table1Integer);
    }
    catch (Exception e) {
      System.out.println(e.toString());
    }
    System.out.println(METHOD_NAME + ": Exit.");
    return table1Key;
  }

/***********************************************************************************************************************************
**
** Create a table1TimestampEntity object
**
** @param      keyField1   the table1IntegerEntity object
** @param      keyField2   the table1IntegerEntity object
** @param      keyField3   the table1IntegerEntity object
** @param      field1   the table1IntegerEntity object
** @param      field2   the table1IntegerEntity object
** @param      field3   the table1IntegerEntity object
**
** @return     the primary key
**
************************************************************************************************************************************
*/
  public Table1Key createTestTimestamp (
  String keyField1, int keyField2, String keyField3, String field1, String field2, Date field3) {

    final String METHOD_NAME = "createTestTimestamp";
    System.out.println(METHOD_NAME + ": Enter.");
    Table1Key table1Key = new Table1Key(keyField1, keyField2, keyField3);
    Table1TimestampEntity table1Timestamp = new Table1TimestampEntity();
    table1Timestamp.setId(table1Key);
    table1Timestamp.setField1(field1);
    table1Timestamp.setField2(field2);
    table1Timestamp.setField3(field3);
    _entityManager.persist(table1Timestamp);
    System.out.println(METHOD_NAME + ": Exit.");
    return table1Key;
  }

/***********************************************************************************************************************************
**
** Updates a table1TimestampEntity object
**
** @param      keyField1   the table1IntegerEntity object
** @param      keyField2   the table1IntegerEntity object
** @param      keyField3   the table1IntegerEntity object
** @param      field1   the table1IntegerEntity object
** @param      field2   the table1IntegerEntity object
** @param      field3   the table1IntegerEntity object
**
** @return     the primary key
**
************************************************************************************************************************************
*/
  public Table1Key updateTestTimestamp (
  String keyField1, int keyField2, String keyField3, String field1, String field2, Date field3) {

    final String METHOD_NAME = "updateTestTimestamp";
    System.out.println(METHOD_NAME + ": Enter.");
    Table1TimestampEntity table1Timestamp;
    Table1Key table1Key = new Table1Key(keyField1, keyField2, keyField3);
    try {
      table1Timestamp = _entityManager.find(Table1TimestampEntity.class, table1Key);
      table1Timestamp.setField1(field1);
      table1Timestamp.setField2(field2);
      table1Timestamp.setField3(field3);
      _entityManager.merge(table1Timestamp);
    }
    catch (Exception e) {
      System.out.println(e.toString());
    }
    System.out.println(METHOD_NAME + ": Exit.");
    return table1Key;
  }
}
