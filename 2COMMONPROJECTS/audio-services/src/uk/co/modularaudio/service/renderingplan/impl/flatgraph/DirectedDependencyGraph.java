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

package uk.co.modularaudio.service.renderingplan.impl.flatgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class DirectedDependencyGraph
{
	private final List<FlattenedRenderJob> jobs = new ArrayList<FlattenedRenderJob>();

	public DirectedDependencyGraph()
	{
	}

	public void addFlattenedRenderJob( final FlattenedRenderJob flattenedRenderJob )
	{
		jobs.add( flattenedRenderJob );
		// Add the component instance to job map
		madInstanceToRenderJobMap.put( flattenedRenderJob.getMadInstance(), flattenedRenderJob );
	}

	private final Map<MadInstance<?,?>, FlattenedRenderJob> madInstanceToRenderJobMap = new HashMap<MadInstance<?,?>, FlattenedRenderJob>();

	public FlattenedRenderJob findJobByMadInstance( final MadInstance<?,?> madInstance )
		throws RecordNotFoundException
	{
		final FlattenedRenderJob retVal = madInstanceToRenderJobMap.get( madInstance );
		if( retVal != null )
		{
			return retVal;
		}
		else
		{
			final String msg = "Unable to find a render job for mad instance: " + madInstance.getInstanceName();
			throw new RecordNotFoundException( msg );
		}
	}

	public List<FlattenedRenderJob> getJobs()
	{
		return jobs;
	}
}
