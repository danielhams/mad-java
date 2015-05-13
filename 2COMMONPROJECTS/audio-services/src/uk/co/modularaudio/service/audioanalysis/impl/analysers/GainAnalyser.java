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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.audioanalysis.AnalysedData;
import uk.co.modularaudio.service.audioanalysis.impl.AnalysisContext;
import uk.co.modularaudio.service.audioanalysis.impl.AudioAnalyser;
import uk.co.modularaudio.service.hashedstorage.HashedRef;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.math.MathFormatter;

public class GainAnalyser implements AudioAnalyser
{
	private static Log log = LogFactory.getLog( GainAnalyser.class.getName() );

	private final static float REQUIRED_REPLAY_GAIN = -12.0f;

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

		final float maxRmsDbValue = AudioMath.levelToDbF( maxRmsValue );

		final float deltaDb = REQUIRED_REPLAY_GAIN - maxRmsDbValue;

		final float deltaAbs = AudioMath.dbToLevelF( deltaDb );

		log.debug("The max rms value(" + MathFormatter.slowFloatPrint( maxRmsValue, 5, true ) +
				") and max rms Db(" + MathFormatter.slowFloatPrint( maxRmsDbValue, 5, true ) +
				") thus delta Db(" + MathFormatter.slowFloatPrint( deltaDb, 5, true ) +
				") which abs(" + MathFormatter.slowFloatPrint( deltaAbs, 5, true ) + ")");

		final float traktorDb = maxRmsDbValue + 0.5f;

		log.debug("At a guess Traktor says (" + MathFormatter.slowFloatPrint( traktorDb, 5, true ) + ")");


		analysedData.setDetectedPeak( maxRmsDbValue );
		analysedData.setAutoGainAdjustment( deltaDb );
	}

	@Override
	public void completeAnalysis( final AnalysisContext context, final AnalysedData analysedData, final HashedRef hashedRef )
	{
	}

}
