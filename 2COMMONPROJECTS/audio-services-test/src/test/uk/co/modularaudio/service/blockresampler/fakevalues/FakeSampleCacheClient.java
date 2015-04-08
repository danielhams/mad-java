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

package test.uk.co.modularaudio.service.blockresampler.fakevalues;

import uk.co.modularaudio.service.library.LibraryEntry;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;

public class FakeSampleCacheClient implements SampleCacheClient
{
	private long currentFramePosition = 0;
	private long intendedFramePosition = 0;

	public FakeSampleCacheClient()
	{
	}

	public long getCurrentFramePosition()
	{
		return currentFramePosition;
	}

	@Override
	public void setCurrentFramePosition(final long newFramePosition)
	{
		currentFramePosition = newFramePosition;
	}

	@Override
	public void setIntendedFramePosition(final long newIntendedPosition)
	{
		intendedFramePosition = newIntendedPosition;
	}

	public long getIntendedFramePosition()
	{
		return intendedFramePosition;
	}

	@Override
	public long getTotalNumFrames()
	{
		return 320;
	}

	@Override
	public int getNumChannels()
	{
		return 2;
	}

	@Override
	public int getSampleRate()
	{
		return 44100;
	}

	@Override
	public LibraryEntry getLibraryEntry()
	{
		return null;
	}

}
