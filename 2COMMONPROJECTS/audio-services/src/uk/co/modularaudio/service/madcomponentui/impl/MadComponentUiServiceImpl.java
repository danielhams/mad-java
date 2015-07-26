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

package uk.co.modularaudio.service.madcomponentui.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.madcomponentui.MadComponentUiFactory;
import uk.co.modularaudio.service.madcomponentui.MadComponentUiService;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.table.Span;

public class MadComponentUiServiceImpl implements ComponentWithLifecycle, MadComponentUiService
{
	private static Log log = LogFactory.getLog( MadComponentUiServiceImpl.class.getName());

	private final Map<String, MadUiDefinition<?,?>> idToDefinitionMap = new HashMap<String, MadUiDefinition<?,?>>();
	private final Map<MadDefinition<?,?>, MadComponentUiFactory> auDefinitionToFactoryMap = new HashMap<MadDefinition<?,?>, MadComponentUiFactory>();

	@Override
	public void init() throws ComponentConfigurationException
	{
		// No init
	}

	@Override
	public void destroy()
	{
		// No destroy
	}

	@Override
	public void registerComponentUiFactory( final MadComponentUiFactory uiFactory ) throws DatastoreException
	{
		if( log.isDebugEnabled() )
		{
			log.debug( "Registering ui components for " + uiFactory.getClass().getSimpleName() ); // NOPMD by dan on 01/02/15 07:07
		}

		final List<MadUiDefinition<?,?>> factoryDefinitions = uiFactory.listComponentUiDefinitions();

		for( final MadUiDefinition<?,?> uiDefinition : factoryDefinitions )
		{
			final MadDefinition<?,?> audDefinition = uiDefinition.getDefinition();
			final String defId = audDefinition.getId();
			if( idToDefinitionMap.containsKey( defId ) )
			{
				throw new DatastoreException("Unable to UI definition for " + defId + " from " + uiFactory.getClass().getSimpleName() +
						" as the factory " + auDefinitionToFactoryMap.get(audDefinition).getClass().getSimpleName() + " has already registered it.");
			}
		}

		for( final MadUiDefinition<?,?> uiDefinition : factoryDefinitions )
		{
			final MadDefinition<?,?> audDefinition = uiDefinition.getDefinition();
			idToDefinitionMap.put( audDefinition.getId(), uiDefinition );
			auDefinitionToFactoryMap.put( audDefinition, uiFactory );
		}
	}

	@Override
	public void unregisterComponentUiFactory( final MadComponentUiFactory uiFactory ) throws DatastoreException
	{
		if( log.isDebugEnabled() )
		{
			log.debug( "Unregistering ui components for " + uiFactory.getClass().getSimpleName() ); // NOPMD by dan on 01/02/15 07:07
		}

		final List<MadUiDefinition<?,?>> factoryDefinitions = uiFactory.listComponentUiDefinitions();

		for( final MadUiDefinition<?,?> uiDefinition : factoryDefinitions )
		{
			final MadDefinition<?,?> audDefinition = uiDefinition.getDefinition();

			if( auDefinitionToFactoryMap.get( audDefinition ) != uiFactory )
			{
				final String msg = "During uifactory unregister - the factory is not marked as the source for the definition!";
				log.error( msg );
				throw new DatastoreException( msg );
			}
			else
			{
				auDefinitionToFactoryMap.remove( audDefinition );;
			}

			if( idToDefinitionMap.get( audDefinition.getId() ) == uiDefinition )
			{
				idToDefinitionMap.remove( audDefinition.getId() );
			}
			else
			{
				log.warn("Failed to find uifactory uidefinition registered for mad");
			}

		}
	}

	@Override
	public IMadUiInstance<?,?> createUiInstanceForInstance( final MadInstance<?,?> instance )
			throws DatastoreException, RecordNotFoundException
	{
		final MadDefinition<?,?> definition = instance.getDefinition();
		final MadComponentUiFactory factory = auDefinitionToFactoryMap.get( definition );
		if( factory == null )
		{
			final String msg = "Unable to find a UI factory for the definition with id " + definition.getId();
			throw new RecordNotFoundException( msg );
		}
		else
		{
			return factory.createNewComponentUiInstanceForComponent( instance );
		}
	}

	@Override
	public void destroyUiInstance( final IMadUiInstance<?, ?> uiInstance ) throws RecordNotFoundException, DatastoreException
	{
		final MadDefinition<?,?> def = uiInstance.getInstance().getDefinition();
		final MadComponentUiFactory factory = auDefinitionToFactoryMap.get( def );
		if( factory == null )
		{
			final String msg = "Unable to find a UI factory for destruction for definition with id " + def.getId();
			throw new RecordNotFoundException( msg );
		}
		else
		{
			factory.destroyUiInstance( uiInstance );
		}
	}

	@Override
	public Span getUiSpanForDefinition( final MadDefinition<?, ?> definition )
		throws DatastoreException, RecordNotFoundException
	{
		final MadComponentUiFactory factory = auDefinitionToFactoryMap.get( definition );
		if( factory == null )
		{
			final String msg = "Unable to find a UI factory to supply the span for the definition with id " + definition.getId();
			throw new RecordNotFoundException( msg );
		}
		else
		{
			return factory.getUiSpanForDefinition( definition );
		}
	}

}
