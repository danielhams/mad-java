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

package uk.co.modularaudio.mads.masterio;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.masterio.mu.MasterInMadDefinition;
import uk.co.modularaudio.mads.masterio.mu.MasterInMadInstance;
import uk.co.modularaudio.mads.masterio.mu.MasterOutMadDefinition;
import uk.co.modularaudio.mads.masterio.mu.MasterOutMadInstance;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.service.madcomponent.MadComponentFactory;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class MasterIOComponentsFactory
	implements ComponentWithLifecycle, MadComponentFactory
{
	private static Log log = LogFactory.getLog( MasterIOComponentsFactory.class.getName());

	private MadClassificationService classificationService;
	private MadComponentService componentService;

	private final MasterIOComponentsCreationContext creationContext = new MasterIOComponentsCreationContext();

	private MasterInMadDefinition inMd;
	private MasterOutMadDefinition outMd;

	private final ArrayList<MadDefinition<?,?>> mds = new ArrayList<MadDefinition<?,?>>();

	public MasterIOComponentsFactory()
	{
	}

	@Override
	public Collection<MadDefinition<?, ?>> listDefinitions()
	{
		return mds;
	}

	@Override
	public MadInstance<?, ?> createInstanceForDefinition( final MadDefinition<?, ?> definition,
			final Map<MadParameterDefinition, String> parameterValues, final String instanceName ) throws DatastoreException
	{
		if( definition == inMd )
		{
			return new MasterInMadInstance( creationContext,
					instanceName,
					inMd,
					parameterValues,
					inMd.getChannelConfigurationForParameters( parameterValues ) );
		}
		else if( definition == outMd )
		{
			return new MasterOutMadInstance( creationContext,
					instanceName,
					outMd,
					parameterValues,
					outMd.getChannelConfigurationForParameters( parameterValues ) );
		}
		else
		{
			throw new DatastoreException("Unknown mad: " + definition.getName() );
		}
	}

	@Override
	public void destroyInstance( final MadInstance<?, ?> instanceToDestroy ) throws DatastoreException
	{
		instanceToDestroy.destroy();
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( classificationService == null ||
				componentService == null )
		{
			throw new ComponentConfigurationException( "Service missing dependencies. Please check config." );
		}

		try
		{
			inMd = new MasterInMadDefinition( creationContext, classificationService );
			mds.add( inMd );

			outMd = new MasterOutMadDefinition( creationContext, classificationService );
			mds.add( outMd );

			componentService.registerComponentFactory( this );
		}
		catch( final DatastoreException | MAConstraintViolationException | RecordNotFoundException e )
		{
			throw new ComponentConfigurationException( e );
		}
	}

	@Override
	public void destroy()
	{
		try
		{
			componentService.unregisterComponentFactory( this );
		}
		catch( final DatastoreException e )
		{
			log.error( e );
		}
	}

	public void setClassificationService( final MadClassificationService classificationService )
	{
		this.classificationService = classificationService;
	}

	public void setComponentService( final MadComponentService componentService )
	{
		this.componentService = componentService;
	}
}
