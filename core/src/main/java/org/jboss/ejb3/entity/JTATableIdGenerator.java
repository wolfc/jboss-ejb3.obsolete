/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ejb3.entity;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.exception.JDBCExceptionHelper;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerationException;
import org.hibernate.id.IdentifierGeneratorFactory;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.transaction.JBossTransactionManagerLookup;
import org.hibernate.transaction.TransactionManagerLookup;
import org.hibernate.type.Type;
import org.hibernate.util.PropertiesHelper;

/**
 * A hilo <tt>IdentifierGenerator</tt> that uses a database
 * table to store the last generated value.
 * <p/>
 * <p/>
 * This implementation is solely for use inside JBoss using JTA for transactions.
 * </p>
 * <p/>
 * TODO implement sequence allocation
 *
 * @author <a href="mailto:kr@hbt.de">Klaus Richarz</a>.
 * @version <tt>$Revision$</tt>
 * @see org.hibernate.id.TableGenerator
 * @see javax.persistence.TableGenerator
 */
public class JTATableIdGenerator implements PersistentIdentifierGenerator, Configurable
{
   /* COLUMN and TABLE should be renamed but it would break the public API */
   /**
    * The column parameter
    */
   public static final String COLUMN = "column";

   /**
    * Default column name
    */
   public static final String DEFAULT_COLUMN_NAME = "next_hi";

   /**
    * The table parameter
    */
   public static final String TABLE = "table";

   /**
    * Default table name
    */
   public static final String DEFAULT_TABLE_NAME = "next_hi";

   /**
    * The allocation-size parameter
    */
   public static final String ALLOCATION_SIZE = "allocationSize";

   /**
    * Default allocation-size
    */
   public static final int DEFAULT_ALLOCATION_SIZE = 20;

   /**
    * logger for JTATableGenerator
    */
   private static final Log log = LogFactory.getLog(JTATableIdGenerator.class);

   /**
    * Holds the name where this generator gets its sequence from
    */
   private String tableName;

   /**
    * Holds the name ofthe column where the next sequence value is stored
    */
   private String columnName;

   /**
    * Holds the sql query to retrieve the next high value
    */
   private String query;

   /**
    * Holds the sql query to increment the sequence
    */
   private String update;

   /**
    * Holds the transaction manager lookup object
    */
   private TransactionManagerLookup transactionManagerLookup;

   /**
    * Holds the class type for the sequence value returned by generate()
    */
   private Class returnClass;

   /**
    * Holds the size for the sequence increment. The allocated sequences are managed in memory
    * and may be lost if the system stops.
    */
   private int allocationSize;

   public void configure(Type type, Properties params, Dialect dialect)
   {
      this.tableName = PropertiesHelper.getString(TABLE, params, DEFAULT_TABLE_NAME);
      this.columnName = PropertiesHelper.getString(COLUMN, params, DEFAULT_COLUMN_NAME);
      this.allocationSize = PropertiesHelper.getInt(ALLOCATION_SIZE, params, DEFAULT_ALLOCATION_SIZE);
      String schemaName = params.getProperty(SCHEMA);
      String catalogName = params.getProperty(CATALOG);

      if (true) throw new RuntimeException("DOES ANYBODY USE THIS?  It IS CURRENTLY BROKEN");

      /*
      getSchemaSeparator does not exist in hibernate anymore since 3.1 release

      // prepare table name
      if (tableName.indexOf(dialect.getSchemaSeparator()) < 0)
      {
         tableName = Table.qualify(catalogName, schemaName, tableName, dialect.getSchemaSeparator());
      }
      */

      // prepare SQL statements
      query = "select " +
              columnName +
              " from " +
              dialect.appendLockHint(LockMode.UPGRADE, tableName) +
              dialect.getForUpdateString();
      update = "update " +
               tableName +
               " set " +
               columnName +
               " = ? where " +
               columnName +
               " = ?";

      // set up transaction manager lookup
      // only JBoss transaction manager is supported
      transactionManagerLookup = new JBossTransactionManagerLookup();

      // set the sequence type that should be returned
      returnClass = type.getReturnedClass();

      // debug chosen configuration
      if (log.isDebugEnabled())
      {
         log.debug("configuring id generator: " + this.getClass().getName());
         log.debug("tableName=" + tableName);
         log.debug("columnName=" + columnName);
         log.debug("allocationSize=" + allocationSize);
         log.debug("query=" + query);
         log.debug("update=" + update);
         log.debug("returnClass=" + returnClass);
      }
   }

