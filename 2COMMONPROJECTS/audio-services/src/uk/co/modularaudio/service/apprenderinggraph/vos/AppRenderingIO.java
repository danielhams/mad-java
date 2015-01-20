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

package uk.co.modularaudio.service.apprenderinggraph.vos;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.apprenderinggraph.AppRenderingGraphService;
import uk.co.modularaudio.service.apprenderinggraph.renderingjobqueue.ClockSourceJobQueueProcessing;
import uk.co.modularaudio.service.apprenderinggraph.vos.AppRenderingErrorQueue.ErrorSeverity;
import uk.co.modularaudio.service.apprenderinggraph.vos.AppRenderingLifecycleListener.SignalType;
import uk.co.modularaudio.service.rendering.vos.RenderingPlan;
import uk.co.modularaudio.service.timing.TimingService;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOConfiguration;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOOneChannelSetting;
import uk.co.modularaudio.util.audio.mad.hardwareio.IOBuffers;
import uk.co.modularaudio.util.audio.mad.timing.MadChannelPeriodData;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;
import uk.co.modularaudio.util.tuple.TwoTuple;

public abstract class AppRenderingIO implements MadFrameTimeFactory
{
	private static Log log = LogFactory.getLog( AppRenderingIO.class.getName() );
	
	private final static long TEST_IO_WAIT_MILLIS = 200;
	private final static long TEST_SLEEP_MILLIS = 100;
	
	protected AppRenderingGraphService appRenderingGraphService;
	protected AppRenderingGraph appRenderingGraph;
	protected TimingService timingService;
	
	protected AppRenderingErrorQueue errorQueue = null;
	protected AppRenderingErrorCallback errorCallback = null;
	
	protected HardwareIOConfiguration hardwareConfiguration;
	protected MadTimingParameters timingParameters;
	
	protected AtomicBoolean rendering = new AtomicBoolean( false );
	
	protected volatile boolean shouldRecordPeriods = false;
	
	protected int numSoftUnderflows = 0;
	protected int numSoftOverflows = 0;
	protected int numHardUnderflows = 0;
	protected int numHardOverflows = 0;
	protected long numPeriodsRecorded = 0;
	protected boolean fatalException = false;
	
	protected IOBuffers masterInBuffers = null;
	protected IOBuffers masterOutBuffers = null;
	
	protected ClockSourceJobQueueProcessing clockSourceJobQueueProcessing = null;
	
	protected volatile boolean shouldProfileRenderingJobs = false;
	
	public AppRenderingIO( AppRenderingGraphService appRenderingGraphService,
			TimingService timingService,
			HardwareIOConfiguration hardwareConfiguration,
			AppRenderingErrorQueue errorQueue,
			AppRenderingErrorCallback errorCallback ) throws DatastoreException
	{
		this.appRenderingGraphService = appRenderingGraphService;
		this.timingService = timingService;
		this.hardwareConfiguration = hardwareConfiguration;
		appRenderingGraph = appRenderingGraphService.createAppRenderingGraph();
		
		clockSourceJobQueueProcessing = new ClockSourceJobQueueProcessing( appRenderingGraph.getRenderingJobQueue() );
		
		shouldProfileRenderingJobs = appRenderingGraphService.shouldProfileRenderingJobs();
		
		this.errorQueue = errorQueue;
		this.errorCallback = errorCallback;
		errorQueue.addCallbackForRenderingIO( this, errorCallback );
	}

	public void startRendering()
	{
		if( isRendering() )
		{
			errorQueue.queueError( this, ErrorSeverity.FATAL, "AppRenderingIO already rendering" );
		}
		else
		{
			// Some fake values we'll use to start off discovery
			DataRate dataRate = DataRate.SR_44100;
			long nanosOutputLatency = 10000;
			int sampleFramesOutputLatency = hardwareConfiguration.getChannelBufferLength();
			
			HardwareIOOneChannelSetting audioChannelSetting = new HardwareIOOneChannelSetting( dataRate,  sampleFramesOutputLatency );
			HardwareIOChannelSettings hardwareChannelSettings = new HardwareIOChannelSettings(audioChannelSetting,
					nanosOutputLatency,
					sampleFramesOutputLatency );
					
			try
			{
				TwoTuple<HardwareIOChannelSettings, MadTimingParameters> discoveredSettings = doProviderInit( hardwareConfiguration );
				
				hardwareChannelSettings = discoveredSettings.getHead();
				
				timingParameters = timingService.getTimingSource().getTimingParameters();
				timingParameters.reset( discoveredSettings.getTail() );
				
				log.info("Starting up audio IO with channel settings: " + hardwareChannelSettings.toString() );
				log.info("Starting up audio IO with parameters: " + timingParameters.toString() );
				
				fireLifecycleSignal( hardwareChannelSettings, SignalType.PRE_START );
				doProviderStart();
				fireLifecycleSignal( hardwareChannelSettings, SignalType.POST_START );
				rendering.set( true );
			}
			catch( Exception e )
			{
				String msg = "Exception caught starting audio IO: " + e.toString();
				log.error( msg, e );
				errorQueue.queueError( this, ErrorSeverity.FATAL, "Exception caught starting rendering" );
			}
		}
	}
	
