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

package uk.co.modularaudio.service.audioanalysis.impl.analysers;

import uk.co.modularaudio.service.audioanalysis.AnalysedData;
import uk.co.modularaudio.service.audioanalysis.impl.AnalysisContext;
import uk.co.modularaudio.service.audioanalysis.impl.AudioAnalyser;
import uk.co.modularaudio.service.hashedstorage.HashedRef;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.math.AudioMath;

public class GainAnalyser implements AudioAnalyser
{
//	private static Log log = LogFactory.getLog( GainAnalyser.class.getName() );

	@Override
	public void dataStart( final DataRate dataRate, final int numChannels, final long totalFrames )
	{
	}

	@Override
	public void receiveFrames( final float[] data, final int numFrames )
	{
	}

	@Override
	public void dataEnd( final AnalysisContext context, final AnalysedData analysedData, final HashedRef hashedRef )
	{
		final StaticThumbnailAnalyser thumbnailAnalyser = context.getThumbnailAnalyser();
		final float maxRmsValue = thumbnailAnalyser.getMaxRmsValue();
		final float averageRmsValue = thumbnailAnalyser.getAverageRmsValue();

		final float maxRmsDbValue = AudioMath.levelToDbF( maxRmsValue );
		final float averageRmsDbValue = AudioMath.levelToDbF( averageRmsValue );

		final float absPeakDb = AudioMath.levelToDbF( thumbnailAnalyser.getAbsPeakValue() );

		analysedData.setAbsPeakDb( absPeakDb );
		analysedData.setRmsPeakDb( maxRmsDbValue );
		analysedData.setRmsAverageDb( averageRmsDbValue );
	}

	@Override
	public void completeAnalysis( final AnalysisContext context, final AnalysedData analysedData, final HashedRef hashedRef )
	{
	}

}
