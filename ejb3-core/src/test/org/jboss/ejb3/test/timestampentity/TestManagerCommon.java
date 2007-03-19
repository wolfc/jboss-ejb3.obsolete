/***********************************************************************************************************************************
**
** Research In Motion Proprietary. Do not reproduce without permission in writing.
**         Copyright (c) 2006 Research In Motion Limited
**                 All rights reserved
**
************************************************************************************************************************************
*/
package org.jboss.ejb3.test.timestampentity;

import org.jboss.ejb3.test.timestampentity.entity.Table1Key;

import java.util.Date;

/***********************************************************************************************************************************
**
** Defines the Test Manager session bean interface.
** <p>
**
** @author  Jonathan Grills
** @version $Id: TestManagerCommon.java
**
************************************************************************************************************************************
*/
public interface TestManagerCommon {

/***********************************************************************************************************************************
**
** Method to return a specific table1Integer record.
**
** @param      field1   the field1
** @param      field2   the field2
** @param      field3   the field3
**
** @return     the primary key
**
************************************************************************************************************************************
*/
  public boolean findTestInteger (String field1, int field2, String field3);

/***********************************************************************************************************************************
**
** Method to return a specific table1Timestamp record.
**
** @param      field1   the field1
** @param      field2   the field2
** @param      field3   the field3
**
** @return     the primary key
**
************************************************************************************************************************************
*/
  public boolean findTestTimestamp (String field1, int field2, String field3);

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
  public Table1Key createTestInteger (String keyField1, int keyField2, String keyField3, String field1, String field2, Date field3);
  public Table1Key updateTestInteger (String keyField1, int keyField2, String keyField3, String field1, String field2, Date field3);

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
  String keyField1, int keyField2, String keyField3, String field1, String field2, Date field3);
  public Table1Key updateTestTimestamp (
  String keyField1, int keyField2, String keyField3, String field1, String field2, Date field3);
}