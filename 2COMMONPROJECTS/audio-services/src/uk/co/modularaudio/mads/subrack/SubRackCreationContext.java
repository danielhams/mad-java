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

import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.service.jobexecutor.JobExecutorService;
import uk.co.modularaudio.service.madgraph.MadGraphService;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.service.rackmarshalling.RackMarshallingService;
import uk.co.modularaudio.service.userpreferences.UserPreferencesService;
import uk.co.modularaudio.util.audio.mad.MadCreationContext;

public class SubRackCreationContext extends MadCreationContext
{
	private final RackService rackService;
	private final MadGraphService graphService;
	private final RackMarshallingService rackMarshallingService;
	private final GuiService guiService;
	private final JobExecutorService jobExecutorService;
	private final UserPreferencesService userPreferencesService;

	public SubRackCreationContext( final RackService rackService,
			final MadGraphService graphService,
			final RackMarshallingService rackMarshallingService,
			final GuiService guiService,
			final JobExecutorService jobExecutorService,
			final UserPreferencesService userPreferencesService )
	{
		this.rackService = rackService;
		this.graphService = graphService;
		this.rackMarshallingService = rackMarshallingService;
		this.guiService = guiService;
		this.jobExecutorService = jobExecutorService;
		this.userPreferencesService = userPreferencesService;
	}

	public RackService getRackService()
	{
		return rackService;
	}

	public MadGraphService getGraphService()
	{
		return graphService;
	}

	public RackMarshallingService getRackMarshallingService()
	{
		return rackMarshallingService;
	}

	public GuiService getGuiService()
	{
		return guiService;
	}

	public JobExecutorService getJobExecutorService()
	{
		return jobExecutorService;
	}

	public UserPreferencesService getUserPreferencesService()
	{
		return userPreferencesService;
	}

}
