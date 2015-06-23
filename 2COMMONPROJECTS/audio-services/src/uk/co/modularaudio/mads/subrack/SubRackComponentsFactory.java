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

import java.util.HashMap;
import java.util.Map;

import uk.co.modularaudio.mads.subrack.mu.SubRackMadDefinition;
import uk.co.modularaudio.mads.subrack.mu.SubRackMadInstance;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.service.jobexecutor.JobExecutorService;
import uk.co.modularaudio.service.madcomponent.AbstractMadComponentFactory;
import uk.co.modularaudio.service.madgraph.MadGraphService;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.service.rackmarshalling.RackMarshallingService;
import uk.co.modularaudio.service.userpreferences.UserPreferencesService;
import uk.co.modularaudio.util.audio.mad.MadCreationContext;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;

public class SubRackComponentsFactory extends AbstractMadComponentFactory
{
	private final Map<Class<? extends MadDefinition<?,?>>, Class<? extends MadInstance<?,?>> > defClassToInsClassMap =
			new HashMap<Class<? extends MadDefinition<?,?>>, Class<? extends MadInstance<?,?>>>();

	private ConfigurationService configurationService;

	private SubRackCreationContext creationContext;

	private MadGraphService graphService;
	private RackService rackService;
	private RackMarshallingService rackMarshallingService;
	private GuiService guiService;
	private JobExecutorService jobExecutorService;
	private UserPreferencesService userPreferencesService;

	public SubRackComponentsFactory()
	{
		defClassToInsClassMap.put( SubRackMadDefinition.class, SubRackMadInstance.class );
	}

	@Override
	public Map<Class<? extends MadDefinition<?, ?>>, Class<? extends MadInstance<?, ?>>> provideDefClassToInsClassMap()
			throws ComponentConfigurationException
	{
		return defClassToInsClassMap;
	}

	@Override
	public MadCreationContext getCreationContext()
	{
		return creationContext;
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( configurationService == null ||
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

		super.init();
	}

	public void setConfigurationService( final ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
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
}