	public boolean stopRendering()
	{
		if( !isRendering() )
		{
			return false;
		}
		AtomicReference<RenderingPlan> atomicRenderingPlan = appRenderingGraph.getAtomicRenderingPlan();
		RenderingPlan renderingPlan = atomicRenderingPlan.get();
		HardwareIOOneChannelSetting fakeAudioChannelSetting = new HardwareIOOneChannelSetting(DataRate.SR_44100, 1024);
		HardwareIOChannelSettings fakeChannelSettings = new HardwareIOChannelSettings(fakeAudioChannelSetting, AudioTimingUtils.getNumNanosecondsForBufferLength(44100, 1024), 1024);
		HardwareIOChannelSettings hardwareChannelSettings = (renderingPlan != null ? renderingPlan.getPlanChannelSettings() : fakeChannelSettings);
		
		try
		{
			fireLifecycleSignal(hardwareChannelSettings, SignalType.PRE_STOP );
			doProviderStop();
		}
		catch( Exception e )
		{
			String msg = "Exception caught stopping audio IO: " + e.toString();
			log.error( msg, e );
		}
		
		try
		{
			fireLifecycleSignal(hardwareChannelSettings, SignalType.POST_STOP );
			doProviderDestroy();
		}
		catch( Exception e )
		{
			String msg = "Exception caught destroying audio IO: " + e.toString();
			log.error( msg, e );
		}
	
		rendering.set( false );
		
		return true;
	}

	public boolean isRendering()
	{
		return rendering.get();
	}

	public boolean testRendering( long testClientRunMillis )
	{
		AudioTestResults retVal = new AudioTestResults();
		
		DataRate dataRate = DataRate.SR_44100;
		long nanosOutputLatency = 10000;
		int sampleFramesOutputLatency = hardwareConfiguration.getChannelBufferLength();
		
		HardwareIOOneChannelSetting audioChannelSetting = new HardwareIOOneChannelSetting( dataRate,  sampleFramesOutputLatency );
		HardwareIOChannelSettings hardwareChannelSettings = new HardwareIOChannelSettings(audioChannelSetting,
				nanosOutputLatency,
				sampleFramesOutputLatency );
		
		errorQueue.removeCallbackForRenderingIO( this );
		TestRenderingErrorCallback testErrorCallback = new TestRenderingErrorCallback();
		errorQueue.addCallbackForRenderingIO( this, testErrorCallback );
		
		try
		{
			TwoTuple<HardwareIOChannelSettings, MadTimingParameters> discoveredSettings = doProviderInit( hardwareConfiguration );
			
			hardwareChannelSettings = discoveredSettings.getHead();
			
			timingParameters = timingService.getTimingSource().getTimingParameters();
			timingParameters.reset( discoveredSettings.getTail() );
			
			fireLifecycleSignal( hardwareChannelSettings, SignalType.PRE_START );
			doProviderStart();
			fireLifecycleSignal( hardwareChannelSettings, SignalType.POST_START );
			
			long startupTimestampMillis = System.currentTimeMillis();
			
			log.debug("Started test IO at timestamp: " + startupTimestampMillis );
			
			// Let it settle
			try
			{
				Thread.sleep( TEST_IO_WAIT_MILLIS );
			}
			catch( InterruptedException ie )
			{
			}
			
			fireLifecycleSignal( hardwareChannelSettings, SignalType.START_TEST );
			
			setShouldRecordPeriods( true );
			
			startupTimestampMillis = System.currentTimeMillis();
			long currentTimestampMillis = System.currentTimeMillis();
			while( currentTimestampMillis <= startupTimestampMillis + testClientRunMillis )
			{
				try
				{
					Thread.sleep( TEST_SLEEP_MILLIS );
				}
				catch( InterruptedException ie )
				{
				}
				currentTimestampMillis = System.currentTimeMillis();
			}
		}
		catch( Exception e )
		{
			log.error("Exception caught audio IO testing: " + e.toString(), e );
		}
		finally
		{
			setShouldRecordPeriods( false );
			try
			{
				fireLifecycleSignal(hardwareChannelSettings, SignalType.STOP_TEST );

				fireLifecycleSignal( hardwareChannelSettings, SignalType.PRE_STOP );
				doProviderStop();
				fireLifecycleSignal(hardwareChannelSettings, SignalType.POST_STOP );

			}
			catch( Exception e )
			{
				log.error("Exception caught shutting down test rendering: " + e.toString(), e );
				fatalException = true;
			}

			long endTimestampMillis = System.currentTimeMillis();
			try
			{
				doProviderDestroy();
			}
			catch( Exception e )
			{
				log.error("Exception caught destroying provider ari: " + e.toString(), e );
				fatalException = true;
			}
			
			errorQueue.removeCallbackForRenderingIO( this );
			errorQueue.addCallbackForRenderingIO( this, errorCallback );
			
			fatalException = fatalException | testErrorCallback.hadFatalErrors;
			retVal.fillIn(numSoftOverflows, numSoftUnderflows, numHardOverflows, numHardUnderflows, fatalException, numPeriodsRecorded);
			
			log.debug("Testing finished at timestamp: " + endTimestampMillis) ;
			retVal.logResults( log );
		}

		return retVal.isSuccessfull();
	}
	
