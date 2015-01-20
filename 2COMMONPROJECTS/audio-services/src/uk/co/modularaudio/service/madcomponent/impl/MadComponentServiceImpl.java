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

package uk.co.modularaudio.service.madcomponent.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.madcomponent.MadComponentFactory;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.MadClassificationGroup.Visibility;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinitionComparator;
import uk.co.modularaudio.util.audio.mad.MadDefinitionListModel;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.factory.IMadInstanceFactory;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class MadComponentServiceImpl implements ComponentWithLifecycle, MadComponentService
{
	private static Log log = LogFactory.getLog( MadComponentServiceImpl.class.getName() );

	private MadDefinitionComparator auDefComparator = new MadDefinitionComparator();

	private Map<String, MadDefinition<?,?>> idToDefinitionMap = new HashMap<String, MadDefinition<?,?>>();
	private Set<String> displayNameSet = new HashSet<String>();
	private Map<MadDefinition<?,?>, MadComponentFactory> definitionToFactoryMap = new HashMap<MadDefinition<?,?>, MadComponentFactory>();

	private boolean showAlpha = false;
	private boolean showBeta = false;

	public MadComponentServiceImpl()
	{
	}

	@Override
	public MadDefinitionListModel listDefinitionsAvailable()
			throws DatastoreException
	{
		Vector<MadDefinition<?,?>> defsForSorting = new Vector<MadDefinition<?,?>>();
		for( MadDefinition<?,?> definition : definitionToFactoryMap.keySet() )
		{
			boolean shouldAdd = false;

			MadClassification classification = definition.getClassification();

			if( classification.getGroup().getVisibility() == Visibility.PUBLIC )
			{
				ReleaseState state = classification.getState();

				switch( state )
				{
					case ALPHA:
					{
						if( showAlpha )
						{
							shouldAdd = true;
						}
						break;
					}
					case BETA:
					{
						if( showBeta )
						{
							shouldAdd = true;
						}
						break;
					}
					case RELEASED:
					{
						shouldAdd = true;
						break;
					}
				}

				if( shouldAdd )
				{
					defsForSorting.add( definition );
				}
			}
		}

		Collections.sort( defsForSorting, auDefComparator );

		MadDefinitionListModel knownDefinitions = new MadDefinitionListModel( defsForSorting, auDefComparator );
		return knownDefinitions;
	}

	@Override
	public void registerComponentFactory( MadComponentFactory componentFactory)
			throws DatastoreException, MAConstraintViolationException
	{
		log.debug("Registering components for " + componentFactory.getClass().getSimpleName());
		Collection<MadDefinition<?,?>> factoryDefinitions = componentFactory.listDefinitions();

		// First pass checking the definitions are unique
		for( MadDefinition<?,?> definition : factoryDefinitions )
		{
			String id = definition.getId();
			if( idToDefinitionMap.get( id ) != null )
			{
				throw new MAConstraintViolationException( "Attempting to register a definition with an ID already taken: " + id );
			}
			else if( displayNameSet.contains( id ) )
			{
				throw new MAConstraintViolationException( "Attempting to register a definition with a conflicting display name: " + id );
			}
		}
		// Second pass, adding them
		for( MadDefinition<?,?> definition : factoryDefinitions )
		{
			displayNameSet.add( definition.getClassification().getName() );
			idToDefinitionMap.put( definition.getId(), definition );
			definitionToFactoryMap.put( definition, componentFactory );
		}
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
	}

	@Override
	public MadDefinition<?,?> findDefinitionById( String definitionId )
			throws DatastoreException, RecordNotFoundException
	{
		MadDefinition<?,?> retVal = idToDefinitionMap.get( definitionId );
		if( retVal == null )
		{
			String msg = "Mad Definition with id: " + definitionId + " not found.";
			throw new RecordNotFoundException( msg );
		}
		return retVal;
	}

	@Override
	public MadInstance<?,?> createInstanceFromDefinition( MadDefinition<?,?> definition,
			Map<MadParameterDefinition, String> parameterValues,
			String instanceName )
			throws DatastoreException, RecordNotFoundException,
			MadProcessingException
	{
		// Find the associated factory and delegate
		IMadInstanceFactory instanceFactory = definitionToFactoryMap.get( definition  );
		if( instanceFactory != null )
		{
			MadInstance<?,?> factoryCreatedInstance = instanceFactory.createInstanceForDefinition( definition, parameterValues, instanceName );
			if( factoryCreatedInstance == null )
			{
				throw new RecordNotFoundException( "Factory did not create an instance of the required mad: " + definition.getId());
			}
			else
			{
				return factoryCreatedInstance;
			}
		}
		else
		{
			String msg = "Unable to find factory for definition: " + definition.getId();
			throw new RecordNotFoundException( msg );
		}
	}

	@Override
	public void unregisterComponentFactory( MadComponentFactory componentFactory )
			throws DatastoreException
	{
		log.debug("Unregistering components for " + componentFactory.getClass().getSimpleName());
		Collection<MadDefinition<?,?>> factoryDefs = componentFactory.listDefinitions();
		for( MadDefinition<?,?> def : factoryDefs )
		{
			displayNameSet.remove( def.getClassification().getName() );
			idToDefinitionMap.remove( def.getId() );
			definitionToFactoryMap.remove( def );
		}
	}

	@Override
	public void destroyInstance( MadInstance<?,?> instance )
			throws DatastoreException, RecordNotFoundException
	{
		MadDefinition<?,?> definition = instance.getDefinition();
		// Find the associated factory and delegate
		IMadInstanceFactory instanceFactory = definitionToFactoryMap.get( definition  );
		if( instanceFactory != null )
		{
			instanceFactory.destroyInstance( instance );
		}
		else
		{
			String msg = "Unable to find factory for definition: " + definition.getId();
			throw new RecordNotFoundException( msg );
		}
	}

	@Override
	public void setReleaseLevel( boolean showAlpha, boolean showBeta )
	{
		this.showAlpha = showAlpha;
		this.showBeta = showBeta;
	}
}
