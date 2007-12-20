/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.hibernate.ejb.packaging.PersistenceMetadata;
import org.hibernate.ejb.packaging.XmlHelper;
import org.hibernate.ejb.HibernatePersistence;
import org.hibernate.ejb.util.ConfigurationHelper;
import org.hibernate.util.StringHelper;
import org.hibernate.cfg.EJB3DTDEntityResolver;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceUnitTransactionType;
import java.net.URL;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

/**
 * Persistence.xml handler
 *
 * THIS CLASS SHOULD BE REMOVED WHEN HEM FIXES default transactiontype bug
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author Emmanuel Bernard
 */
@Deprecated
public final class PersistenceXmlLoader
{
	private static Log log = LogFactory.getLog( PersistenceXmlLoader.class );

	private PersistenceXmlLoader() {
	}

	private static Document loadURL(URL configURL, EntityResolver resolver) throws Exception {
		InputStream is = configURL != null ? configURL.openStream() : null;
		if ( is == null ) {
			throw new IOException( "Failed to obtain InputStream from url: " + configURL );
		}
		List errors = new ArrayList();
		DocumentBuilderFactory docBuilderFactory = null;
		docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setValidating( true );
		docBuilderFactory.setNamespaceAware( true );
		try {
			//otherwise Xerces fails in validation
			docBuilderFactory.setAttribute( "http://apache.org/xml/features/validation/schema", true );
		}
		catch (IllegalArgumentException e) {
			docBuilderFactory.setValidating( false );
			docBuilderFactory.setNamespaceAware( false );
		}
		InputSource source = new InputSource( is );
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		docBuilder.setEntityResolver( resolver );
		docBuilder.setErrorHandler( new PersistenceXmlLoader.ErrorLogger( "XML InputStream", errors, resolver ) );
		Document doc = docBuilder.parse( source );
		if ( errors.size() != 0 ) {
			throw new PersistenceException( "invalid persistence.xml", (Throwable) errors.get( 0 ) );
		}
		return doc;
	}

   public static List<PersistenceMetadata> deploy(URL url, Map overrides, EntityResolver resolver) throws Exception {
      return deploy(url, overrides, resolver, PersistenceUnitTransactionType.RESOURCE_LOCAL);
   }


   public static List<PersistenceMetadata> deploy(URL url, Map overrides, EntityResolver resolver, PersistenceUnitTransactionType defaultTransactionType) throws Exception {
		Document doc = loadURL( url, resolver );
		Element top = doc.getDocumentElement();
		NodeList children = top.getChildNodes();
		ArrayList<PersistenceMetadata> units = new ArrayList<PersistenceMetadata>();
		for ( int i = 0; i < children.getLength() ; i++ ) {
			if ( children.item( i ).getNodeType() == Node.ELEMENT_NODE ) {
				Element element = (Element) children.item( i );
				String tag = element.getTagName();
				if ( tag.equals( "persistence-unit" ) ) {
					PersistenceMetadata metadata = parsePersistenceUnit( element, defaultTransactionType);
					//override properties of metadata if needed
					String provider = (String) overrides.get( HibernatePersistence.PROVIDER );
					if ( provider != null ) {
						metadata.setProvider( provider );
					}
					String transactionType = (String) overrides.get( HibernatePersistence.TRANSACTION_TYPE );
					if ( StringHelper.isNotEmpty( transactionType ) ) {
						metadata.setTransactionType( getTransactionType( transactionType ) );
					}
					String dataSource = (String) overrides.get( HibernatePersistence.JTA_DATASOURCE );
					if ( dataSource != null ) {
						metadata.setJtaDatasource( dataSource );
					}
					dataSource = (String) overrides.get( HibernatePersistence.NON_JTA_DATASOURCE );
					if ( dataSource != null ) {
						metadata.setNonJtaDatasource( dataSource );
					}
					Properties properties = metadata.getProps();
					ConfigurationHelper.overrideProperties( properties, overrides );
					units.add( metadata );
				}
			}
		}

		return units;
	}

