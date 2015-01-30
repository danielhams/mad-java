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

import java.util.Arrays;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.blockresampler.BlockResamplerService;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.blockresampler.BlockResamplingMethod;
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

		final float fpNumFramesNeeded = (numFramesRequired * playbackSpeed);
		final int intNumFramesNeeded = (int)Math.ceil(fpNumFramesNeeded);

//		log.debug("Resample at frame(" + curFramePosition +") and fpoffset(" + startFpOffset +") need " + numRequired + " frames at speed " + playbackSpeed + " will read " + intNumFramesNeeded );

		// Read needed first sample for cubic interpolation and two samples at the end
		final int numFramesWithCubicSamples = intNumFramesNeeded + 3;
		final int tmpBufferFramePosForRead = 0;

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
				tmpBufferFramePosForRead,
				numFramesWithCubicSamples );

		if( retCode != RealtimeMethodReturnCodeEnum.SUCCESS )
		{
			log.debug("Reading samples has failed.");
			// Over the end of the sample - output zeros
			Arrays.fill( outputLeftFloats, 0.0f );
			Arrays.fill( outputRightFloats, 0.0f );
			return retCode;
		}

		// Split up into two streams of samples in the temporary buffer
		final int leftNonInterleavedIndex = numFramesWithCubicSamples + numFramesWithCubicSamples;
		final int rightNonInterleavedIndex = leftNonInterleavedIndex + numFramesWithCubicSamples;
		deinterleaveFetchedSamples( tmpBuffer,
				numFramesWithCubicSamples,
				leftNonInterleavedIndex,
				rightNonInterleavedIndex);

		if( !isForwards )
		{
			ArrayUtils.reverse( tmpBuffer, leftNonInterleavedIndex, numFramesWithCubicSamples );
			ArrayUtils.reverse( tmpBuffer, rightNonInterleavedIndex, numFramesWithCubicSamples );
		}

//		boolean lpnl = resampledSamplePlaybackDetails.getLowPassNyquistLimits();
//		if( lpnl )
//		{
//			// Now if (a) ((fileSampleRate/2) * speed) > (outputSampleRate/2)
//			// We have frequencies that will exceed the nyquist when resampled so lets low pass by
//			// the appropriate amount before we resample
//			// Since from (a) above we need an adjustment if
//			// (b) speed > (outputSampleRate/(2*filesampleRate)
//			// or
//			// (c) fileSampleRate > (outputSampleRate/2*speed)
//			float maxFileSampleRate = outputSampleRate / (2.0f * absSpeed );
//			// Check if this max sample rate is lower that the sound file sample rate
//			if( maxFileSampleRate < sourceSampleRate )
//			{
//				float lowPassFrequency = maxFileSampleRate / 2.0f;
////				RBJFilterRT lpf = resampledSamplePlaybackDetails.getLowPassFilter();
////				RBJFilter.filterIt( lpf,
//
//			}
//		}

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

	private void deinterleaveFetchedSamples(final float[] tmpBuffer, final int numFramesWithCubicSamples,
			final int leftNonInterleavedIndex, final int rightNonInterleavedIndex)
	{
		for( int s = 0 ; s < numFramesWithCubicSamples ; ++s )
		{
			final int readOffset = (s * 2);
			tmpBuffer[ leftNonInterleavedIndex +s ] = tmpBuffer[ readOffset ];
			tmpBuffer[ rightNonInterleavedIndex + s ] = tmpBuffer[ readOffset + 1 ];
		}
	}

	private void interpolate( final BlockResamplingMethod resamplingMethod, final float[] sourceBuffer, final int sourceIndex, final float sourceFrac,
		final float[] output, final int outputPos, final int numFramesRequired, final float playbackSpeed, final boolean haveAmps, final float[] amps )
	{
		if( outputPos + numFramesRequired > output.length )
		{
			log.error("Badly computed length for interpolate!");
		}
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
			throws DatastoreException, UnsupportedAudioFileException
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

}
