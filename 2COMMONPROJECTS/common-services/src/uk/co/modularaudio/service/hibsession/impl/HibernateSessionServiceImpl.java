/**
 *
 * Copyright (C) 2015 - Daniel Hams, Modular Audio Limited
 *                      daniel.hams@gmail.com
 *
 * Mad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mad.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.co.modularaudio.service.hibsession.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.configuration.ConfigurationServiceHelper;
import uk.co.modularaudio.service.hibsession.HibernateSessionService;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.hibernate.HibernateExceptionHandler;
import uk.co.modularaudio.util.hibernate.component.HibernatePersistedBeanDefinition;
import uk.co.modularaudio.util.hibernate.component.TableNamePrefixer;
import uk.co.modularaudio.util.io.IOUtils;

/**
 * Implementation class that is responsible of the configuration of hibernate. All that you need to configure and to
 * supply the sessions for hibernate WARNING : This class maintains state because the sessionFactory is expensive to
 * load.
 */
public class HibernateSessionServiceImpl implements HibernateSessionService, ComponentWithLifecycle
{

	private final Log log = LogFactory.getLog(HibernateSessionServiceImpl.class.getName());

	private static final String HIBERNATE_KEY = "hibernate";

	private static final String HIBERNATE_CONNECTION_URL_KEY = "hibernate.connection.url";

	// Hibernate things
	private String databaseFilename;
	private SessionFactory sessionFactory;
	private ServiceRegistry serviceRegistry;

	private Configuration configuration;

	private ConfigurationService configurationService;

	private final Set<Session> sessionsInUse = Collections.synchronizedSet(new HashSet<Session>());

	/**
	 * Method to setup hibernate with appropriate configuration for the supplied persisted beans
	 *
	 * @param persistedBeans The beans to setup
	 * @throws IOException On failure during the read of the related HBM bean metadata files
	 */
	public void setupPersistenceConfig(final List<HibernatePersistedBeanDefinition> persistedBeans )
			throws IOException
	{
		for (final HibernatePersistedBeanDefinition beanDefinition : persistedBeans)
		{
			final String originalHbmResourceName = beanDefinition.getHbmResourceName();
			if( log.isTraceEnabled() )
			{
				log.trace("Looking for " + originalHbmResourceName );
			}
			final InputStream hbmInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( originalHbmResourceName );

			final String originalHbmString = IOUtils.basicReadInputStreamUTF8( hbmInputStream );

			final String newHbmString = TableNamePrefixer.prefixTableNames( originalHbmString, beanDefinition.getPersistedBeanTablePrefix());

			configuration.addXML( newHbmString );
		}
		log.trace("setupPersistenceConfig end");
	}

	/**
	 * Configure hibernate with properties using the configuration service
	 *
	 * @throws RecordNotFoundException
	 *
	 */
	public void configureProperties()
			throws RecordNotFoundException
	{
		final String[] hibernateProperties = configurationService.getKeysBeginningWith(HIBERNATE_KEY);
		String key;
		String value;
		Properties prop;
		for (int i = 0; i < hibernateProperties.length; i++)
		{
			prop = new Properties();
			key = hibernateProperties[i];
			value = configurationService.getSingleStringValue(key);
			prop.setProperty(key, value);
			configuration.addProperties(prop);
			if( log.isTraceEnabled() )
			{
				log.trace("HibernateProperty " + key + " : " + value);
			}
		}
		log.trace("configureProperties end");
	}

	/**
	 * Create the configuration object for hibernate configuration
	 */
	public void prepareConfiguration()
	{
		log.trace("PrepareConfiguration start");
		// special parameter to avoid the use of the CGLIB optimizer
		// Hibernate always uses CGLIB though
		System.setProperty("hibernate.cglib.use_reflection_optimizer", "false");

		configuration = new Configuration();
		log.trace("PrepareConfiguration end");
	}

	/**
	 * Method that should be exposed to do all the configuration of hibernate
	 *
	 * @param persistedBeans The beans that will be configured
	 * @throws DatastoreException On a failure during the configuration of the beans
	 */
	@Override
	public void configureHibernate(final List<HibernatePersistedBeanDefinition> persistedBeans )
			throws DatastoreException
	{
		try
		{
			prepareConfiguration();
			configureProperties();
			setupPersistenceConfig(persistedBeans);
			serviceRegistry = new StandardServiceRegistryBuilder().applySettings( configuration.getProperties() ).build();
			sessionFactory = configuration.buildSessionFactory( serviceRegistry );
		}
		catch (final HibernateException he)
		{
			HibernateExceptionHandler.rethrowAsDatastoreLogAll( he, log,
					"HibernateException thrown building session factory");
		}
		catch(final Throwable t)
		{
			final String msg = "Throwable caught building session factory: " + t.toString();
			log.error( msg, t );
			throw new DatastoreException( msg, t );
		}
	}

	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	@Override
	public Session getSession()
	{
		if( log.isTraceEnabled() )
		{
			final String msg = "hibernate.getSession() called for thread " + Thread.currentThread().getName();
			log.trace( msg );
		}
		final Session tmpSession = sessionFactory.openSession();
		sessionsInUse.add( tmpSession );
		return tmpSession;
	}

	@Override
	public void releaseSession( final Session session )
	{
		if( log.isTraceEnabled() )
		{
			final String msg = "hibernate.releaseSession() called for thread " + Thread.currentThread().getName();
			log.trace( msg );
		}
		sessionsInUse.remove( session );
		session.close();
	}

	@Override
	public void init()
			throws ComponentConfigurationException
	{
		// Fetch the connection url we will use and pull out the database filename (if any)
		final Map<String,String> errors = new HashMap<String,String>();
		final String connectionUrl = ConfigurationServiceHelper.checkForSingleStringKey( configurationService,
				HIBERNATE_CONNECTION_URL_KEY,
				errors );

		ConfigurationServiceHelper.errorCheck( errors );

		int fileBeginning;
		if( (fileBeginning = connectionUrl.indexOf( "file:" )) != -1 )
		{
			int lastIndex = connectionUrl.indexOf( ";", fileBeginning + 1 );
			lastIndex = (lastIndex == -1 ? connectionUrl.length() - 1 : lastIndex );
			databaseFilename = connectionUrl.substring( fileBeginning + 5, lastIndex );
		}
		else
		{
			log.warn("Database backing is not disk based!");
		}
	}

	@Override
	public void destroy()
	{
		log.trace("destroy() called");
		if( sessionFactory != null)
		{
			if( sessionsInUse.size() > 0 )
			{
				log.error("Some sessions are still in use! Will attempt to close them");
			}

			for( final Session usedSession : sessionsInUse )
			{
				usedSession.close();
			}

			sessionFactory.close();
		}
	}

	public String getDatabaseFilename()
	{
		return databaseFilename;
	}
}
