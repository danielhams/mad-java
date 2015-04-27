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

package uk.co.modularaudio.service.library;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LibraryEntry
{
	private int libraryEntryId;
	private Date creationTimestamp;
	private List<CuePoint> cuePoints;
	private int numChannels;
	private int sampleRate;
	private long totalNumFrames;
	private long totalNumFloats;
	private String title;
	private String format;
	private String location;

	public LibraryEntry()
	{
		this( -1, new ArrayList<CuePoint>(), 0, 0, 0, "", "", "");
	}

	public LibraryEntry( final int libraryEntryId,
			final List<CuePoint> cuePoints,
			final int numChannels,
			final int sampleRate,
			final long totalNumFrames,
			final String title,
			final String format,
			final String location )
	{
		this.libraryEntryId = libraryEntryId;
		this.cuePoints = cuePoints;
		this.numChannels = numChannels;
		this.sampleRate = sampleRate;
		this.totalNumFrames = totalNumFrames;
		this.totalNumFloats = totalNumFrames * numChannels;
		this.title = title;
		this.format = format;
		this.location = location;
	}

	public void setLibraryEntryId( final int libraryEntryId )
	{
		this.libraryEntryId = libraryEntryId;
	}

	public Date getCreationTimestamp()
	{
		return creationTimestamp;
	}

	public void setCreationTimestamp( final Date creationTimestamp )
	{
		this.creationTimestamp = creationTimestamp;
	}

	public void setTitle( final String newTitle )
	{
		this.title = newTitle;
	}

	public void setLocation( final String newLocation )
	{
		this.location = newLocation;
	}

	public void setNumChannels( final int numChannels )
	{
		this.numChannels = numChannels;
	}

	public void setTotalNumFrames( final long totalNumFrames )
	{
		this.totalNumFrames = totalNumFrames;
	}

	public void setTotalNumFloats( final long totalNumFloats )
	{
		this.totalNumFloats = totalNumFloats;
	}

	public void setCuePoints( final List<CuePoint> cuePoints )
	{
		this.cuePoints = cuePoints;
	}

	public int getLibraryEntryId()
	{
		return libraryEntryId;
	}

	public List<CuePoint> getCuePoints()
	{
		return cuePoints;
	}

	public int getNumChannels()
	{
		return numChannels;
	}

	public long getTotalNumFrames()
	{
		return totalNumFrames;
	}

	public long getTotalNumFloats()
	{
		return totalNumFloats;
	}

	public String getTitle()
	{
		return title;
	}

	public String getLocation()
	{
		return location;
	}

	public void setSampleRate( final int sampleRate )
	{
		this.sampleRate = sampleRate;
	}

	public int getSampleRate()
	{
		return sampleRate;
	}

	public String getFormat()
	{
		return format;
	}

	public void setFormat( final String format )
	{
		this.format = format;
	}

}