   public synchronized Serializable generate(SessionImplementor session, Object object)
           throws HibernateException
   {
      // get TransactionManager from JNDI
      // no JNDI properties provided -> we are in the container
      TransactionManager tm = transactionManagerLookup.getTransactionManager(new Properties());
      Transaction surroundingTransaction = null;  // for resuming in finally block
      Connection conn = null; // for ressource cleanup
      String sql = null; // for exception
      try
      {
         long result; // holds the resulting sequence value

         // prepare a new transaction context for the generator
         surroundingTransaction = tm.suspend();
         if (log.isDebugEnabled())
         {
            log.debug("surrounding tx suspended");
         }
         tm.begin();

         // get connection from managed environment
         conn = session.getBatcher().openConnection();

         // execute fetching of current sequence value
         sql = query;
         PreparedStatement qps = conn.prepareStatement(query);
         try
         {
            ResultSet rs = qps.executeQuery();
            if (!rs.next())
            {
               String err = "could not read sequence value - you need to populate the table: " + tableName;
               log.error(err);
               throw new IdentifierGenerationException(err);
            }
            result = rs.getLong(1);
            rs.close();
         }
         catch (SQLException sqle)
         {
            log.error("could not read a sequence value", sqle);
            throw sqle;
         }
         finally
         {
            qps.close();
         }

         // increment sequence value
         sql = update;
         long sequence = result + 1;
         PreparedStatement ups = conn.prepareStatement(update);
         try
         {
            ups.setLong(1, sequence);
            ups.setLong(2, result);
            ups.executeUpdate();
         }
         catch (SQLException sqle)
         {
            log.error("could not update sequence value in: " + tableName, sqle);
            throw sqle;
         }
         finally
         {
            ups.close();
         }

         // commit transaction to ensure updated sequence is not rolled back
         tm.commit();

         // transform sequence to the desired type and return the value
         Number typedSequence = IdentifierGeneratorFactory.createNumber(sequence, returnClass);
         if (log.isDebugEnabled())
         {
            log.debug("generate() returned: " + typedSequence);
         }
         return typedSequence;
      }
      catch (SQLException sqle)
      {
         throw JDBCExceptionHelper.convert(session.getFactory().getSQLExceptionConverter(),
                                           sqle,
                                           "could not get or update next value",
                                           sql);
      }
      catch (Exception e)
      {
         try
         {
            tm.rollback();
            throw new HibernateException(e);
         }
         catch (SystemException e1)
         {
            throw new HibernateException(e1);
         }
      }
      finally
      {
         if (conn != null)
            try
            {
               conn.close();
            }
            catch (SQLException e)
            {
               // ignore exception
            }
         // switch back to surrounding transaction context
         if (surroundingTransaction != null)
         {
            try
            {
               tm.resume(surroundingTransaction);
               if (log.isDebugEnabled())
               {
                  log.debug("surrounding tx resumed");
               }
            }
            catch (Exception e)
            {
               throw new HibernateException(e);
            }
         }
      }
   }


   public String[] sqlCreateStrings(Dialect dialect) throws HibernateException
   {
      return new String[]{
         "create table " + tableName + " ( " + columnName + " " + dialect.getTypeName(Types.BIGINT) + " )",
         "insert into " + tableName + " values ( 0 )"
      };
   }

   public String[] sqlDropStrings(Dialect dialect)
   {
      //return "drop table " + tableName + dialect.getCascadeConstraintsString();
      StringBuffer sqlDropString = new StringBuffer()
              .append("drop table ");
      if (dialect.supportsIfExistsBeforeTableName())
      {
         sqlDropString.append("if exists ");
      }
      sqlDropString.append(tableName)
              .append(dialect.getCascadeConstraintsString());
      if (dialect.supportsIfExistsAfterTableName())
      {
         sqlDropString.append(" if exists");
      }
      return new String[]{sqlDropString.toString()};
   }

   public Object generatorKey()
   {
      return tableName;
   }
}
