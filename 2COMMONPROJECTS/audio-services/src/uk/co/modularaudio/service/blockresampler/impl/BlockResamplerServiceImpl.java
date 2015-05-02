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
import java.util.Arrays;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.blockresampler.BlockResamplerService;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.blockresampler.BlockResamplingMethod;
import uk.co.modularaudio.service.blockresampler.NTBlockResamplingClient;
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
	}

	public void destroy()
	{
	}

	public void setSampleCachingService( final SampleCachingService sampleCachingService )
	{
		this.sampleCachingService = sampleCachingService;
	}

	@Override
	public RealtimeMethodReturnCodeEnum sampleClientFetchFramesResample( final float[] tmpBuffer,
			final int tmpBufferOffset,
			final BlockResamplingClient resamplingClient,
			final int outputSampleRate,
			final float playbackSpeed,
			final float[] outputLeftFloats,
			final float[] outputRightFloats,
			final int outputPos,
			final int numFramesRequired,
			final boolean addToOutput )
	{
		return sampleClientUnifiedFetchAndResample( tmpBuffer,
				tmpBufferOffset,
				(InternalResamplingClient)resamplingClient,
				outputSampleRate,
				playbackSpeed,
				outputLeftFloats, outputRightFloats,
				outputPos,
				numFramesRequired,
				addToOutput,
				false,
				null );
	}

	@Override
	public RealtimeMethodReturnCodeEnum sampleClientFetchFramesResampleWithAmps( final float[] tmpBuffer,
			final int tmpBufferOffset,
			final BlockResamplingClient resamplingClient,
			final int outputSampleRate,
			final float playbackSpeed,
			final float[] outputLeftFloats,
			final float[] outputRightFloats,
			final int outputPos,
			final int numFramesRequired,
			final float[] requiredAmps,
			final boolean addToOutput )
	{
		return sampleClientUnifiedFetchAndResample( tmpBuffer,
				tmpBufferOffset,
				(InternalResamplingClient)resamplingClient,
				outputSampleRate,
				playbackSpeed,
				outputLeftFloats, outputRightFloats,
				outputPos,
				numFramesRequired,
				addToOutput,
				true,
				requiredAmps );
	}

	private RealtimeMethodReturnCodeEnum sampleClientUnifiedFetchAndResample( final float[] tmpBuffer,
		final int tmpBufferOffset,
		final InternalResamplingClient resamplingClient,
		final int outputSampleRate,
		float playbackSpeed,
		final float[] outputLeftFloats,
		final float[] outputRightFloats,
		final int outputPos,
		final int numFramesRequired,
		final boolean addToOutput,
		final boolean haveAmps,
		final float[] amps )
	{

		final SampleCacheClient sampleCacheClient = resamplingClient.getSampleCacheClient();

		final int sourceSampleRate = sampleCacheClient.getSampleRate();
		if( sourceSampleRate != outputSampleRate )
		{
			final float speedMultiplier = sourceSampleRate / (float)outputSampleRate;
			playbackSpeed *= speedMultiplier;
		}
//		log.debug("Playback speed currently " + playbackSpeed );

//		float absSpeed = Math.abs(playbackSpeed);

		final boolean isForwards = (playbackSpeed >= 0.0f);
		if( !isForwards )
		{
			playbackSpeed = -playbackSpeed;
		}

		final long curFramePosition = resamplingClient.getFramePosition();
		final float curFpOffset = resamplingClient.getFpOffset();
//		log.debug("curFramePosition(" + curFramePosition + ") and startFpOffset(" + MathFormatter.slowDoublePrint(curFpOffset, 3, false ) + ")");

		final float fpNumFramesNeeded = (numFramesRequired * playbackSpeed) + (isForwards ? curFpOffset : 1.0f-curFpOffset);
		final int intNumFramesNeeded = (int)Math.ceil(fpNumFramesNeeded);

//		log.debug("Resample at frame(" + curFramePosition +") and fpoffset(" + curFpOffset +") need " + fpNumFramesNeeded +
//				" frames at speed " + playbackSpeed + " will read " + intNumFramesNeeded );

		// Read needed first sample for cubic interpolation and two samples at the end
		final int numFramesWithCubicSamples = intNumFramesNeeded + 3;

		long adjustedFramePosition;
		float adjustedFpOffset;

		if( isForwards )
		{
			// We need the "previous" sample too - we'll let the sample caching service fill in zeros
			adjustedFramePosition = curFramePosition - 1;
			adjustedFpOffset = curFpOffset + playbackSpeed;
			if( adjustedFpOffset >= 1.0f )
			{
				final int extraInt = (int)adjustedFpOffset;
				adjustedFramePosition += extraInt;
				adjustedFpOffset -= extraInt;
			}
		}
		else
		{
			adjustedFramePosition = (curFramePosition + 2) - numFramesWithCubicSamples;

			final float deltaOffset = (curFpOffset - playbackSpeed );
			final int deltaOffsetInt = (int)deltaOffset;
			adjustedFramePosition += deltaOffsetInt;
			adjustedFpOffset = deltaOffset - deltaOffsetInt;

			if( adjustedFpOffset < 0.0f )
			{
				adjustedFpOffset = -adjustedFpOffset;
			}
			else
			{
				adjustedFramePosition += 1;
				adjustedFpOffset = 1.0f - adjustedFpOffset;
			}
		}

//		log.debug("adjustedFramePosition(" + adjustedFramePosition + ") and adjustedFpOffset(" + MathFormatter.slowFloatPrint(adjustedFpOffset, 3, false ) + ")");

		sampleCacheClient.setCurrentFramePosition( adjustedFramePosition );

		final RealtimeMethodReturnCodeEnum retCode = sampleCachingService.readSamplesForCacheClient( sampleCacheClient,
				tmpBuffer,
				tmpBufferOffset,
				numFramesWithCubicSamples );

		if( retCode != RealtimeMethodReturnCodeEnum.SUCCESS )
		{
			log.debug("Reading samples has failed.");
			// Over the end of the sample - output zeros
			Arrays.fill( outputLeftFloats, 0.0f );
			Arrays.fill( outputRightFloats, 0.0f );
			return retCode;
		}

		final int numFloats = numFramesWithCubicSamples * 2;
		checkFloatsForMagic( "readSamples", tmpBuffer, tmpBufferOffset, numFloats );

		// Split up into two streams of samples in the temporary buffer
		final int leftNonInterleavedIndex =
				tmpBufferOffset + numFramesWithCubicSamples + numFramesWithCubicSamples;
		final int rightNonInterleavedIndex =
				leftNonInterleavedIndex + numFramesWithCubicSamples;
		deinterleaveFetchedSamples( tmpBuffer,
				tmpBufferOffset,
				numFramesWithCubicSamples,
				leftNonInterleavedIndex,
				rightNonInterleavedIndex);

		checkFloatsForMagic( "deinterleave left", tmpBuffer, leftNonInterleavedIndex, numFramesWithCubicSamples );
		checkFloatsForMagic( "deinterleave right", tmpBuffer, rightNonInterleavedIndex, numFramesWithCubicSamples );

		if( !isForwards )
		{
			ArrayUtils.reverse( tmpBuffer, leftNonInterleavedIndex, numFramesWithCubicSamples );
			ArrayUtils.reverse( tmpBuffer, rightNonInterleavedIndex, numFramesWithCubicSamples );
		}

		checkFloatsForMagic( "reverse left", tmpBuffer, leftNonInterleavedIndex, numFramesWithCubicSamples );
		checkFloatsForMagic( "reverse right", tmpBuffer, rightNonInterleavedIndex, numFramesWithCubicSamples );

		final BlockResamplingMethod resamplingMethod = resamplingClient.getResamplingMethod();

		// Now resample output into the given arrays
		if( addToOutput )
		{
			interpolateAdd( resamplingMethod, tmpBuffer, leftNonInterleavedIndex, adjustedFpOffset,
					outputLeftFloats, outputPos, numFramesRequired, playbackSpeed, haveAmps, amps );
			interpolateAdd( resamplingMethod, tmpBuffer, rightNonInterleavedIndex, adjustedFpOffset,
					outputRightFloats, outputPos, numFramesRequired, playbackSpeed, haveAmps, amps );
		}
		else
		{
			interpolate( resamplingMethod, tmpBuffer, leftNonInterleavedIndex, adjustedFpOffset,
					outputLeftFloats, outputPos, numFramesRequired, playbackSpeed, haveAmps, amps );
			interpolate( resamplingMethod, tmpBuffer, rightNonInterleavedIndex, adjustedFpOffset,
					outputRightFloats, outputPos, numFramesRequired, playbackSpeed, haveAmps, amps );
		}

		checkFloatsForMagic( "interpolate left", outputLeftFloats, outputPos, numFramesRequired );
		checkFloatsForMagic( "interpolate right", outputRightFloats, outputPos, numFramesRequired );

		final int overflowInt = (int)fpNumFramesNeeded;
        final float overflowFpOffset = fpNumFramesNeeded - overflowInt;

		long newFramePosition;
		float newFpOffset;

        if( isForwards )
        {
        	newFramePosition = curFramePosition + overflowInt;
    		newFpOffset = curFpOffset + overflowFpOffset;
    		if( newFpOffset >= 1.0f )
    		{
    			final int extraInt = (int)newFpOffset;
    			newFramePosition += extraInt;
    			newFpOffset -= extraInt;
    		}
        }
        else
        {
        	newFramePosition = curFramePosition - overflowInt;
        	newFpOffset = curFpOffset - overflowFpOffset;
        	if( newFpOffset < 0.0f )
        	{
        		newFramePosition -= 1;
        		newFpOffset += 1.0f;
        	}
        }

//		log.debug("Updating position from " + curFramePosition + " offset " + curFpOffset + " at speed " + playbackSpeed + " to " + newFramePosition +
///				" with FpOffset "  + newFpOffset );

        resamplingClient.setFramePosition( newFramePosition );
        resamplingClient.setFpOffset( newFpOffset );

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	private static void checkFloatsForMagic( final String bufSource, final float[] buffer, final int offset, final int length )
	{
		for( int i = 0 ; i < length ; ++i )
		{
			if( Math.abs(buffer[ offset + i ])  >= BlockResamplerService.EXCESSIVE_FLOAT )
			{
				log.error("Magic float error from " + bufSource);
			}
		}
	}

	private void deinterleaveFetchedSamples(final float[] tmpBuffer,
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

	private void interpolate( final BlockResamplingMethod resamplingMethod,
			final float[] sourceBuffer,
			final int sourceIndex,
			final float sourceFrac,
			final float[] output,
			final int outputPos,
			final int numFramesRequired,
			final float playbackSpeed,
			final boolean haveAmps,
			final float[] amps )
	{
		if( haveAmps )
		{
			switch( resamplingMethod )
			{
				case CUBIC:
				{
					interpolateCubicLoop(sourceBuffer, sourceIndex, sourceFrac, output, outputPos, numFramesRequired, playbackSpeed);
					for( int s = 0 ; s < numFramesRequired ; ++s )
					{
						output[ outputPos + s ] *= amps[s];
					}
					break;
				}
				case LINEAR:
				{
					interpolateLinearLoop(sourceBuffer, sourceIndex, sourceFrac, output, outputPos, numFramesRequired, playbackSpeed);
					for( int s = 0 ; s < numFramesRequired ; ++s )
					{
						output[ outputPos + s ] *= amps[s];
					}
					break;
				}
				case NEAREST:
				{
					interpolateNearestLoop(sourceBuffer, sourceIndex, sourceFrac, output, outputPos, numFramesRequired, playbackSpeed);
					for( int s = 0 ; s < numFramesRequired ; ++s )
					{
						output[ outputPos + s ] *= amps[s];
					}
					break;
				}
				default:
				{
					log.warn("Unknown interpolation method. Will fill with zeros");
					Arrays.fill( output, outputPos, outputPos + numFramesRequired, 0.0f );
				}
			}
		}
		else
		{
			switch( resamplingMethod )
			{
				case CUBIC:
				{
					interpolateCubicLoop(sourceBuffer, sourceIndex, sourceFrac, output, outputPos, numFramesRequired, playbackSpeed);
					break;
				}
				case LINEAR:
				{
					interpolateLinearLoop(sourceBuffer, sourceIndex, sourceFrac, output, outputPos, numFramesRequired, playbackSpeed);
					break;
				}
				case NEAREST:
				{
					interpolateNearestLoop(sourceBuffer, sourceIndex, sourceFrac, output, outputPos, numFramesRequired, playbackSpeed);
					break;
				}
				default:
				{
					log.warn("Unknown interpolation method. Will fill with zeros");
					Arrays.fill( output, outputPos, outputPos + numFramesRequired, 0.0f );
				}
			}
		}
	}

	private void interpolateAdd( final BlockResamplingMethod resamplingMethod, final float[] sourceBuffer, final int sourceIndex, final float sourceFrac,
		final float[] output, final int outputPos, final int numFramesRequired, final float playbackSpeed, final boolean haveAmps, final float[] amps )
	{
		if( haveAmps )
		{
			switch( resamplingMethod )
			{
				case CUBIC:
				{
					interpolateAddCubicLoopAmps(sourceBuffer, sourceIndex, sourceFrac, output, outputPos, numFramesRequired, playbackSpeed, amps);
					break;
				}
				case LINEAR:
				{
					interpolateAddLinearLoopAmp(sourceBuffer, sourceIndex, sourceFrac, output, outputPos, numFramesRequired, playbackSpeed, amps);
					break;
				}
				case NEAREST:
				{
					interpolateAddNearestLoopAmps(sourceBuffer, sourceIndex, sourceFrac, output, outputPos, numFramesRequired, playbackSpeed, amps);
					break;
				}
				default:
				{
					log.warn("Unknown interpolation method. Will fill with zeros");
					Arrays.fill( output, outputPos, outputPos + numFramesRequired, 0.0f );
				}
			}
		}
		else
		{
			switch( resamplingMethod )
			{
				case CUBIC:
				{
					interpolateAddCubicLoop(sourceBuffer, sourceIndex, sourceFrac, output, outputPos, numFramesRequired, playbackSpeed);
					break;
				}
				case LINEAR:
				{
					interpolateAddLinearLoop(sourceBuffer, sourceIndex, sourceFrac, output, outputPos, numFramesRequired, playbackSpeed);
					break;
				}
				case NEAREST:
				{
					interpolateAddNearestLoop(sourceBuffer, sourceIndex, sourceFrac, output, outputPos, numFramesRequired, playbackSpeed);
					break;
				}
				default:
				{
					log.warn("Unknown interpolation method. Will fill with zeros");
					Arrays.fill( output, outputPos, outputPos + numFramesRequired, 0.0f );
				}
			}
		}
	}

	private void interpolateCubicLoop( final float[] sourceBuffer, int sourceIndex, float frac,
		final float[] output, final int outputPos, final int numFramesRequired, final float playbackSpeed )
	{
		for( int s = 0 ; s < numFramesRequired ; ++s )
		{
			final float y0 = sourceBuffer[ sourceIndex ];
			final float y1 = sourceBuffer[ sourceIndex + 1 ];
			final float y2 = sourceBuffer[ sourceIndex + 2 ];
			final float y3 = sourceBuffer[ sourceIndex + 3 ];
//			log.debug("CubicInterpolate between y0(" + y0 + ") y1(" + y1 + ") y2(" + y2 + ") y3(" + y3 + ")");

			if( Math.abs(y0) >= BlockResamplerService.EXCESSIVE_FLOAT ||
					Math.abs(y1) >= BlockResamplerService.EXCESSIVE_FLOAT ||
					Math.abs(y2) >= BlockResamplerService.EXCESSIVE_FLOAT ||
					Math.abs(y3) >= BlockResamplerService.EXCESSIVE_FLOAT )
			{
				log.error("Failed on frame " + s + " with vals " + y0 +
						" " + y1 +
						" " + y2 +
						" " + y3 );
			}

			final float fracSq = frac * frac;

//			float a0 = y3 - y2 - y0 + y1;
//			float a1 = y0 - y1 - a0;
//			float a2 = y2 - y0;
//			float a3 = y1;

			final float a0 = -0.5f*y0 + 1.5f*y1 - 1.5f*y2 + 0.5f*y3;
			final float a1 = y0 - 2.5f*y1 + 2.0f*y2 - 0.5f*y3;
			final float a2 = -0.5f*y0 + 0.5f*y2;
			final float a3 = y1;

			output[ outputPos + s ] = (a0 * frac * fracSq) + (a1 * fracSq) + (a2 * frac) + a3;
			// Update source position using speed
			frac += playbackSpeed;
			// Unnecessary branch...
//			if( frac > 1.0f )
//			{
				final int extraInt = (int)frac;
				sourceIndex += extraInt;
				frac -= extraInt;
//			}
		}
	}

	private void interpolateLinearLoop( final float[] sourceBuffer, int sourceIndex, float frac,
		final float[] output, final int outputPos, final int numFramesRequired, final float playbackSpeed )
	{
		for( int s = 0 ; s < numFramesRequired ; ++s )
		{
			final float y0 = sourceBuffer[ sourceIndex ];
			final float y1 = sourceBuffer[ sourceIndex + 1 ];

			if( Math.abs(y0) >= BlockResamplerService.EXCESSIVE_FLOAT ||
					Math.abs(y1) >= BlockResamplerService.EXCESSIVE_FLOAT )
			{
				log.error("Failed on frame " + s + " with vals " + y0 +
						" " + y1 );
			}


			output[ outputPos + s ] = (y0 * (1.0f - frac)) + (y1 * frac);
			// Update source position using speed
			frac += playbackSpeed;
			// Unnecessary branch...
//				if( frac > 1.0f )
//				{
				final int extraInt = (int)frac;
				sourceIndex += extraInt;
				frac -= extraInt;
//				}
		}
	}

	private void interpolateNearestLoop( final float[] sourceBuffer, int sourceIndex, float frac,
		final float[] output, final int outputPos, final int numFramesRequired, final float playbackSpeed )
	{
		for( int s = 0 ; s < numFramesRequired ; ++s )
		{
			final float y0 = sourceBuffer[ sourceIndex ];
			final float y1 = sourceBuffer[ sourceIndex + 1 ];

			output[ outputPos + s ] = ( frac < 0.5f ? y0 : y1);
			// Update source position using speed
			frac += playbackSpeed;
			// Unnecessary branch...
//					if( frac > 1.0f )
//					{
				final int extraInt = (int)frac;
				sourceIndex += extraInt;
				frac -= extraInt;
//					}
		}
	}

	private void interpolateAddCubicLoop( final float[] sourceBuffer, int sourceIndex, float frac,
		final float[] output, final int outputPos, final int numFramesRequired, final float playbackSpeed )
	{
		for( int s = 0 ; s < numFramesRequired ; ++s )
		{
			final float y0 = sourceBuffer[ sourceIndex ];
			final float y1 = sourceBuffer[ sourceIndex + 1 ];
			final float y2 = sourceBuffer[ sourceIndex + 2 ];
			final float y3 = sourceBuffer[ sourceIndex + 3 ];
//				log.debug("CubicInterpolate between y0(" + y0 + ") y1(" + y1 + ") y2(" + y2 + ") y3(" + y3 + ")");

			final float fracSq = frac * frac;

//				float a0 = y3 - y2 - y0 + y1;
//				float a1 = y0 - y1 - a0;
//				float a2 = y2 - y0;
//				float a3 = y1;

			final float a0 = -0.5f*y0 + 1.5f*y1 - 1.5f*y2 + 0.5f*y3;
			final float a1 = y0 - 2.5f*y1 + 2.0f*y2 - 0.5f*y3;
			final float a2 = -0.5f*y0 + 0.5f*y2;
			final float a3 = y1;

			output[ outputPos + s ] += (a0 * frac * fracSq) + (a1 * fracSq) + (a2 * frac) + a3;
			// Update source position using speed
			frac += playbackSpeed;
			// Unnecessary branch...
//				if( frac > 1.0f )
//				{
				final int extraInt = (int)frac;
				sourceIndex += extraInt;
				frac -= extraInt;
//				}
		}
	}

	private void interpolateAddLinearLoop( final float[] sourceBuffer, int sourceIndex, float frac,
		final float[] output, final int outputPos, final int numFramesRequired, final float playbackSpeed )
	{
		for( int s = 0 ; s < numFramesRequired ; ++s )
		{
			final float y0 = sourceBuffer[ sourceIndex ];
			final float y1 = sourceBuffer[ sourceIndex + 1 ];

			output[ outputPos + s ] += (y0 * (1.0f - frac)) + (y1 * frac);
			// Update source position using speed
			frac += playbackSpeed;
			// Unnecessary branch...
//					if( frac > 1.0f )
//					{
				final int extraInt = (int)frac;
				sourceIndex += extraInt;
				frac -= extraInt;
//					}
		}
	}

	private void interpolateAddNearestLoop( final float[] sourceBuffer, int sourceIndex, float frac,
		final float[] output, final int outputPos, final int numFramesRequired, final float playbackSpeed )
	{
		for( int s = 0 ; s < numFramesRequired ; ++s )
		{
			final float y0 = sourceBuffer[ sourceIndex ];
			final float y1 = sourceBuffer[ sourceIndex + 1 ];

			output[ outputPos + s ] += ( frac < 0.5f ? y0 : y1);
			// Update source position using speed
			frac += playbackSpeed;
			// Unnecessary branch...
//						if( frac > 1.0f )
//						{
				final int extraInt = (int)frac;
				sourceIndex += extraInt;
				frac -= extraInt;
//						}
		}
	}

	private void interpolateAddCubicLoopAmps( final float[] sourceBuffer, int sourceIndex, float frac,
		final float[] output, final int outputPos, final int numFramesRequired, final float playbackSpeed, final float[] amps  )
	{
		for( int s = 0 ; s < numFramesRequired ; ++s )
		{
			final float y0 = sourceBuffer[ sourceIndex ];
			final float y1 = sourceBuffer[ sourceIndex + 1 ];
			final float y2 = sourceBuffer[ sourceIndex + 2 ];
			final float y3 = sourceBuffer[ sourceIndex + 3 ];
//					log.debug("CubicInterpolate between y0(" + y0 + ") y1(" + y1 + ") y2(" + y2 + ") y3(" + y3 + ")");

			final float fracSq = frac * frac;

//					float a0 = y3 - y2 - y0 + y1;
//					float a1 = y0 - y1 - a0;
//					float a2 = y2 - y0;
//					float a3 = y1;

			final float a0 = -0.5f*y0 + 1.5f*y1 - 1.5f*y2 + 0.5f*y3;
			final float a1 = y0 - 2.5f*y1 + 2.0f*y2 - 0.5f*y3;
			final float a2 = -0.5f*y0 + 0.5f*y2;
			final float a3 = y1;

			output[ outputPos + s ] += ((a0 * frac * fracSq) + (a1 * fracSq) + (a2 * frac) + a3) * amps[s];
			// Update source position using speed
			frac += playbackSpeed;
			// Unnecessary branch...
//					if( frac > 1.0f )
//					{
				final int extraInt = (int)frac;
				sourceIndex += extraInt;
				frac -= extraInt;
//					}
		}
	}

	private void interpolateAddLinearLoopAmp( final float[] sourceBuffer, int sourceIndex, float frac,
		final float[] output, final int outputPos, final int numFramesRequired, final float playbackSpeed, final float[] amps )
	{
		for( int s = 0 ; s < numFramesRequired ; ++s )
		{
			final float y0 = sourceBuffer[ sourceIndex ];
			final float y1 = sourceBuffer[ sourceIndex + 1 ];

			output[ outputPos + s ] += ((y0 * (1.0f - frac)) + (y1 * frac)) * amps[s];
			// Update source position using speed
			frac += playbackSpeed;
			// Unnecessary branch...
//						if( frac > 1.0f )
//						{
				final int extraInt = (int)frac;
				sourceIndex += extraInt;
				frac -= extraInt;
//						}
		}
	}

	private void interpolateAddNearestLoopAmps( final float[] sourceBuffer, int sourceIndex, float frac,
		final float[] output, final int outputPos, final int numFramesRequired, final float playbackSpeed, final float[] amps )
	{
		for( int s = 0 ; s < numFramesRequired ; ++s )
		{
			final float y0 = sourceBuffer[ sourceIndex ];
			final float y1 = sourceBuffer[ sourceIndex + 1 ];

			output[ outputPos + s ] += (( frac < 0.5f ? y0 : y1)) * amps[s];
			// Update source position using speed
			frac += playbackSpeed;
			// Unnecessary branch...
//							if( frac > 1.0f )
//							{
				final int extraInt = (int)frac;
				sourceIndex += extraInt;
				frac -= extraInt;
//							}
		}
	}

	@Override
	public BlockResamplingClient createResamplingClient( final String pathToFile, final BlockResamplingMethod resamplingMethod )
			throws DatastoreException, IOException, UnsupportedAudioFileException
	{
		SampleCacheClient scc;
		try
		{
			scc = sampleCachingService.registerCacheClientForFile(pathToFile);
			final InternalResamplingClient brc = new InternalResamplingClient(scc, resamplingMethod, 0, 0.0f);
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
	public BlockResamplingClient promoteSampleCacheClientToResamplingClient( final SampleCacheClient sampleCacheClient,
		final BlockResamplingMethod resamplingMethod )
	{
		return new InternalResamplingClient(sampleCacheClient, resamplingMethod, 0, 0.0f);
	}

	@Override
	public void destroyResamplingClient( final BlockResamplingClient resamplingClient ) throws DatastoreException, RecordNotFoundException
	{
		final InternalResamplingClient brc = (InternalResamplingClient)resamplingClient;
		final SampleCacheClient scc = brc.getSampleCacheClient();
		sampleCachingService.unregisterCacheClientForFile(scc);
	}

	@Override
	public RealtimeMethodReturnCodeEnum fetchAndResample( final NTBlockResamplingClient resamplingClient,
			final int outputSampleRate,
			float playbackSpeed,
			final float[] outputLeftFloats, final float[] outputRightFloats,
			final int outputPos, final int numFramesRequired,
			final float[] tmpBuffer, final int tmpBufferOffset )
	{
		final boolean isForwards = (playbackSpeed >= 0.0f );
		log.debug("Playing " + (isForwards ? "forwards" : "backwards") + " at speed " + playbackSpeed );

		final NTInternalResamplingClient realClient = (NTInternalResamplingClient)resamplingClient;
		final SampleCacheClient scc = realClient.getSampleCacheClient();

		final int sourceSampleRate = scc.getSampleRate();
		if( sourceSampleRate != outputSampleRate )
		{
			final float speedMultiplier = sourceSampleRate / (float)outputSampleRate;
			playbackSpeed *= speedMultiplier;
			log.debug("Sample rates differ - adjusted playback speed to " + playbackSpeed );
		}

		long prevSamplePos = realClient.getFramePosition();
		float prevSampleFpOffset = realClient.getFpOffset();
		log.debug("Prev sample pos(" + prevSamplePos + ":" + prevSampleFpOffset +")");

		final long firstSamplePos;
		float firstSampleFpOffset;
		long samplesOffset;

		if( isForwards )
		{
			firstSampleFpOffset = prevSampleFpOffset + playbackSpeed;
			final int extraInt = (int)Math.floor(firstSampleFpOffset);
			firstSamplePos = prevSamplePos + extraInt;
			firstSampleFpOffset -= extraInt;
		}
		else
		{
			firstSampleFpOffset = prevSampleFpOffset + playbackSpeed;
			final int extraInt = (int)Math.floor(firstSampleFpOffset);
			firstSamplePos = prevSamplePos + extraInt;
			firstSampleFpOffset -= extraInt;
		}

		final float dx = playbackSpeed * numFramesRequired;

		log.debug("So first sample pos(" + firstSamplePos + ":" + firstSampleFpOffset +") and our delta is " + dx );

		playbackSpeed = (playbackSpeed < 0.0f ? -playbackSpeed : playbackSpeed );
		// For cubic, we use one frame before and (up to) 3 frames after (4 in total)
		// 3 because the fp offset means we can span an extra one - it's easier to
		// just always read one extra than worry about calculating the exact number
		final int numFileFramesRequired = 4 +
				(int)((numFramesRequired * playbackSpeed) + firstSampleFpOffset);

//		log.debug("We require " + numFileFramesRequired + " frames from the file");

		samplesOffset = (isForwards ?
				firstSamplePos - 1 :
				((firstSamplePos + 1) - numFileFramesRequired) );

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

		checkFloatsForMagic( "postReadSamples", tmpBuffer, fileReadBufferOffset, numFileFramesRequired * 2 );

		// De-interleave
		final int leftDeinterleaveOffset = fileReadBufferOffset + numFileFramesRequired + numFileFramesRequired + 1000;
		final int rightDeinterleaveOffset = leftDeinterleaveOffset + numFileFramesRequired + numFileFramesRequired + 1000;
		deinterleaveFetchedSamples( tmpBuffer,
				fileReadBufferOffset,
				numFileFramesRequired,
				leftDeinterleaveOffset, rightDeinterleaveOffset );
		checkFloatsForMagic( "leftdeint", tmpBuffer, leftDeinterleaveOffset, numFileFramesRequired );
		checkFloatsForMagic( "rightdeint", tmpBuffer, rightDeinterleaveOffset, numFileFramesRequired );

//		checkFloatsForLargeDis( "leftdeint", tmpBuffer, leftDeinterleaveOffset, numFileFramesRequired );
//		checkFloatsForLargeDis( "rightdeint", tmpBuffer, rightDeinterleaveOffset, numFileFramesRequired );

		// Reverse the samples if we are going backwards
		if( !isForwards )
		{
			ArrayUtils.reverse( tmpBuffer, leftDeinterleaveOffset, numFileFramesRequired );
			ArrayUtils.reverse( tmpBuffer, rightDeinterleaveOffset, numFileFramesRequired );
		}

		// Now turn these into the output by resampling
		// we need to start from the current sample and offset to make
		// the fetch work
		realClient.setFpOffset( isForwards ?
				firstSampleFpOffset :
				1.0f - firstSampleFpOffset );
		final RealtimeMethodReturnCodeEnum rsRc = realClient.resample( tmpBuffer, leftDeinterleaveOffset,
				tmpBuffer, rightDeinterleaveOffset,
				outputLeftFloats, outputPos,
				outputRightFloats, outputPos,
				playbackSpeed,
				numFramesRequired,
				numFileFramesRequired );
		if( rsRc != RealtimeMethodReturnCodeEnum.SUCCESS )
		{
			return rsRc;
		}

		checkFloatsForMagic( "leftResample", outputLeftFloats, outputPos, numFramesRequired );
		checkFloatsForMagic( "rightResample", outputRightFloats, outputPos, numFramesRequired );

		// Update our position
		prevSampleFpOffset += dx;
		final int extraInt = (int)Math.floor(prevSampleFpOffset);
		prevSamplePos += extraInt;
		prevSampleFpOffset -= extraInt;
		log.debug("Updating to new position of " + prevSamplePos + ":" + prevSampleFpOffset +")");

		realClient.setFramePosition( prevSamplePos );
		realClient.setFpOffset( prevSampleFpOffset );

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	private static void checkFloatsForLargeDis( final String srcString, final float[] buffer, final int offset,
			final int length )
	{
		float prevVal = buffer[offset];

		for( int i = 0 ; i < length ; ++i )
		{
			final float curVal = buffer[offset +i];
			final float diff = curVal - prevVal;
			if( Math.abs(diff) >= 0.2f )
			{
				log.error("Arg, major discontinuity from " + srcString );
			}
			prevVal = curVal;
		}
	}

	@Override
	public RealtimeMethodReturnCodeEnum fetchAndResampleVarispeed( final NTBlockResamplingClient resamplingClient,
			final int outputSampleRate,
			final float[] playbackSpeeds,
			final float[] outputLeftFloats, final float[] outputRightFloats,
			final int outputPos, final int numFramesRequired,
			final float[] tmpBuffer, final int tmpBufferOffset )
	{
		// Need to break it up into sections of "forward" or "backward"
		// so we can read a bunch of samples at once and work on them.

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	@Override
	public NTBlockResamplingClient createNTResamplingClient( final String pathToFile, final BlockResamplingMethod resamplingMethod )
			throws DatastoreException, IOException, UnsupportedAudioFileException
	{
		SampleCacheClient scc;
		try
		{
			scc = sampleCachingService.registerCacheClientForFile(pathToFile);
			final NTInternalResamplingClient brc = new NTInternalResamplingClient( scc,
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
	public void destroyNTResamplingClient( final NTBlockResamplingClient resamplingClient )
			throws DatastoreException, RecordNotFoundException
	{
		final NTInternalResamplingClient brc = (NTInternalResamplingClient)resamplingClient;
		final SampleCacheClient scc = brc.getSampleCacheClient();
		sampleCachingService.unregisterCacheClientForFile(scc);
	}

}
