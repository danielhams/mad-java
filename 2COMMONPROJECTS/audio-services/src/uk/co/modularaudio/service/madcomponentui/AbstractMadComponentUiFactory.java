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

package uk.co.modularaudio.service.madcomponentui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
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

	protected BufferedImageAllocationService bufferedImageAllocationService;
	protected MadComponentUiService componentUiService;
	protected ComponentImageFactory componentImageFactory;
	protected ConfigurationService configurationService;

	protected Map<MadDefinition<?,?>, MadUiDefinition<? extends MadDefinition<?,?>, ? extends MadInstance<?,?>>> componentDefinitionToUiDefinitionMap =
			new HashMap<MadDefinition<?,?>, MadUiDefinition<? extends MadDefinition<?,?>, ? extends MadInstance<?,?>>>();

	public AbstractMadComponentUiFactory()
	{
	}

	protected abstract void setupTypeToDefinitionClasses() throws DatastoreException;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public IMadUiInstance<?, ?> createUiInstanceForMad( final MadInstance<?, ?> instance )
			throws DatastoreException, RecordNotFoundException
	{
		final MadDefinition<?, ?> definition = instance.getDefinition();
		// log.debug("Creating new ui instance of " + definition.getName() );
		final MadUiDefinition cud = componentDefinitionToUiDefinitionMap.get( definition );
		final AbstractMadUiInstance<?, ?> retVal = cud.createNewUiInstance( instance );
		// log.debug("Created new ui instance of " + definition.getName() );
		return retVal;
	}

	@Override
	public void cleanupUiInstance( final IMadUiInstance<?,?> instanceToDestroy ) // NOPMD by dan on 19/10/15 10:12
			throws DatastoreException, RecordNotFoundException
	{
		// Default is to do nothing.
	}

	@Override
	public void destroy()
	{
		try
		{
			componentUiService.unregisterComponentUiFactory( this );
		}
		catch( final Exception e )
		{
			if( log.isErrorEnabled() )
			{
				log.error( "Exception caught unregistering UI factory: " + e.toString(), e );
			}
		}
	}


	@Override
	public void init() throws ComponentConfigurationException
	{
		try
		{
			setupTypeToDefinitionClasses();
			// Now for each type we have, instantiate the corresponding definitions
			// Read our configuration from the configuration service

			componentUiService.registerComponentUiFactory( this );
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught attempting to register component types: " + e.toString();
			log.error( msg, e );
			throw new ComponentConfigurationException( msg, e );
		}
	}


	public void setComponentImageFactory(final ComponentImageFactory componentImageFactory)
	{
		this.componentImageFactory = componentImageFactory;
	}


	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	public void setComponentUiService(final MadComponentUiService componentUiService)
	{
		this.componentUiService = componentUiService;
	}

	@Override
	public List<MadUiDefinition<?,?>> listComponentUiDefinitions()
	{
		final List<MadUiDefinition<?,?>> retVal = new ArrayList<MadUiDefinition<?,?>>();
		retVal.addAll( componentDefinitionToUiDefinitionMap.values() );
		return retVal;
	}

	public void setBufferedImageAllocationService(
			final BufferedImageAllocationService bufferedImageAllocationService )
	{
		this.bufferedImageAllocationService = bufferedImageAllocationService;
	}

	@Override
	public Span getUiSpanForDefinition( final MadDefinition<?, ?> definition )
			throws DatastoreException, RecordNotFoundException
	{
		final MadUiDefinition<?,?> uiDef = componentDefinitionToUiDefinitionMap.get( definition );
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
