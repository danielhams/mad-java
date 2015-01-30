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

public class AnalysedData
{
	// Beat related data
	private float detectedBpm = 0.0f;
	private long[] detectedBeatPositions = new long[0];
	
	// Gain related data
	private float detectedPeak = 0.0f;
	private float autoGainAdjustment = 0.0f;
	
	// Scrolling thumbnails
	private String pathToZiScrollingThumbnail = null;
	private String pathToZoScrollingThumbnail = null;
	
	// Static thumbnail
	private String pathToStaticThumbnail = null;
	
	public AnalysedData()
	{
	}

	public float getDetectedBpm()
	{
		return detectedBpm;
	}

	public long[] getDetectedBeatPositions()
	{
		return detectedBeatPositions;
	}

	public float getDetectedPeak()
	{
		return detectedPeak;
	}

	public float getAutoGainAdjustment()
	{
		return autoGainAdjustment;
	}

	public String getPathToZiScrollingThumbnail()
	{
		return pathToZiScrollingThumbnail;
	}

	public String getPathToZoScrollingThumbnail()
	{
		return pathToZoScrollingThumbnail;
	}

	public String getPathToStaticThumbnail()
	{
		return pathToStaticThumbnail;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( "\nDetected Bpm: " + detectedBpm );
		sb.append( "\nDetected Peak: " + detectedPeak );
		sb.append( "\nAuto Gain Adjustment: " + autoGainAdjustment );
		sb.append( "\nZiScrollingThumbnail Path: " + pathToZiScrollingThumbnail );
		sb.append( "\nZoScrollingThumbnail Path: " + pathToZoScrollingThumbnail );
		sb.append( "\nStaticThumbnail Path: " + pathToStaticThumbnail );
		return sb.toString();
	}

	public void setDetectedBpm(float detectedBpm)
	{
		this.detectedBpm = detectedBpm;
	}

	public void setDetectedBeatPositions(long[] detectedBeatPositions)
	{
		this.detectedBeatPositions = detectedBeatPositions;
	}

	public void setDetectedPeak(float detectedPeak)
	{
		this.detectedPeak = detectedPeak;
	}

	public void setAutoGainAdjustment(float autoGainAdjustment)
	{
		this.autoGainAdjustment = autoGainAdjustment;
	}

	public void setPathToZiScrollingThumbnail(String pathToZiScrollingThumbnail)
	{
		this.pathToZiScrollingThumbnail = pathToZiScrollingThumbnail;
	}

	public void setPathToZoScrollingThumbnail(String pathToZoScrollingThumbnail)
	{
		this.pathToZoScrollingThumbnail = pathToZoScrollingThumbnail;
	}

	public void setPathToStaticThumbnail(String pathToStaticThumbnail)
	{
		this.pathToStaticThumbnail = pathToStaticThumbnail;
	}
}
