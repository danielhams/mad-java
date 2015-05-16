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

package uk.co.modularaudio.service.audioanalysis;

import java.util.Date;

public class AnalysedData
{
	private int analysisId;
	private Date creationTimestamp;
	private int libraryEntryId;

	// Beat related data
	private float bpm;

	// Gain related data
	private float absPeakDb;
	private float rmsAverageDb;
	private float rmsPeakDb;

	// Static overview thumbnail
	private String pathToStaticThumbnail;

	public AnalysedData()
	{
	}

	public AnalysedData( final int analysisId,
			final int libraryId,
			final float detectedBpm,
			final float absPeakDb,
			final float rmsAverageDb,
			final float rmsPeakDb,
			final String pathToStaticThumbnail )
	{
		super();
		this.analysisId = analysisId;
		this.libraryEntryId = libraryId;
		this.bpm = detectedBpm;
		this.absPeakDb = absPeakDb;
		this.rmsPeakDb = rmsPeakDb;
		this.rmsAverageDb = rmsAverageDb;
		this.pathToStaticThumbnail = pathToStaticThumbnail;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append( "\nBpm: " + bpm );
		sb.append( "\nAbs Peak dB: " + absPeakDb );
		sb.append( "\nRMS Average dB: " + rmsAverageDb );
		sb.append( "\nRMS Peak dB: " + rmsPeakDb );
		sb.append( "\nStaticThumbnail Path: " + pathToStaticThumbnail );
		return sb.toString();
	}

	public int getAnalysisId()
	{
		return analysisId;
	}

	public void setAnalysisId( final int analysisId )
	{
		this.analysisId = analysisId;
	}

	public Date getCreationTimestamp()
	{
		return creationTimestamp;
	}

	public void setCreationTimestamp( final Date creationTimestamp )
	{
		this.creationTimestamp = creationTimestamp;
	}

	public int getLibraryEntryId()
	{
		return libraryEntryId;
	}

	public void setLibraryEntryId( final int libraryEntryId )
	{
		this.libraryEntryId = libraryEntryId;
	}

	public float getBpm()
	{
		return bpm;
	}

	public void setBpm( final float detectedBpm )
	{
		this.bpm = detectedBpm;
	}

	public String getPathToStaticThumbnail()
	{
		return pathToStaticThumbnail;
	}

	public void setPathToStaticThumbnail( final String pathToStaticThumbnail )
	{
		this.pathToStaticThumbnail = pathToStaticThumbnail;
	}

	public float getRmsAverageDb()
	{
		return rmsAverageDb;
	}

	public float getRmsPeakDb()
	{
		return rmsPeakDb;
	}

	public void setRmsAverageDb( final float detectedRmsAverageDb )
	{
		this.rmsAverageDb = detectedRmsAverageDb;
	}

	public void setRmsPeakDb( final float detectedRmsPeakDb )
	{
		this.rmsPeakDb = detectedRmsPeakDb;
	}

	public float getAbsPeakDb()
	{
		return absPeakDb;
	}

	public void setAbsPeakDb( final float absPeakDb )
	{
		this.absPeakDb = absPeakDb;
	}
}
