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

package uk.co.modularaudio.service.audioanalysis.impl;

import uk.co.modularaudio.service.audioanalysis.AnalysedData;
import uk.co.modularaudio.service.audioanalysis.impl.analysers.GainAnalyser;
import uk.co.modularaudio.service.audioanalysis.impl.analysers.StaticThumbnailAnalyser;
import uk.co.modularaudio.service.hashedstorage.HashedRef;
import uk.co.modularaudio.util.audio.format.DataRate;

public class AnalysisContext
{
	private final GainAnalyser gainAnalyser;
	private final StaticThumbnailAnalyser thumbnailAnalyser;

	public AnalysisContext( final GainAnalyser gainAnalyser,
			final StaticThumbnailAnalyser thumbnailAnalyser )
	{
		this.gainAnalyser = gainAnalyser;
		this.thumbnailAnalyser = thumbnailAnalyser;
	}

	public GainAnalyser getGainAnalyser()
	{
		return gainAnalyser;
	}

	public StaticThumbnailAnalyser getThumbnailAnalyser()
	{
		return thumbnailAnalyser;
	}

	public void dataStart( final DataRate dataRate, final int numChannels, final long totalFrames )
	{
		thumbnailAnalyser.dataStart( dataRate, numChannels, totalFrames );
		gainAnalyser.dataStart( dataRate, numChannels, totalFrames );
	}

	public void receiveFrames( final float[] data, final int numFrames )
	{
		thumbnailAnalyser.receiveFrames( data, numFrames );
		gainAnalyser.receiveFrames( data, numFrames );
	}

	public void dataEnd( final AnalysedData analysedData, final HashedRef hashedRef )
	{
		thumbnailAnalyser.dataEnd( this, analysedData, hashedRef );
		gainAnalyser.dataEnd( this, analysedData, hashedRef );
	}

	public void completeAnalysis( final AnalysedData analysedData, final HashedRef hashedRef )
	{
		thumbnailAnalyser.completeAnalysis( this, analysedData, hashedRef );
		gainAnalyser.completeAnalysis( this, analysedData, hashedRef );
	}
}
