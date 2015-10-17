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

package uk.co.modularaudio.mads.subrack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.subrack.mu.SubRackMadDefinition;
import uk.co.modularaudio.mads.subrack.mu.SubRackMadInstance;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.service.jobexecutor.JobExecutorService;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.service.madcomponent.MadComponentFactory;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.madgraph.MadGraphService;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.service.rackmarshalling.RackMarshallingService;
import uk.co.modularaudio.service.userpreferences.UserPreferencesService;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class SubRackComponentsFactory
	implements ComponentWithLifecycle, MadComponentFactory
{
	private static Log log = LogFactory.getLog( SubRackComponentsFactory.class.getName() );

	private SubRackCreationContext creationContext;

	private ConfigurationService configurationService;
	private MadClassificationService classificationService;
	private MadComponentService componentService;
	private MadGraphService graphService;
	private RackService rackService;
	private RackMarshallingService rackMarshallingService;
	private GuiService guiService;
	private JobExecutorService jobExecutorService;
	private UserPreferencesService userPreferencesService;

	private SubRackMadDefinition subRackMD;
	private final ArrayList<MadDefinition<?,?>> mds = new ArrayList<MadDefinition<?,?>>();

	public SubRackComponentsFactory()
	{
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( configurationService == null ||
				classificationService == null ||
				componentService == null ||
				graphService == null ||
				rackService == null ||
				rackMarshallingService == null ||
				guiService == null ||
				jobExecutorService == null ||
				userPreferencesService == null )
		{
			throw new ComponentConfigurationException( "Factory missing dependencies. Check configuration" );
		}

		creationContext = new SubRackCreationContext( rackService,
				graphService,
				rackMarshallingService,
				guiService,
				jobExecutorService,
				userPreferencesService );

		try
		{
			subRackMD = new SubRackMadDefinition( creationContext, classificationService );
			mds.add( subRackMD );

			componentService.registerComponentFactory( this );
		}
		catch( final DatastoreException | RecordNotFoundException | MAConstraintViolationException e )
		{
			throw new ComponentConfigurationException( "Unable to create mad definitions: " + e.toString() );
		}
	}

	public void setConfigurationService( final ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	public void setClassificationService( final MadClassificationService classificationService )
	{
		this.classificationService = classificationService;
	}

	public void setComponentService( final MadComponentService componentService )
	{
		this.componentService = componentService;
	}

	public void setGraphService( final MadGraphService graphService )
	{
		this.graphService = graphService;
	}

	public void setRackService( final RackService rackService )
	{
		this.rackService = rackService;
	}

	public void setRackMarshallingService(
			final RackMarshallingService rackMarshallingService )
	{
		this.rackMarshallingService = rackMarshallingService;
	}

	public void setGuiService( final GuiService guiService )
	{
		this.guiService = guiService;
	}

	public void setJobExecutorService( final JobExecutorService jobExecutorService )
	{
		this.jobExecutorService = jobExecutorService;
	}

	public void setUserPreferencesService( final UserPreferencesService userPreferencesService )
	{
		this.userPreferencesService = userPreferencesService;
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

	@Override
	public Collection<MadDefinition<?, ?>> listDefinitions()
	{
		return mds;
	}

	@Override
	public MadInstance<?, ?> createInstanceForDefinition( final MadDefinition<?, ?> definition,
			final Map<MadParameterDefinition, String> parameterValues,
			final String instanceName )
		throws DatastoreException
	{
		assert( definition == subRackMD );
		try
		{
			return new SubRackMadInstance( creationContext,
					instanceName,
					subRackMD,
					parameterValues,
					subRackMD.getChannelConfigurationForParameters( parameterValues ) );
		}
		catch( final MAConstraintViolationException | RecordNotFoundException | IOException e )
		{
			throw new DatastoreException( "Failed creating mad instance: " + e.toString() );
		}
	}

	@Override
	public void destroyInstance( final MadInstance<?, ?> instanceToDestroy ) throws DatastoreException
	{
		// Java GC will take care of most of this
		instanceToDestroy.destroy();
	}
}
