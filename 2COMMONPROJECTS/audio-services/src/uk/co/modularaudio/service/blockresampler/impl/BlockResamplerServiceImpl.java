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

package uk.co.modularaudio.service.blockresampler.impl;

import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.blockresampler.BlockResamplerService;
import uk.co.modularaudio.service.blockresampler.BlockResamplingMethod;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.SampleCachingService;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.hibernate.NoSuchHibernateSessionException;
import uk.co.modularaudio.util.lang.ArrayUtils;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class BlockResamplerServiceImpl implements BlockResamplerService
{
	private static Log log = LogFactory.getLog( BlockResamplerServiceImpl.class.getName() );

	private SampleCachingService sampleCachingService;

	public void init() throws ComponentConfigurationException
	{
		if( sampleCachingService == null )
		{
			throw new ComponentConfigurationException( "Service missing dependencies. Check configuration" );
		}
	}

	public void destroy()
	{
	}

	public void setSampleCachingService( final SampleCachingService sampleCachingService )
	{
		this.sampleCachingService = sampleCachingService;
	}

	protected static final void checkFloatsForMagic( final String bufSource, final float[] buffer, final int offset, final int length )
	{
		for( int i = 0 ; i < length ; ++i )
		{
			if( Math.abs(buffer[ offset + i ])  >= BlockResamplerService.EXCESSIVE_FLOAT )
			{
				if( log.isErrorEnabled() )
				{
					log.error("Magic float error from " + bufSource +
							" at offset " + offset + " and index " + i +
							" with value " + buffer[ offset + i ] );
				}
			}
		}
	}

	protected static final void deinterleaveFetchedSamples( final float[] tmpBuffer,
			final int tmpBufferOffset,
			final int numFramesWithCubicSamples,
			final int leftNonInterleavedIndex, final int rightNonInterleavedIndex)
	{
		for( int s = 0 ; s < numFramesWithCubicSamples ; ++s )
		{
			final int readOffset = tmpBufferOffset + (s * 2);
			tmpBuffer[ leftNonInterleavedIndex + s ] = tmpBuffer[ readOffset ];
			tmpBuffer[ rightNonInterleavedIndex + s ] = tmpBuffer[ readOffset + 1 ];
		}
	}

	@Override
	public RealtimeMethodReturnCodeEnum fetchAndResample( final BlockResamplingClient resamplingClient,
			final int outputSampleRate,
			final float iPlaybackSpeed,
			final float[] outputLeftFloats, final int outputLeftOffset,
			final float[] outputRightFloats, final int outputRightOffset,
			final int numFramesRequired,
			final float[] tmpBuffer,
			final int tmpBufferOffset )
	{
		float playbackSpeed = iPlaybackSpeed;
		final boolean isForwards = playbackSpeed >= 0.0f;

		final InternalResamplingClient realClient = (InternalResamplingClient)resamplingClient;
		final SampleCacheClient scc = realClient.getSampleCacheClient();

		final int sourceSampleRate = scc.getSampleRate();
		if( sourceSampleRate != outputSampleRate )
		{
			final float speedMultiplier = sourceSampleRate / (float)outputSampleRate;
			playbackSpeed *= speedMultiplier;
		}

		long prevSamplePos = realClient.getFramePosition();
		float prevSampleFpOffset = realClient.getFpOffset();

		float firstSampleFpOffset = prevSampleFpOffset + playbackSpeed;
		final int extraInt = (int)Math.floor(firstSampleFpOffset);
		final long firstSamplePos = prevSamplePos + extraInt;
		firstSampleFpOffset -= extraInt;

		final float nonDirectionalFpOffset = ( isForwards ? firstSampleFpOffset : (1.0f - firstSampleFpOffset) );

		final float dx = playbackSpeed * numFramesRequired;

//		log.debug("So first sample pos(" + firstSamplePos + ":" + firstSampleFpOffset +") and our delta is " + dx );

		playbackSpeed = (playbackSpeed < 0.0f ? -playbackSpeed : playbackSpeed );
		// For cubic, we use one frame before and 2 frames after (3 in total)
		// But the number of samples we'll use during the resample run might run over due to
		// the initial fp offset
		final int numFileFramesRequired = 3 +
				(int)Math.ceil((numFramesRequired * playbackSpeed) + nonDirectionalFpOffset);

//		log.debug("We require " + numFileFramesRequired + " frames from the file");

		final long samplesOffset = (isForwards ?
				firstSamplePos - 1 :
				((firstSamplePos + 3) - numFileFramesRequired) );

//		log.debug("We'll start reading from index " + samplesOffset );

		scc.setCurrentFramePosition( samplesOffset );

		final int fileReadBufferOffset = tmpBufferOffset;

		final RealtimeMethodReturnCodeEnum fileReadRc = sampleCachingService.readSamplesForCacheClient(
				scc,
				tmpBuffer,
				fileReadBufferOffset,
				numFileFramesRequired );

		if( fileReadRc != RealtimeMethodReturnCodeEnum.SUCCESS )
		{
			return fileReadRc;
		}

//		checkFloatsForMagic( "postReadSamples", tmpBuffer, fileReadBufferOffset, numFileFramesRequired * 2 );

		// De-interleave
		final int leftDeinterleaveOffset = fileReadBufferOffset +
				numFileFramesRequired + numFileFramesRequired;
		final int rightDeinterleaveOffset = leftDeinterleaveOffset +
				numFileFramesRequired;
		deinterleaveFetchedSamples( tmpBuffer,
				fileReadBufferOffset,
				numFileFramesRequired,
				leftDeinterleaveOffset, rightDeinterleaveOffset );

//		checkFloatsForMagic( "leftdeint", tmpBuffer, leftDeinterleaveOffset, numFileFramesRequired );
//		checkFloatsForMagic( "rightdeint", tmpBuffer, rightDeinterleaveOffset, numFileFramesRequired );

		// Reverse the samples if we are going backwards
		if( !isForwards )
		{
			ArrayUtils.reverse( tmpBuffer, leftDeinterleaveOffset, numFileFramesRequired );
			ArrayUtils.reverse( tmpBuffer, rightDeinterleaveOffset, numFileFramesRequired );
		}

		// Now turn these into the output by resampling
		// we need to start from the current sample and offset to make
		// the fetch work
		realClient.setFpOffset( nonDirectionalFpOffset );
		final RealtimeMethodReturnCodeEnum rsRc = realClient.resample(
				tmpBuffer, leftDeinterleaveOffset,
				tmpBuffer, rightDeinterleaveOffset,
				outputLeftFloats, outputLeftOffset,
				outputRightFloats, outputRightOffset,
				playbackSpeed,
				numFramesRequired,
				numFileFramesRequired );
		if( rsRc != RealtimeMethodReturnCodeEnum.SUCCESS )
		{
			return rsRc;
		}

//		checkFloatsForMagic( "leftResample", outputLeftFloats, outputLeftOffset, numFramesRequired );
//		checkFloatsForMagic( "rightResample", outputRightFloats, outputRightOffset, numFramesRequired );

		// Update our position
		prevSampleFpOffset += dx;
		final int newPosOverflow = (int)Math.floor(prevSampleFpOffset);
		prevSamplePos += newPosOverflow;
		prevSampleFpOffset -= newPosOverflow;

//		log.debug("Updating to new position of " + prevSamplePos + ":" + prevSampleFpOffset +")");

		realClient.setFramePosition( prevSamplePos );
		realClient.setFpOffset( prevSampleFpOffset );

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	@Override
	public RealtimeMethodReturnCodeEnum fetchAndResampleVarispeed(
			final BlockResamplingClient resamplingClient,
			final int outputSampleRate,
			final float[] playbackSpeeds, final int playbackOffset,
			final float[] outputLeftFloats, final int outputLeftOffset,
			final float[] outputRightFloats, final int outputRightOffset,
			final int numFramesRequired,
			final float[] tmpBuffer,
			final int tmpBufferOffset )
	{
		final InternalResamplingClient realClient = (InternalResamplingClient)resamplingClient;
		final SampleCacheClient scc = realClient.getSampleCacheClient();
		final int sourceSampleRate = scc.getSampleRate();

		if( sourceSampleRate != outputSampleRate )
		{
			final float speedMultiplier = sourceSampleRate / (float)outputSampleRate;
			for( int m = 0 ; m < numFramesRequired ; ++m )
			{
				playbackSpeeds[playbackOffset+m] *=speedMultiplier;
			}
		}

		// Need to break it up into sections of "forward" or "backward"
		// so we can read a bunch of samples at once and work on them.
		int numLeft = numFramesRequired;
		int curOffset = 0;

		while( numLeft > 0 )
		{
			final int innerPlaybackSpeedOffset = playbackOffset + curOffset;
			float cumulativeSpeeds = playbackSpeeds[innerPlaybackSpeedOffset];
			final boolean isForwards = cumulativeSpeeds >= 0.0f;
			final int numToTest = numLeft - 1;
			int s = 1;

			if( isForwards )
			{
				// Look for any negative playback speeds
				for( ; s <= numToTest ; ++s )
				{
					final float nextSpeed = playbackSpeeds[innerPlaybackSpeedOffset + s];
					if( nextSpeed < 0.0f )
					{
						break;
					}
					cumulativeSpeeds += nextSpeed;
				}
			}
			else
			{
				// Look for any positive playback speeds
				for( ; s <= numToTest ; ++s )
				{
					final float nextSpeed = playbackSpeeds[innerPlaybackSpeedOffset + s];
					if( nextSpeed >= 0.0f )
					{
						break;
					}
					cumulativeSpeeds += nextSpeed;
				}
			}
			final int numThisRound = s;

			// So our "consistent" (in direction) range runs from curOffset -> curOffset + numThisRound

			// Work out how many samples we'll need to read and from where
			long prevSamplePos = realClient.getFramePosition();
			float prevSampleFpOffset = realClient.getFpOffset();

			float firstSampleFpOffset = prevSampleFpOffset + playbackSpeeds[innerPlaybackSpeedOffset];
			final int extraInt = (int)Math.floor(firstSampleFpOffset);
			final long firstSamplePos = prevSamplePos + extraInt;
			firstSampleFpOffset -= extraInt;

			final float nonDirectionalFpOffset = ( isForwards ? firstSampleFpOffset : (1.0f - firstSampleFpOffset) );

			final float dx = cumulativeSpeeds;
			cumulativeSpeeds = (cumulativeSpeeds < 0.0f ? -cumulativeSpeeds : cumulativeSpeeds);

			final int numFileFramesRequired = 3 +
					(int)Math.ceil(cumulativeSpeeds + nonDirectionalFpOffset);

			final long samplesOffset = (isForwards ?
					firstSamplePos - 1 :
					((firstSamplePos+3) - numFileFramesRequired) );

			// Read them into the temporary buffer
			scc.setCurrentFramePosition( samplesOffset );

			final int fileReadBufferOffset = tmpBufferOffset;

			final RealtimeMethodReturnCodeEnum fileReadRc = sampleCachingService.readSamplesForCacheClient(
					scc,
					tmpBuffer,
					fileReadBufferOffset,
					numFileFramesRequired );

			if( fileReadRc != RealtimeMethodReturnCodeEnum.SUCCESS )
			{
				return fileReadRc;
			}

//			checkFloatsForMagic( "postReadSamples", tmpBuffer, fileReadBufferOffset, numFileFramesRequired * 2 );

			// De-interleave them
			final int leftDeinterleaveOffset = fileReadBufferOffset +
					numFileFramesRequired + numFileFramesRequired;
			final int rightDeinterleaveOffset = leftDeinterleaveOffset +
					numFileFramesRequired;
			deinterleaveFetchedSamples( tmpBuffer,
					fileReadBufferOffset,
					numFileFramesRequired,
					leftDeinterleaveOffset, rightDeinterleaveOffset );

//			checkFloatsForMagic( "leftdeint", tmpBuffer, leftDeinterleaveOffset, numFileFramesRequired );
//			checkFloatsForMagic( "rightdeint", tmpBuffer, rightDeinterleaveOffset, numFileFramesRequired );

			// If going backwards, reverse them
			if( !isForwards )
			{
				ArrayUtils.reverse( tmpBuffer, leftDeinterleaveOffset, numFileFramesRequired );
				ArrayUtils.reverse( tmpBuffer, rightDeinterleaveOffset, numFileFramesRequired );
			}

			// Resample them using the speeds + sample rate difference
			realClient.setFpOffset( nonDirectionalFpOffset );

			final RealtimeMethodReturnCodeEnum rsRc = realClient.resampleVarispeed(
					tmpBuffer, leftDeinterleaveOffset,
					tmpBuffer, rightDeinterleaveOffset,
					outputLeftFloats, outputLeftOffset + curOffset,
					outputRightFloats, outputRightOffset + curOffset,
					playbackSpeeds, innerPlaybackSpeedOffset, (isForwards ? 1 : -1),
					numThisRound,
					numFileFramesRequired );

			if( rsRc != RealtimeMethodReturnCodeEnum.SUCCESS )
			{
				return rsRc;
			}

//			checkFloatsForMagic( "leftResample", outputLeftFloats, outputLeftOffset+curOffset, numThisRound );
//			checkFloatsForMagic( "rightResample", outputRightFloats, outputRightOffset+curOffset, numThisRound );

			// Update the last sample output position
			prevSampleFpOffset += dx;
			final int newPosOverflow = (int)Math.floor(prevSampleFpOffset);
			prevSamplePos += newPosOverflow;
			prevSampleFpOffset -= newPosOverflow;

			realClient.setFramePosition( prevSamplePos );
			realClient.setFpOffset( prevSampleFpOffset );

			curOffset += numThisRound;
			numLeft -= numThisRound;
		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	@Override
	public BlockResamplingClient createResamplingClient( final String pathToFile,
			final BlockResamplingMethod resamplingMethod )
			throws DatastoreException, IOException, UnsupportedAudioFileException
	{
		SampleCacheClient scc;
		try
		{
			scc = sampleCachingService.registerCacheClientForFile(pathToFile);
			final InternalResamplingClient brc = new InternalResamplingClient( scc,
					resamplingMethod,
					-1,
					0.0f);
			return brc;
		}
		catch( final NoSuchHibernateSessionException nshe )
		{
			final String msg = "Missing hibernate session during register of sample cache client: " + nshe.toString();
			log.error( msg, nshe );
			throw new DatastoreException( msg, nshe );
		}
	}

	@Override
	public BlockResamplingClient promoteSampleCacheClientToResamplingClient(
			final SampleCacheClient sampleCacheClient,
			final BlockResamplingMethod resamplingMethod )
	{
		final long clientPosition = sampleCacheClient.getCurrentFramePosition();
		return new InternalResamplingClient( sampleCacheClient, resamplingMethod, clientPosition-1, 0.0f );
	}

	@Override
	public void destroyResamplingClient( final BlockResamplingClient resamplingClient )
			throws DatastoreException, RecordNotFoundException
	{
		final InternalResamplingClient brc = (InternalResamplingClient)resamplingClient;
		final SampleCacheClient scc = brc.getSampleCacheClient();
		sampleCachingService.unregisterCacheClientForFile(scc);
	}

}