	public AppRenderingGraph getAppRenderingGraph()
	{
		return appRenderingGraph;
	}

	public void destroy()
	{
		try
		{
			if( isRendering() )
			{
				log.warn("Destroy called but still rendering!");
				stopRendering();
			}
			if( appRenderingGraph.isApplicationGraphActive() )
			{
				appRenderingGraph.deactivateApplicationGraph();
			}
			if( appRenderingGraph.isApplicationGraphSet() )
			{
				MadGraphInstance<?,?> graphToUnset = appRenderingGraph.getCurrentApplicationGraph();
				appRenderingGraph.unsetApplicationGraph( graphToUnset );
				// We don't destroy it, it was set by someone else, so they are responsible for destroying it
			}
			appRenderingGraphService.destroyAppRenderingGraph( appRenderingGraph );
			
		}
		catch( Exception e )
		{
			log.error("Exception caught attempting to stop rendering and destroy: " + e.toString(), e );
		}
		finally
		{
			errorQueue.removeCallbackForRenderingIO( this );
		}
	}
	
	private void fireLifecycleSignal( HardwareIOChannelSettings hardwareChannelSettings, AppRenderingLifecycleListener.SignalType signal )
		throws DatastoreException, MadProcessingException
	{
		if( appRenderingGraph != null )
		{
			appRenderingGraph.receiveEngineSignal( hardwareChannelSettings, this, signal, errorQueue );
		}

		switch( signal )
		{
			case PRE_START:
			{
				masterInBuffers = appRenderingGraph.getMasterInBuffers();
				masterOutBuffers = appRenderingGraph.getMasterOutBuffers();
				
				break;
			}
			default:
			{
				break;
			}
		}
		
	}
	
	public void setShouldRecordPeriods( boolean should )
	{
		this.shouldRecordPeriods = should;
	}

	protected abstract TwoTuple<HardwareIOChannelSettings, MadTimingParameters> doProviderInit( HardwareIOConfiguration hardwareConfiguration ) throws DatastoreException;
	protected abstract void doProviderStart() throws DatastoreException;
	protected abstract void doProviderStop() throws DatastoreException;
	protected abstract void doProviderDestroy() throws DatastoreException;
	
	// Used by the providers to do the actual call through to the rendering plan and jobs
	protected RealtimeMethodReturnCodeEnum doClockSourceProcessing( int numFrames, long periodStartFrameTime )
	{
		long ccs = System.nanoTime();
		
		long ccpp = System.nanoTime();
		
		RenderingPlan rp = null;
		long  clockCallbackPostRpFetch = -1;
		try
		{
			rp = appRenderingGraph.getAtomicRenderingPlan().get();
			
			clockCallbackPostRpFetch = System.nanoTime();
			
			MadChannelPeriodData autpd = timingService.getTimingSource().getTimingPeriodData();
			autpd.reset( periodStartFrameTime,  numFrames);
			
			clockSourceJobQueueProcessing.doUnblockedJobQueueProcessing(rp, shouldProfileRenderingJobs );
		}
		catch( Exception e )
		{
			String msg = "Exception caught during clock source processing: " + e.toString();
			log.error( msg, e );
			return RealtimeMethodReturnCodeEnum.FAIL_FATAL;
		}
		long ccpl = System.nanoTime();
		
		if( shouldProfileRenderingJobs )
		{
			rp.fillProfilingIfNotFilled(ccs,  ccpp, clockCallbackPostRpFetch,  ccpl );
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public abstract long getCurrentUiFrameTime();
}
