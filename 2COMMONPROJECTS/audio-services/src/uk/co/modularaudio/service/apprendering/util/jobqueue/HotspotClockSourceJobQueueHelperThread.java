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

package uk.co.modularaudio.service.apprendering.util.jobqueue;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.apprendering.util.AppRenderingJobQueue;
import uk.co.modularaudio.service.apprendering.util.HotspotFrameTimeFactory;
import uk.co.modularaudio.service.renderingplan.RenderingPlan;
import uk.co.modularaudio.service.renderingplan.RenderingPlanService;
import uk.co.modularaudio.service.timing.TimingService;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.timing.MadChannelPeriodData;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.AbstractInterruptableThread;
import uk.co.modularaudio.util.thread.ThreadUtils.MAThreadPriority;

public class HotspotClockSourceJobQueueHelperThread extends AbstractInterruptableThread
{
	private static Log log = LogFactory.getLog( HotspotClockSourceJobQueueHelperThread.class.getName() );

	private final RenderingPlan renderingPlan;
	private final RenderingPlanService renderingService;
	private final TimingService timingService;

	private final boolean shouldProfileRenderingJobs;
	private final ClockSourceJobQueueProcessing clockSourceJobQueueProcessing;

	private final HotspotFrameTimeFactory frameTimeFactory = new HotspotFrameTimeFactory();

	public HotspotClockSourceJobQueueHelperThread( final RenderingPlan renderingPlan,
			final RenderingPlanService renderingService,
			final TimingService timingService,
			final AppRenderingJobQueue renderingJobQueue,
			final boolean shouldProfileRenderingJobs )
	{
		super( MAThreadPriority.IDLE );
		setName("HotspotClockSourceJobQueueHelperThread");

		this.renderingPlan = renderingPlan;
		this.renderingService = renderingService;
		this.timingService = timingService;

		this.shouldProfileRenderingJobs = shouldProfileRenderingJobs;
		this.clockSourceJobQueueProcessing = new ClockSourceJobQueueProcessing( renderingJobQueue );
	}

	@Override
	protected void doJob()
	{
		final HardwareIOChannelSettings renderingPlanIOSettings = renderingPlan.getPlanChannelSettings();
		final int configuredSampleRate = renderingPlanIOSettings.getAudioChannelSetting().getDataRate().getValue();
		final int samplesPerRenderPeriod = renderingPlanIOSettings.getAudioChannelSetting().getChannelBufferLength();

		final long nanosPerBackEndPeriod = AudioTimingUtils.getNumNanosecondsForBufferLength( configuredSampleRate,
				samplesPerRenderPeriod );
		final long nanosPerBackEndSample = nanosPerBackEndPeriod / samplesPerRenderPeriod;
		final long nanosPerFrontEndPeriod = 1000000000 / 60;
		final int sampleFramesPerFrontEndPeriod = configuredSampleRate / 60;

		final MadTimingParameters timingParameters = timingService.getTimingSource().getTimingParameters();
		timingParameters.reset( nanosPerBackEndPeriod,
				nanosPerBackEndSample,
				nanosPerFrontEndPeriod,
				sampleFramesPerFrontEndPeriod,
				renderingPlanIOSettings.getAudioChannelSetting().getChannelBufferLength() );

		final MadChannelPeriodData periodData = timingService.getTimingSource().getTimingPeriodData();
		long periodStartFrameTime = 0;
		periodData.reset( periodStartFrameTime, samplesPerRenderPeriod );

		try
		{
			final Set<MadInstance<?,?>> allAuis = renderingPlan.getAllInstances();
			for( final MadInstance<?,?> aui : allAuis )
			{
				aui.internalEngineStartup( renderingPlan.getPlanChannelSettings(),
						renderingPlan.getPlanTimingParameters(),
						frameTimeFactory );
			}

			boolean localShouldHalt = shouldHalt;
			while( !localShouldHalt )
			{
				periodData.reset( periodStartFrameTime, periodData.getNumFramesThisPeriod() );

				clockSourceJobQueueProcessing.doUnblockedJobQueueProcessing( renderingPlan, shouldProfileRenderingJobs );

				localShouldHalt = shouldHalt;
				periodStartFrameTime += periodData.getNumFramesThisPeriod();
			}

			for( final MadInstance<?,?> aui : allAuis )
			{
				aui.internalEngineStop();
			}

			// And cleanup the rendering plan
			renderingService.destroyRenderingPlan( renderingPlan );
		}
		catch (final MadProcessingException e)
		{
			final String msg = "MadProcessingException caught hotspot looping: " + e.toString();
			log.error( msg, e );
		}
	}

}
