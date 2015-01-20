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

package uk.co.modularaudio.mads.base.spectralamp.mu;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.buffer.BackendToFrontendDataRingBuffer;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class SpectralAmpMadInstance extends MadInstance<SpectralAmpMadDefinition,SpectralAmpMadInstance>
{
	private static Log log = LogFactory.getLog( SpectralAmpMadInstance.class.getName() );
	
	protected boolean active = false;
	
	protected int maxRingBufferingInSamples = -1;
	
	private BackendToFrontendDataRingBuffer dataRingBuffer = null;
	
	private int numSamplePerFrontEndPeriod = -1;

	public SpectralAmpMadInstance( BaseComponentsCreationContext creationContext,
			String instanceName,
			SpectralAmpMadDefinition definition,
			Map<MadParameterDefinition, String> creationParameterValues,
			MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
		dataRingBuffer = new BackendToFrontendDataRingBuffer( 1 );
	}

	@Override
	public void startup( HardwareIOChannelSettings hardwareChannelSettings, MadTimingParameters timingParameters, MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		try
		{
			int sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();
			
			long nanosFeBuffering = timingParameters.getNanosPerFrontEndPeriod() * 2;
			long nanosBeBuffering = timingParameters.getNanosPerBackEndPeriod() * 2;
			long nanosForBuffering = nanosFeBuffering + nanosBeBuffering;
			
			maxRingBufferingInSamples = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, 16 ) +
				AudioTimingUtils.getNumSamplesForNanosAtSampleRate( sampleRate, nanosForBuffering );
			
			dataRingBuffer = new BackendToFrontendDataRingBuffer( maxRingBufferingInSamples );
			dataRingBuffer.setNumSamplesQueued( 0 );
			
			numSamplePerFrontEndPeriod = timingParameters.getSampleFramesPerFrontEndPeriod();
		}
		catch (Exception e)
		{
			throw new MadProcessingException( e );
		}
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( ThreadSpecificTemporaryEventStorage tempQueueEntryStorage,
			MadTimingParameters timingParameters,
			long periodStartTimestamp,
			MadChannelConnectedFlags channelConnectedFlags,
			MadChannelBuffer[] channelBuffers,
			int numFrames )
	{
		boolean inConnected = channelConnectedFlags.get( SpectralAmpMadDefinition.CONSUMER_IN);
		MadChannelBuffer inCb = channelBuffers[ SpectralAmpMadDefinition.CONSUMER_IN ];
		float[] inFloats = (inConnected ? inCb.floatBuffer : null );
		
		if( active )
		{
			try
			{
				if( inConnected )
				{
					int curSampleIndex = 0;
					while( curSampleIndex < numFrames )
					{
						long timestampForIndexUpdate = periodStartTimestamp + curSampleIndex;
						
						if( dataRingBuffer.getNumSamplesQueued() >= numSamplePerFrontEndPeriod )
						{
							queueWriteIndexUpdate( tempQueueEntryStorage,
								0,
								dataRingBuffer.getWritePosition(),
								timestampForIndexUpdate );
							dataRingBuffer.setNumSamplesQueued( 0 );
							
							postProcess( tempQueueEntryStorage, timingParameters, timestampForIndexUpdate );
							preProcess( tempQueueEntryStorage, timingParameters, timestampForIndexUpdate );
						}
						
						int numLeft = numSamplePerFrontEndPeriod - dataRingBuffer.getNumSamplesQueued();
						
						int numAvailable = numFrames - curSampleIndex;
						int numThisRound = ( numLeft > numAvailable ? numAvailable : numLeft );
						
						int spaceAvailable = dataRingBuffer.getNumWriteable();
						
						int numToWrite = ( spaceAvailable > numThisRound ? numThisRound : spaceAvailable );
						
						if( numToWrite > 0 )
						{
							dataRingBuffer.write( inFloats, curSampleIndex, numToWrite );
							dataRingBuffer.setNumSamplesQueued( dataRingBuffer.getNumSamplesQueued() + numToWrite );
						}
						else
						{
							break;
						}
						curSampleIndex += numThisRound;
					}
				}

			}
			catch( Exception e )
			{
				String msg = "Exception caught processing into ring buffer: " + e.toString();
				log.error( msg, e );
			}
		}
		
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
	
	protected void queueWriteIndexUpdate( ThreadSpecificTemporaryEventStorage tses,
			int dataChannelNum,
			int writePosition,
			long frameTime )
	{
		long joinedParts = ((long)writePosition << 32 ) | (dataChannelNum );
		
		localBridge.queueTemporalEventToUi( tses,
			frameTime,
			SpectralAmpIOQueueBridge.COMMAND_OUT_RINGBUFFER_WRITE_INDEX,
			joinedParts, 
			null );
	}
	
	public BackendToFrontendDataRingBuffer getDataRingBuffer()
	{
		return dataRingBuffer;
	}
}
