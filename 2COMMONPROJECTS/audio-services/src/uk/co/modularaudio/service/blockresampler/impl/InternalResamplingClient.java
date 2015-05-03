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

import uk.co.modularaudio.service.blockresampler.BlockResamplingMethod;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.blockresampler.impl.interpolators.CubicInterpolator;
import uk.co.modularaudio.service.blockresampler.impl.interpolators.LinearInterpolator;
import uk.co.modularaudio.service.blockresampler.impl.interpolators.NearestInterpolation;
import uk.co.modularaudio.service.blockresampler.impl.interpolators.Interpolator;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

class InternalResamplingClient implements BlockResamplingClient
{
	private final SampleCacheClient sampleCacheClient;
	private final BlockResamplingMethod resamplingMethod;
	private long framePosition;
	private float fpOffset;
	private final long totalNumFrames;

	private Interpolator resampler;

	public InternalResamplingClient( final SampleCacheClient sampleCacheClient,
			final BlockResamplingMethod resamplingMethod,
			final long framePosition,
			final float fpOffset )
	{
		this.sampleCacheClient = sampleCacheClient;
		this.resamplingMethod = resamplingMethod;
		this.framePosition = framePosition;
		this.fpOffset = fpOffset;
		totalNumFrames = sampleCacheClient.getTotalNumFrames();

		switch( resamplingMethod )
		{
			case NEAREST:
			{
				resampler = new NearestInterpolation();
				break;
			}
			case LINEAR:
			{
				resampler = new LinearInterpolator();
				break;
			}
			case CUBIC:
			default:
			{
				resampler = new CubicInterpolator();
			}
		}
	}

	@Override
	public SampleCacheClient getSampleCacheClient()
	{
		return sampleCacheClient;
	}

	public BlockResamplingMethod getResamplingMethod()
	{
		return resamplingMethod;
	}

	@Override
	public long getFramePosition()
	{
		return framePosition;
	}

	@Override
	public float getFpOffset()
	{
		return fpOffset;
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.projects.pac.service.blockresampler.BlockResamplingClient#setFramePosition(long)
	 */
	@Override
	public void setFramePosition( final long newPosition )
	{
		this.framePosition = newPosition;
		sampleCacheClient.setCurrentFramePosition( newPosition );
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.projects.pac.service.blockresampler.BlockResamplingClient#setFpOffset(float)
	 */
	@Override
	public void setFpOffset( final float newFpOffset )
	{
		this.fpOffset = newFpOffset;
	}

	@Override
	public long getTotalNumFrames()
	{
		return totalNumFrames;
	}

	public RealtimeMethodReturnCodeEnum resample(
			final float[] leftSourceBuffer, final int leftSourceOffset,
			final float[] rightSourceBuffer, final int rightSourceOffset,
			final float[] leftOutputBuffer, final int leftOutputOffset,
			final float[] rightOutputBuffer, final int rightOutputOffset,
			final float resampledSpeed, final int numFramesRequired,
			final int numFramesInSourceBuffers )
	{
		int curPos = 1;
		float localFpPos = fpOffset;

		for( int s = 0 ; s < numFramesRequired ; ++s )
		{
			leftOutputBuffer[leftOutputOffset+s] = resampler.interpolate(
					leftSourceBuffer,
					leftSourceOffset + curPos,
					localFpPos );

			rightOutputBuffer[rightOutputOffset+s] = resampler.interpolate(
					rightSourceBuffer,
					rightSourceOffset + curPos,
					localFpPos );

			localFpPos += resampledSpeed;
			final int extraInt = (int)localFpPos;
			curPos += extraInt;
			localFpPos -= extraInt;

		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public RealtimeMethodReturnCodeEnum resampleVarispeed(
			final float[] leftSourceBuffer, final int leftSourceOffset,
			final float[] rightSourceBuffer, final int rightSourceOffset,
			final float[] leftOutputBuffer, final int leftOutputOffset,
			final float[] rightOutputBuffer, final int rightOutputOffset,
			final float[] resampledSpeeds, final int speedsOffset, final int speedsMultiplier,
			final int numFramesRequired,
			final int numFramesInSourceBuffers )
	{
		int curPos = 1;
		float localFpPos = fpOffset;

		for( int s = 0 ; s < numFramesRequired ; ++s )
		{
			leftOutputBuffer[leftOutputOffset+s] = resampler.interpolate(
					leftSourceBuffer,
					leftSourceOffset + curPos,
					localFpPos );

			rightOutputBuffer[rightOutputOffset+s] = resampler.interpolate(
					rightSourceBuffer,
					rightSourceOffset + curPos,
					localFpPos );

			localFpPos += (resampledSpeeds[speedsOffset+s] * speedsMultiplier);
			final int extraInt = (int)localFpPos;
			curPos += extraInt;
			localFpPos -= extraInt;

		}

		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}
}
