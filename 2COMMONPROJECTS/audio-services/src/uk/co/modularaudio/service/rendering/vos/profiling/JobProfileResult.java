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

package uk.co.modularaudio.service.rendering.vos.profiling;

import uk.co.modularaudio.service.rendering.vos.AbstractParallelRenderingJob;

public class JobProfileResult
{
	private int jobThreadExecutor = Integer.MAX_VALUE;
	private long startTimestamp = -1;
	private long endTimestamp = -1;

	public void pullResultsFromJob( AbstractParallelRenderingJob job )
	{
		jobThreadExecutor = job.getJobThreadExecutor();
		startTimestamp = job.getJobStartTimestamp();
		endTimestamp = job.getJobEndTimestamp();
	}

	public int getJobThreadExecutor()
	{
		return jobThreadExecutor;
	}

	public long getStartTimestamp()
	{
		return startTimestamp;
	}

	public long getEndTimestamp()
	{
		return endTimestamp;
	}

}
