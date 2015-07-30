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

package uk.co.modularaudio.util.spring;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.xml.sax.InputSource;

import uk.co.modularaudio.util.exception.DatastoreException;

public class SpringComponentHelper
{
	private static final String CONFIG_RESOURCE_PATH_PROPERTY = "configResourcePath";
	private static final String ADDITIONAL_RESOURCE_PATHS = "additionalResourcePaths";

	private static Log log = LogFactory.getLog( SpringComponentHelper.class.getName() );

	public final String DEFAULT_BEANS_FILENAME = "/beans.xml";

	protected List<SpringContextHelper> contextHelpers = new ArrayList<SpringContextHelper>();

	protected BeanInstantiationListAsPostProcessor beanInstantiationList = new BeanInstantiationListAsPostProcessor();

	public SpringComponentHelper()
	{
	}

	public SpringComponentHelper( final List<SpringContextHelper> clientHelpers )
	{
		contextHelpers.addAll( clientHelpers );
	}

	public GenericApplicationContext makeAppContext()
			throws DatastoreException
	{
		return makeAppContext( DEFAULT_BEANS_FILENAME, null, null, null );
	}

	public GenericApplicationContext makeAppContext( final String beansResourcePath,
			final String configResourcePath )
					throws DatastoreException
	{
		return makeAppContext( beansResourcePath, configResourcePath, null, null );
	}

	public GenericApplicationContext makeAppContext( final String beansResourcePath,
			final String configResourcePath,
			final String[] additionalBeansResources,
			final String[] additionalConfigResources )
					throws DatastoreException
	{
		GenericApplicationContext appContext = null;

		final Class<SpringComponentHelper> thisClass = SpringComponentHelper.class;

		// Do any work needed before we instantiate the context
		for( final SpringContextHelper helper : contextHelpers )
		{
			try
			{
				helper.preContextDoThings();
			}
			catch(final Exception ce )
			{
				final String msg = "Exception caught calling precontext of helper " +
						helper.getClass() +
						": " + ce.toString();
				log.error( msg, ce );
				// Will halt context creation.
				throw new DatastoreException( msg, ce );
			}
		}

		try
		{
			final InputStream bIStream = thisClass.getResourceAsStream( beansResourcePath );
			if( bIStream != null )
			{
				final InputSource bISource = new InputSource( bIStream );

				appContext = new GenericApplicationContext();
				appContext.addBeanFactoryPostProcessor( beanInstantiationList );

				final XmlBeanDefinitionReader xbdr = new XmlBeanDefinitionReader( appContext );

				xbdr.setValidationMode( XmlBeanDefinitionReader.VALIDATION_NONE );
				xbdr.loadBeanDefinitions( bISource );

				// And load any additional beans files now we've loaded the "driver"
				if( additionalBeansResources != null && additionalBeansResources.length > 0 )
				{
					for( final String additionalBeansFilename : additionalBeansResources )
					{
						InputStream aBiStream = null;
						InputSource aBiSource = null;
						try
						{
							aBiStream = thisClass.getResourceAsStream( additionalBeansFilename );

							if( aBiStream != null )
							{
								aBiSource = new InputSource( aBiStream );
								xbdr.loadBeanDefinitions( aBiSource );
							}
							else
							{
								throw new DatastoreException("Failed to load additional services from file: " + additionalBeansFilename );
							}
						}
						finally
						{
							if( aBiStream != null )
							{
								aBiStream.close();
							}
						}
					}
				}

				final BeanDefinition bd = appContext.getBeanDefinition( "configurationService" );
				final MutablePropertyValues mpvs = bd.getPropertyValues();
				// Push in the configuration file if one needs to be set
				if( configResourcePath != null )
				{
					mpvs.removePropertyValue( CONFIG_RESOURCE_PATH_PROPERTY );
					mpvs.addPropertyValue( CONFIG_RESOURCE_PATH_PROPERTY, configResourcePath );

				}
				if( additionalConfigResources != null && additionalConfigResources.length > 0 )
				{
					mpvs.removePropertyValue( ADDITIONAL_RESOURCE_PATHS );
					mpvs.addPropertyValue( ADDITIONAL_RESOURCE_PATHS, additionalConfigResources );
				}

				// Do any work needed before we refresh the context
				// Prerefresh
				for( final SpringContextHelper helper : contextHelpers )
				{
					try
					{
						helper.preRefreshDoThings( appContext );
					}
					catch(final Exception prer )
					{
						final String msg = "Exception caught calling prerefresh of helper " +
								helper.getClass() +
								": " + prer.toString();
						log.error( msg, prer );
						// Will halt context creation
						throw new DatastoreException( msg, prer );
					}
				}

				appContext.refresh();

			}
			else
			{
				// Didn't find the beans file
				final String msg = "Unable to find beans file: " + beansResourcePath;
				log.error( msg );
				throw new DatastoreException( msg );
			}
		}
		catch( final Exception e )
		{
			final String msg = "Exception caught setting up app context: " + e.toString();
			log.error( msg, e );
			throw new DatastoreException( msg, e );
		}

		// Perform a GC pass here to clean up before things are launched post refresh
		Runtime.getRuntime().gc(); // NOPMD by dan on 30/07/15 15:00

		// Do any work needed after we refresh the context
		// Post refresh calls
		for( final SpringContextHelper helper : contextHelpers )
		{
			try
			{
				helper.postRefreshDoThings( appContext, beanInstantiationList );
			}
			catch(final Exception pre )
			{
				final String msg = "Exception caught calling postrefresh of helper " +
						helper.getClass() +
						": " + pre.toString();
				log.error( msg, pre );
				// Will halt context creation
				throw new DatastoreException( msg, pre );
			}
		}

		return appContext;
	}

	public void destroyAppContext( final GenericApplicationContext gac )
	{
		// Preshutdown calls
		for( final SpringContextHelper helper : contextHelpers )
		{
			try
			{
				helper.preShutdownDoThings( gac, beanInstantiationList );
			}
			catch (final DatastoreException e)
			{
				final String msg = "Exception caught calling preshutdown of helper " +
						helper.getClass() +
						": " + e.toString();
				log.error( msg, e );
				// We don't halt as the destroy should try and carry on
			}
		}
		gac.close();
	}
}
