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

package uk.co.modularaudio.service.madcomponentui.factoryshell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.service.madcomponentui.MadComponentUiFactory;
import uk.co.modularaudio.service.madcomponentui.MadComponentUiService;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.gui.mad.MadUiInstance;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.table.Span;

public abstract class AbstractMadComponentUiFactory implements MadComponentUiFactory, ComponentWithLifecycle
{
	private static Log log = LogFactory.getLog( AbstractMadComponentUiFactory.class.getName() );

	protected static final String IMAGE_ROOT = "ImageRoot";

	protected BufferedImageAllocationService bufferedImageAllocationService = null;
	protected MadComponentUiService componentUiService = null;
	protected ComponentImageFactory componentImageFactory = null;
	protected ConfigurationService configurationService = null;

	protected String imageRoot = null;

	protected Map<MadDefinition<?,?>, MadUiDefinition<? extends MadDefinition<?,?>, ? extends MadInstance<?,?>>> componentDefinitionToUiDefinitionMap =
			new HashMap<MadDefinition<?,?>, MadUiDefinition<? extends MadDefinition<?,?>, ? extends MadInstance<?,?>>>();

	public AbstractMadComponentUiFactory()
	{
	}

	public abstract void setupTypeToDefinitionClasses() throws DatastoreException;


	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public MadUiInstance<?,?> createNewComponentUiInstanceForComponent( MadInstance<?,?> instance )
			throws DatastoreException, RecordNotFoundException
	{
		MadDefinition<?,?> definition = instance.getDefinition();
//		log.debug("Creating new ui instance of " + definition.getName() );
		MadUiDefinition cud = componentDefinitionToUiDefinitionMap.get( definition );
		MadUiInstance<?, ?> retVal = cud.createNewUiInstance( instance );
//		log.debug("Created new ui instance of " + definition.getName() );
		return retVal;
	}

	@Override
	public void destroyUiInstance( MadUiInstance<?,?> instanceToDestroy )
		throws DatastoreException, RecordNotFoundException
	{
		// Do nothing, java is easy :-)
	}

	@Override
	public void destroy()
	{
		try
		{
			componentUiService.unregisterComponentUiFactory( this );
		}
		catch( Exception e )
		{
			log.error( "Exception caught unregistering UI factory: " + e.toString(), e );
		}
	}


	@Override
	public void init() throws ComponentConfigurationException
	{
		String declaringClassSimpleName = null;
		try
		{
			Class<?> declaringClass = this.getClass();
			if( declaringClass == null )
			{
				String msg = "Declaring class not defined - unable to fetch class specific configuration";
				log.error( msg );
				throw new ComponentConfigurationException( msg );
			}
			else
			{
				declaringClassSimpleName = declaringClass.getSimpleName();
				imageRoot = configurationService.getSingleStringValue( declaringClassSimpleName + "." + IMAGE_ROOT );
			}
		}
		catch( RecordNotFoundException rnfe )
		{
			String msg = "Error configuring image factory: " + rnfe.toString();
			log.error( msg, rnfe );
			throw new ComponentConfigurationException( msg, rnfe );
		}

		try
		{
			setupTypeToDefinitionClasses();
			// Now for each type we have, instantiate the corresponding definitions
			// Read our configuration from the configuration service

			componentUiService.registerComponentUiFactory( this );
		}
		catch (Exception e)
		{
			String msg = "Exception caught attempting to register component types: " + e.toString();
			log.error( msg, e );
			throw new ComponentConfigurationException( msg, e );
		}
	}


	public ComponentImageFactory getComponentImageFactory()
	{
		return componentImageFactory;
	}


	public void setComponentImageFactory(ComponentImageFactory componentImageFactory)
	{
		this.componentImageFactory = componentImageFactory;
	}


	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}


	public void setConfigurationService(ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	public MadComponentUiService getComponentUiService()
	{
		return componentUiService;
	}

	public void setComponentUiService(MadComponentUiService componentUiService)
	{
		this.componentUiService = componentUiService;
	}

	@Override
	public List<MadUiDefinition<?,?>> listComponentUiDefinitions()
	{
		List<MadUiDefinition<?,?>> retVal = new ArrayList<MadUiDefinition<?,?>>();
		retVal.addAll( componentDefinitionToUiDefinitionMap.values() );
		return retVal;
	}

	public BufferedImageAllocationService getBufferedImageAllocationService()
	{
		return bufferedImageAllocationService;
	}

	public void setBufferedImageAllocationService(
			BufferedImageAllocationService bufferedImageAllocationService )
	{
		this.bufferedImageAllocationService = bufferedImageAllocationService;
	}

	@Override
	public Span getUiSpanForDefinition( MadDefinition<?, ?> definition )
		throws DatastoreException, RecordNotFoundException
	{
		MadUiDefinition<?,?> uiDef = componentDefinitionToUiDefinitionMap.get( definition );
		if( uiDef == null )
		{
			throw new RecordNotFoundException("No such definition handled: " + definition.getId() );
		}
		else
		{
			return uiDef.getCellSpan();
		}
	}
}
