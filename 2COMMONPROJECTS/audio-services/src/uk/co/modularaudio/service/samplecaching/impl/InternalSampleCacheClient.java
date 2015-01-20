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

package uk.co.modularaudio.service.samplecaching.impl;

import java.util.concurrent.atomic.AtomicLong;

import uk.co.modularaudio.service.library.vos.LibraryEntry;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;

public class InternalSampleCacheClient implements SampleCacheClient
{
//	private static Log log = LogFactory.getLog( InternalSampleCacheClient.class.getName() );
	
	private LibraryEntry libraryEntry = null;
	private AtomicLong currentFramePosition = new AtomicLong(0);
	private AtomicLong intendedFramePosition = new AtomicLong(0);

	private int lastReadBlockNumber = 0;

	public InternalSampleCacheClient( LibraryEntry libraryEntry, long currentFramePosition, long intendedFramePosition )
	{
		this.libraryEntry = libraryEntry;
		this.currentFramePosition.set( currentFramePosition );
		this.intendedFramePosition.set( intendedFramePosition );
	}

	public LibraryEntry getLibraryEntry()
	{
		return libraryEntry;
	}
	
	public long getCurrentFramePosition()
	{
		return currentFramePosition.get();
	}

	@Override
	public void setCurrentFramePosition( long newFramePosition )
	{
		currentFramePosition.set( newFramePosition );
	}
	
	public long getIntendedFramePosition()
	{
		return intendedFramePosition.get();
	}
	
	@Override
	public void setIntendedFramePosition( long newIntendedPosition )
	{
		intendedFramePosition.set( newIntendedPosition );
	}

	@Override
	public long getTotalNumFrames()
	{
		return libraryEntry.getTotalNumFrames();
	}

	public int getNumChannels()
	{
		return libraryEntry.getNumChannels();
	}

	public int getLastReadBlockNumber()
	{
		return lastReadBlockNumber;
	}
	
	public void setLastReadBlockNumber( int newLastReadBlockNumber )
	{
		lastReadBlockNumber = newLastReadBlockNumber;
	}
	
	public int getSampleRate()
	{
		return libraryEntry.getSampleRate();
	}
}
