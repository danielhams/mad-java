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

package uk.co.modularaudio.service.rendering.impl.flatgraph;

import java.util.HashSet;
import java.util.Set;

import uk.co.modularaudio.util.audio.mad.MadInstance;

public class FlattenedRenderJob implements Comparable<FlattenedRenderJob>
{
	public static int CARDINALITY_NOT_SET = -1;
	private MadInstance<?,?> madInstance = null;
	private Set<FlattenedRenderJob> producerJobsWeWaitFor = new HashSet<FlattenedRenderJob>();
	private Set<FlattenedRenderJob> consumerJobsWaitingForUs = new HashSet<FlattenedRenderJob>();
	private int cardinality = CARDINALITY_NOT_SET;
	
	public FlattenedRenderJob( MadInstance<?,?> madInstance, Set<FlattenedRenderJob> producerJobsWeWaitFor )
	{
		this.madInstance = madInstance;
		this.producerJobsWeWaitFor = producerJobsWeWaitFor;
	}

	public MadInstance<?,?> getMadInstance()
	{
		return madInstance;
	}

	public Set<FlattenedRenderJob> getProducerJobsWeWaitFor()
	{
		return producerJobsWeWaitFor;
	}

	public int getCardinality()
	{
		return cardinality;
	}

	public void setCardinality(int cardinality)
	{
		this.cardinality = cardinality;
	}

	@Override
	public int compareTo( FlattenedRenderJob c )
	{
		return this.cardinality - c.cardinality;
	}

	public Set<FlattenedRenderJob> getConsumerJobsWaitingForUs()
	{
		return consumerJobsWaitingForUs;
	}
	
	public String toString()
	{
		return madInstance.getInstanceName() + "(" + cardinality + ")";
	}

	public void addConsumerJobWaitingForUs( FlattenedRenderJob flatJob )
	{
		consumerJobsWaitingForUs.add(  flatJob  );
		
	}
}
