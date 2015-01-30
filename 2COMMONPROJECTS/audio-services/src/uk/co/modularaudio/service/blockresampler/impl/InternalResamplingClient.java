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

import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.blockresampler.BlockResamplingMethod;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;

class InternalResamplingClient implements BlockResamplingClient
{
	private final SampleCacheClient sampleCacheClient;
	private final BlockResamplingMethod resamplingMethod;
	private long framePosition;
	private float fpOffset;
	private final long totalNumFrames;

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
}
