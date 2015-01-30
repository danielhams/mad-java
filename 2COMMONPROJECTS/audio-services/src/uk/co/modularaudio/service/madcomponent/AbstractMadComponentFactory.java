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

package uk.co.modularaudio.service.madcomponent;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadCreationContext;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;

public abstract class AbstractMadComponentFactory implements ComponentWithLifecycle, MadComponentFactory
{
	private static Log log = LogFactory.getLog( AbstractMadComponentFactory.class.getName());

	protected MadComponentService componentService;
	protected MadClassificationService classificationService;

	private Map<Class<? extends MadDefinition<?,?>>, Class<? extends MadInstance<?,?>> > defClassToInsClassMap;
	private final List<MadDefinition<?,?>> definitions = new ArrayList<MadDefinition<?,?>>();

	@Override
	public Collection<MadDefinition<?,?>> listDefinitions()
	{
		return definitions;
	}

	protected abstract Map<Class<? extends MadDefinition<?,?>>, Class<? extends MadInstance<?,?>> > provideDefClassToInsClassMap() throws ComponentConfigurationException;
	protected abstract MadCreationContext getCreationContext();

	@SuppressWarnings("rawtypes")
	@Override
	public void init() throws ComponentConfigurationException
	{
		if( componentService == null ||
				classificationService == null )
		{
			final String msg = "ComponentFactory(" + this.getClass().getSimpleName() + ") has missing service dependencies. Check configuration.";
			log.error( msg );
			throw new ComponentConfigurationException( msg );
		}

		try
		{
			final MadCreationContext creationContext = getCreationContext();

			defClassToInsClassMap = provideDefClassToInsClassMap();
			for( final Class<? extends MadDefinition<?,?>> definitionClass : defClassToInsClassMap.keySet() )
			{
				// Create an instance of it
				final Class[] consParamTypes = new Class[] { creationContext.getClass(),
						MadClassificationService.class };
				final Constructor cons = definitionClass.getConstructor( consParamTypes );
				final Object[] consParams = new Object[] { creationContext,
						classificationService };
				final Object oDef = cons.newInstance( consParams );
				final MadDefinition<?,?> newDef = (MadDefinition<?,?>)oDef;
				definitions.add( newDef );
			}

			componentService.registerComponentFactory( this );
		}
		catch ( final Exception e )
		{
			final String msg = "Exception caught registering component factory: " + e.toString();
			log.error( msg, e );
			throw new ComponentConfigurationException( msg, e );
		}
	}

	@Override
	public void destroy()
	{
		try
		{
			componentService.unregisterComponentFactory( this );
		}
		catch ( final Exception e )
		{
			final String msg = "Exception caught registering component factory: " + e.toString();
			log.error( msg, e );
		}
	}

	public void setComponentService( final MadComponentService componentService )
	{
		this.componentService = componentService;
	}

	public void setClassificationService( final MadClassificationService classificationService )
	{
		this.classificationService = classificationService;
	}

	@Override
	public MadInstance<?, ?> createInstanceForDefinition( final MadDefinition<?, ?> definition,
			final Map<MadParameterDefinition, String> parameterValues,
			final String instanceName )
		throws DatastoreException
	{
		MadInstance<?,?> retVal = null;
		final Class<? extends MadInstance<?,?>> instanceClassToCreate = defClassToInsClassMap.get( definition.getClass() );
		if( instanceClassToCreate == null )
		{
			final String msg = "Asked to create unknown mad: " + definition.getId() + " of class " + definition.getClass();
			throw new DatastoreException( msg );
		}

		try
		{
			final MadChannelConfiguration channelConfiguration = definition.getChannelConfigurationForParameters( parameterValues );

			final MadCreationContext creationContext = getCreationContext();

			@SuppressWarnings("rawtypes")
			Class[] constructorParams = null;
			Object[] constructorParamValues = null;

			constructorParams = new Class[] {
					creationContext.getClass(),
					String.class,
					definition.getClass(),
					Map.class,
					MadChannelConfiguration.class };

			constructorParamValues = new Object[] {
					creationContext,
					instanceName,
					definition,
					parameterValues,
					channelConfiguration };

			final Constructor<?> constructor = instanceClassToCreate.getConstructor(  constructorParams );
//			log.debug("Creating new instance of " + definition.getName() );
			retVal = (MadInstance<?,?>)constructor.newInstance( constructorParamValues );
//			log.debug("Created new instance of " + definition.getName() );

		}
		catch ( final Exception e )
		{
			final String msg = "Error on fetching constructor: " + e.toString();
			throw new DatastoreException( msg, e );
		}

		return retVal;
	}

	@Override
	public void destroyInstance( final MadInstance<?, ?> instanceToDestroy )
		throws DatastoreException
	{
//		MadDefinition<?,?> definition = instanceToDestroy.getDefinition();
//		log.debug("Destroying instance of " + definition.getName() );
		instanceToDestroy.destroy();
//		log.debug("Destroyed instance of " + definition.getName() );
	}

}
