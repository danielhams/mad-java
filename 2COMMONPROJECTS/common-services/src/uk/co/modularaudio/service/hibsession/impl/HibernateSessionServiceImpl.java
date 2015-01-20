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
import java.util.HashSet;
import java.util.List;
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
import uk.co.modularaudio.service.hibsession.HibernateSessionService;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.hibernate.HibernateExceptionHandler;
import uk.co.modularaudio.util.hibernate.common.HibernatePersistedBeanDefinition;
import uk.co.modularaudio.util.hibernate.common.TableNamePrefixer;
import uk.co.modularaudio.util.io.FileUtils;

/**
 * Implementation class that is responsible of the configuration of hibernate. All that you need to configure and to
 * supply the sessions for hibernate WARNING : This class maintains state because the sessionFactory is expensive to
 * load.
 */
public class HibernateSessionServiceImpl implements HibernateSessionService, ComponentWithLifecycle
{

	private Log log = LogFactory.getLog(HibernateSessionServiceImpl.class.getName());

	private static final String HIBERNATE_KEY = "hibernate";

	// Hibernate things
	private SessionFactory sessionFactory;
	private ServiceRegistry serviceRegistry;

	private Configuration configuration;

	private ConfigurationService configurationService;

	private Set<Session> sessionsInUse = Collections.synchronizedSet(new HashSet<Session>());

	/**
	 * Method to setup the mapping files for hibernate
	 *
	 * @param listHibernateFiles
	 */
	public void setupPersistenceConfig(List<HibernatePersistedBeanDefinition> listHibernateFiles)
		throws IOException
	{
		for (HibernatePersistedBeanDefinition beanDefinition : listHibernateFiles)
		{
			String originalHbmResourceName = beanDefinition.getHbmResourceName();
			log.trace("Looking for " + originalHbmResourceName );
			InputStream hbmInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( originalHbmResourceName );

			String originalHbmString = FileUtils.basicReadInputStreamUTF8( hbmInputStream );

			String newHbmString = TableNamePrefixer.prefixTableNames( originalHbmString, beanDefinition.getPersistedBeanTablePrefix());

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
		String[] hibernateProperties = configurationService.getKeysBeginningWith(HIBERNATE_KEY);
		String key = null;
		String value = null;
		Properties prop = null;
		for (int i = 0; i < hibernateProperties.length; i++)
		{
			prop = new Properties();
			key = hibernateProperties[i];
			value = configurationService.getSingleStringValue(key);
			prop.setProperty(key, value);
			configuration.addProperties(prop);
			log.trace("HibernateProperty " + key + " : " + value);
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
		// Hibernate allways uses CGLIB though
		System.setProperty("hibernate.cglib.use_reflection_optimizer", "false");

		configuration = new Configuration();
		log.trace("PrepareConfiguration end");
	}

	/**
	 * Method that should be exposed to do all the configuration of hibernate
	 *
	 * @param listHibernateFiles
	 * @throws RecordNotFoundException
	 */
	@Override
	public void configureHibernate(List<HibernatePersistedBeanDefinition> listHibernateFiles)
			throws RecordNotFoundException, DatastoreException
	{
		try
		{
			prepareConfiguration();
			configureProperties();
			setupPersistenceConfig(listHibernateFiles);
			serviceRegistry = new StandardServiceRegistryBuilder().applySettings( configuration.getProperties() ).build();
			sessionFactory = configuration.buildSessionFactory( serviceRegistry );
		}
		catch (HibernateException he)
		{
			HibernateExceptionHandler.rethrowAsDatastoreLogAll( he, log,
					"HibernateException thrown building session factory");
		}
		catch(Throwable t)
		{
			String msg = "Throwable caught building session factory: " + t.toString();
			log.error( msg, t );
			throw new DatastoreException( msg, t );
		}
	}

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	public void setConfigurationService(ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	@Override
	public Session getSession()
	{
		Session tmpSession = sessionFactory.openSession();
		sessionsInUse.add( tmpSession );
		return( tmpSession );
	}

	@Override
	public void releaseSession( Session session )
	{
		sessionsInUse.remove( session );
		session.close();
	}

	@Override
	public void init()
			throws ComponentConfigurationException
	{
	}

	@Override
	public void destroy()
	{
		if( sessionFactory != null)
		{
			for( Session usedSession : sessionsInUse )
			{
				usedSession.close();
			}

			sessionFactory.close();
		}
	}
}
