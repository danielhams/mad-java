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

package uk.co.modularaudio.service.renderingplan.impl.rpdump;

import java.util.Collection;

import uk.co.modularaudio.service.renderingplan.RenderingJob;

public class InitialTask implements Runnable
{
//	private static Log log = LogFactory.getLog( InitialTask.class.getName() );

	private final Collection<RenderingJob> initialJobsCollection;

	private final AddNewTaskInterface addNewTaskInterface;

	private final int maxJobs;
	private final Runnable[] newTasks;

	public InitialTask( final AddNewTaskInterface addNewTaskInterface,
			final Collection<RenderingJob> initialJobsCollection,
			final int maxJobs )
	{
		this.addNewTaskInterface = addNewTaskInterface;
		this.initialJobsCollection = initialJobsCollection;
		this.maxJobs = maxJobs;
		this.newTasks = new Runnable[ maxJobs ];
	}

	@Override
	public void run()
	{
		int curJobNum = 0;
		for( final RenderingJob initialJob : initialJobsCollection )
		{
			final RenderTask renderTask = new RenderTask( addNewTaskInterface, initialJob, maxJobs );
			newTasks[ curJobNum++ ] = renderTask;
		}
		addNewTaskInterface.addNewTasks( newTasks, curJobNum );
	}
}
