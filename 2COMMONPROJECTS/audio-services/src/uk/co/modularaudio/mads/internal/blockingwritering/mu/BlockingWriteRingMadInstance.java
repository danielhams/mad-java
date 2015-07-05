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

package uk.co.modularaudio.mads.internal.blockingwritering.mu;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.internal.InternalComponentsCreationContext;
import uk.co.modularaudio.util.audio.buffer.BlockingWriteRingBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class BlockingWriteRingMadInstance extends MadInstance<BlockingWriteRingMadDefinition, BlockingWriteRingMadInstance>
{
	private static Log log = LogFactory.getLog( BlockingWriteRingMadInstance.class.getName() );

	private int periodLength;
	private int sampleRate;

	private BlockingWriteRingBuffer leftRingBuffer;
	private BlockingWriteRingBuffer rightRingBuffer;

	public BlockingWriteRingMadInstance( final InternalComponentsCreationContext creationContext,
			final String instanceName,
			final BlockingWriteRingMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();
		periodLength = hardwareChannelSettings.getAudioChannelSetting().getChannelBufferLength();

		leftRingBuffer = new BlockingWriteRingBuffer( periodLength * 8 );
		rightRingBuffer = new BlockingWriteRingBuffer( periodLength * 8 );
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage ,
			final MadTimingParameters timingParameters ,
			final long periodStartFrameTime ,
			final MadChannelConnectedFlags channelConnectedFlags ,
			final MadChannelBuffer[] channelBuffers , final int frameOffset , final int numFrames  )
	{
//		log.trace( "Doing stuff in blocking write ring" );
		int numAvail = leftRingBuffer.getNumReadable();
		int numToRead = ( numFrames < numAvail ? numFrames : numAvail );
		int numToPad = (numToRead < numFrames ? numFrames - numToRead : 0 );
		if( log.isWarnEnabled() && numToPad > 0 )
		{
			log.warn( "Underflowed from left blocking ring by " + numToPad + " samples" );
		}

		final MadChannelBuffer outLeftCb = channelBuffers[ BlockingWriteRingMadDefinition.PRODUCER_LEFT ];
		final float outLeftBuf[] = outLeftCb.floatBuffer;

		try
		{
			leftRingBuffer.readMaybeBlock( outLeftBuf, 0, numToRead );
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught during ring buffer read: " + e.toString();
			log.error( msg, e );
		}

		for( int i = 0 ; i < numToPad ; i++ )
		{
			outLeftBuf[ numToRead + i ] = 0.0f;
		}

		numAvail = rightRingBuffer.getNumReadable();
		numToRead = ( numFrames < numAvail ? numFrames : numAvail );
		numToPad = (numToRead < numFrames ? numFrames - numToRead : 0 );
		if( log.isWarnEnabled() && numToPad > 0 )
		{
			log.warn( "Underflowed from right blocking ring by " + numToPad + " samples" );
		}

		final MadChannelBuffer outRightCb = channelBuffers[ BlockingWriteRingMadDefinition.PRODUCER_RIGHT ];
		final float outRightBuf[] = outRightCb.floatBuffer;

		try
		{
			rightRingBuffer.readMaybeBlock( outRightBuf, 0, numToRead );
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught during ring buffer read: " + e.toString();
			log.error( msg, e );
		}

		for( int i = 0 ; i < numToPad ; i++ )
		{
			outRightBuf[ numToRead + i ] = 0.0f;
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public BlockingWriteRingBuffer getLeftRingBuffer()
	{
		return leftRingBuffer;
	}

	public BlockingWriteRingBuffer getRightRingBuffer()
	{
		return rightRingBuffer;
	}

	public int getSampleRate()
	{
		return sampleRate;
	}
}
