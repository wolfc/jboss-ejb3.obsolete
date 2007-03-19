/***********************************************************************************************************************************
**
** Research In Motion.  Do not reproduce without permission in writing.
**
** Copyright (c) 2006 Research In Motion.
** All rights reserved.
**
************************************************************************************************************************************
*/
package org.jboss.ejb3.test.timestampentity.entity;

import javax.persistence.Embeddable;

/***********************************************************************************************************************************
**
** This is a primary key class for the BMSTable1 table...Generated 27-Nov-2006 9:48:27 AM by Hibernate Tools 3.2.0.beta7
**
** @author     jgrills
** @version    T.B.D
**
************************************************************************************************************************************
*/
@Embeddable
public class Table1Key implements java.io.Serializable {

/***********************************************************************************************************************************
**
** The locale identifer
**
************************************************************************************************************************************
*/
  private String _keyField1;

/***********************************************************************************************************************************
**
** The plugin identifier
**
************************************************************************************************************************************
*/
  private int _keyField2;

/***********************************************************************************************************************************
**
** The string identifier
**
************************************************************************************************************************************
*/
  private String _keyField3;

/***********************************************************************************************************************************
**
** The default constructor for a Table1 primary key
**
************************************************************************************************************************************
*/
  public Table1Key () {

  }

/***********************************************************************************************************************************
**
** The full constructor for a Table1 primary key
**
** @param      keyField1   the localeIdentifier
** @param      keyField2   the localeIdentifier
** @param      keyField3   the localeIdentifier
**
************************************************************************************************************************************
*/
  public Table1Key (String keyField1, int keyField2, String keyField3) {

    _keyField1 = keyField1;
    _keyField2 = keyField2;
    _keyField3 = keyField3;
  }

/***********************************************************************************************************************************
**
** The get method for the keyField1 identifier
**
** @return     the keyField1 identifier
**
************************************************************************************************************************************
*/
  public String getKeyField1 () {

    return _keyField1;
  }

/***********************************************************************************************************************************
**
** The set method for the keyField1 identifier
**
** @param      keyField1   the keyField1 identifier
**
************************************************************************************************************************************
*/
  public void setKeyField1 (String keyField1) {

    _keyField1 = keyField1;
  }

/***********************************************************************************************************************************
**
** The get method for the keyField2 identifier
**
** @return     the keyField2 identifier
**
************************************************************************************************************************************
*/
  public int getKeyField2 () {

    return _keyField2;
  }

/***********************************************************************************************************************************
**
** The set method for the keyField2 identifier
**
** @param      keyField2   the keyField2 identifier
**
************************************************************************************************************************************
*/
  public void setKeyField2 (int keyField2) {

    _keyField2 = keyField2;
  }

/***********************************************************************************************************************************
**
** The get method for the keyField3 identifier
**
** @return     the keyField3 identifier
**
************************************************************************************************************************************
*/
  public String getKeyField3 () {

    return _keyField3;
  }

/***********************************************************************************************************************************
**
** The set method for the keyField3 identifier
**
** @param      keyField3   the keyField3 identifier
**
************************************************************************************************************************************
*/
  public void setKeyField3 (String keyField3) {

    _keyField3 = keyField3;
  }

/***********************************************************************************************************************************
**
** The primary key class' equals method
**
** @param      other   a comparison object
**
************************************************************************************************************************************
*/
  public boolean equals (Object other) {

    if ((this == other))
    return true;
    if ((other == null))
    return false;
    if (!(other instanceof Table1Key))
    return false;
    Table1Key castOther = (Table1Key) other;

    return ((this.getKeyField1() == castOther.getKeyField1()) || (this.getKeyField1() != null && castOther.getKeyField1() != null
    && this.getKeyField1().equals(castOther.getKeyField1()))) && (this.getKeyField2() == castOther.getKeyField2())
    && ((this.getKeyField3() == castOther.getKeyField3()) || (this.getKeyField3() != null && castOther.getKeyField3() != null
    && this.getKeyField3().equals(castOther.getKeyField3())));
  }

/***********************************************************************************************************************************
**
** The primary key class' hashCode method
**
************************************************************************************************************************************
*/
  public int hashCode () {

    int result = 17;

    result = 37 * result + (getKeyField1() == null ? 0 : this.getKeyField1().hashCode());
    result = 37 * result + this.getKeyField2();
    result = 37 * result+ (getKeyField3() == null ? 0 : this.getKeyField3().hashCode());
    return result;
  }
}
