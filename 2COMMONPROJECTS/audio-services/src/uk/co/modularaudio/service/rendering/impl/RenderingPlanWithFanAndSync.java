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

package uk.co.modularaudio.service.rendering.impl;

import java.util.Set;

import uk.co.modularaudio.service.rendering.vos.AbstractParallelRenderingJob;
import uk.co.modularaudio.service.rendering.vos.RenderingPlan;
import uk.co.modularaudio.service.rendering.vos.profiling.RenderingPlanProfileResults;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;


public class RenderingPlanWithFanAndSync implements RenderingPlan
{
//	private static Log log = LogFactory.getLog( RenderingPlan.class.getName() );
	
	private HardwareIOChannelSettings planChannelSettings = null;
	private MadTimingParameters planTimingParameters = null;
	private MadFrameTimeFactory planFrameTimeFactory = null;
//	private InitialFanParallelRenderingJob initialFanJob = null;
	private FinalSyncParallelRenderingJob finalSyncJob = null;
	private AbstractParallelRenderingJob allJobs[] = null;
	private AbstractParallelRenderingJob initialJobs[] = null;
	private int totalNumJobs = -1;
	private Set<MadInstance<?,?>> allMadInstances = null;

	private final RenderingPlanProfileResults profileResults;
	
	public RenderingPlanWithFanAndSync( HardwareIOChannelSettings planChannelSettings,
			MadTimingParameters planTimingParameters,
			MadFrameTimeFactory planFrameTimeFactory,
			InitialFanParallelRenderingJob initialFanJob,
			FinalSyncParallelRenderingJob finalSyncJob,
			AbstractParallelRenderingJob[] allJobs,
			AbstractParallelRenderingJob[] initialJobs,
			int totalNumJobs,
			Set<MadInstance<?,?>> allMadInstances,
			MadChannelBuffer[] allChannelBuffers )
	{
		this.planChannelSettings = planChannelSettings;
		this.planTimingParameters = planTimingParameters;
		this.planFrameTimeFactory = planFrameTimeFactory;
//		this.initialFanJob = initialFanJob;
		this.finalSyncJob = finalSyncJob;
		this.allJobs = allJobs;
		this.initialJobs = initialJobs;
		this.totalNumJobs = totalNumJobs;
		this.allMadInstances = allMadInstances;
		
		profileResults = new RenderingPlanProfileResults( allJobs );
	}
	
	@Override
	public HardwareIOChannelSettings getPlanChannelSettings()
	{
		return planChannelSettings;
	}
	
	@Override
	public MadTimingParameters getPlanTimingParameters()
	{
		return planTimingParameters;
	}
	
	@Override
	public MadFrameTimeFactory getPlanFrameTimeFactory()
	{
		return planFrameTimeFactory;
	}
	
	@Override
	public AbstractParallelRenderingJob[] getInitialJobs()
	{
		return initialJobs;
	}

	@Override
	public int getTotalNumJobs()
	{
		return totalNumJobs;
	}

	@Override
	public Set<MadInstance<?,?>> getAllInstances()
	{
		return allMadInstances;
	}

	@Override
	public boolean getPlanUsed()
	{
		return finalSyncJob.wasExecutedAtLeastOnce();
	}
	
	@Override
	public AbstractParallelRenderingJob[] getAllJobs()
	{
		return allJobs;
	}

	@Override
	public void fillProfilingIfNotFilled( long clockCallbackStart, long clockCallbackPostProducer, long clockCallbackPostRpFetch, long clockCallbackPostLoop )
	{
		if( !profileResults.isFilled() )
		{
			profileResults.fillIn( clockCallbackStart, clockCallbackPostProducer, clockCallbackPostRpFetch, clockCallbackPostLoop );
		}		
	}

	@Override
	public boolean getProfileResultsIfFilled( RenderingPlanProfileResults toFillIn )
	{
		if( profileResults.isFilled() )
		{
			RenderingPlanProfileResults.copyFromTo( profileResults, toFillIn );
			profileResults.setFilled( false );
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public void resetPlanExecution()
	{
		finalSyncJob.resetJobExecution();
	}

	@Override
	public boolean wasPlanExecuted()
	{
		return finalSyncJob.wasJobExecuted();
	}
}