	private static PersistenceMetadata parsePersistenceUnit(Element top, PersistenceUnitTransactionType defaultTransactionType)
			throws Exception {
		PersistenceMetadata metadata = new PersistenceMetadata();
		String puName = top.getAttribute( "name" );
		if ( StringHelper.isNotEmpty( puName ) ) {
			log.trace( "Persistent Unit name from persistence.xml: " + puName );
			metadata.setName( puName );
		}
		PersistenceUnitTransactionType transactionType = getTransactionType( top.getAttribute( "transaction-type" ) );
		//parsing a persistence.xml means we are in a JavaSE environment
		transactionType = transactionType != null ? transactionType : defaultTransactionType;
		metadata.setTransactionType( transactionType );
		NodeList children = top.getChildNodes();
		for ( int i = 0; i < children.getLength() ; i++ ) {
			if ( children.item( i ).getNodeType() == Node.ELEMENT_NODE ) {
				Element element = (Element) children.item( i );
				String tag = element.getTagName();
//				if ( tag.equals( "name" ) ) {
//					String puName = XmlHelper.getElementContent( element );
//					log.trace( "FOUND PU NAME: " + puName );
//					metadata.setName( puName );
//				}
//				else
				if ( tag.equals( "non-jta-data-source" ) ) {
					metadata.setNonJtaDatasource( XmlHelper.getElementContent( element ) );
				}
				else if ( tag.equals( "jta-data-source" ) ) {
					metadata.setJtaDatasource( XmlHelper.getElementContent( element ) );
				}
				else if ( tag.equals( "provider" ) ) {
					metadata.setProvider( XmlHelper.getElementContent( element ) );
				}
				else if ( tag.equals( "class" ) ) {
					metadata.getClasses().add( XmlHelper.getElementContent( element ) );
				}
				else if ( tag.equals( "mapping-file" ) ) {
					metadata.getMappingFiles().add( XmlHelper.getElementContent( element ) );
				}
				else if ( tag.equals( "jar-file" ) ) {
					metadata.getJarFiles().add( XmlHelper.getElementContent( element ) );
				}
				else if ( tag.equals( "exclude-unlisted-classes" ) ) {
					metadata.setExcludeUnlistedClasses( true );
				}
				else if ( tag.equals( "properties" ) ) {
					NodeList props = element.getChildNodes();
					for ( int j = 0; j < props.getLength() ; j++ ) {
						if ( props.item( j ).getNodeType() == Node.ELEMENT_NODE ) {
							Element propElement = (Element) props.item( j );
							if ( !"property".equals( propElement.getTagName() ) ) continue;
							String propName = propElement.getAttribute( "name" ).trim();
							String propValue = propElement.getAttribute( "value" ).trim();
							if ( StringHelper.isEmpty( propValue ) ) {
								//fall back to the natural (Hibernate) way of description
								propValue = XmlHelper.getElementContent( propElement, "" );
							}
							metadata.getProps().put( propName, propValue );
						}
					}

				}
			}
		}

		return metadata;
	}

	public static PersistenceUnitTransactionType getTransactionType(String elementContent) {
		if ( StringHelper.isEmpty( elementContent ) ) {
			return null; //PersistenceUnitTransactionType.JTA;
		}
		else if ( elementContent.equalsIgnoreCase( "JTA" ) ) {
			return PersistenceUnitTransactionType.JTA;
		}
		else if ( elementContent.equalsIgnoreCase( "RESOURCE_LOCAL" ) ) {
			return PersistenceUnitTransactionType.RESOURCE_LOCAL;
		}
		else {
			throw new PersistenceException( "Unknown TransactionType: " + elementContent );
		}
	}

	public static class ErrorLogger implements ErrorHandler
   {
		private String file;
		private List errors;
		private EntityResolver resolver;

		ErrorLogger(String file, List errors, EntityResolver resolver) {
			this.file = file;
			this.errors = errors;
			this.resolver = resolver;
		}

		public void error(SAXParseException error) {
			if ( resolver instanceof EJB3DTDEntityResolver) {
				if ( ( (EJB3DTDEntityResolver) resolver ).isResolved() == false ) return;
			}
			log.error( "Error parsing XML: " + file + '(' + error.getLineNumber() + ") " + error.getMessage() );
			errors.add( error );
		}

		public void fatalError(SAXParseException error) {
			log.error( "Error parsing XML: " + file + '(' + error.getLineNumber() + ") " + error.getMessage() );
			errors.add( error );
		}

		public void warning(SAXParseException warn) {
			log.warn( "Warning parsing XML: " + file + '(' + warn.getLineNumber() + ") " + warn.getMessage() );
		}
	}

}
