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

package uk.co.modularaudio.mads.base.interptester.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mvc.displayslider.models.CompressionOutputGainSliderModel;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.CompressionRatioSliderModel;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.CompressionThresholdSliderModel;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.DJCrossFaderSliderModel;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.DJDeckFaderSliderModel;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.DJEQGainSliderModel;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.LeftRightPanSliderModel;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.LogarithmicTimeMillisMinOneSliderModel;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.MixdownSliderModel;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.OscillatorFrequencySliderModel;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.PlaybackSpeedSliderModel;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.SAOOffsetSliderModel;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.SAOScaleSliderModel;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.SVValueSliderModel;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;

public class InterpTesterSliderModels
{
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog( InterpTesterSliderModels.class.getName() );

	private final SliderDisplayModel[] models = {
			new DJCrossFaderSliderModel(),
			new DJEQGainSliderModel(),
			new DJDeckFaderSliderModel(),
			new MixdownSliderModel(),
			new PlaybackSpeedSliderModel(),
			new OscillatorFrequencySliderModel(),
			new LeftRightPanSliderModel(),
			new CompressionThresholdSliderModel(),
			new CompressionRatioSliderModel(),
			new CompressionOutputGainSliderModel(),
			new LogarithmicTimeMillisMinOneSliderModel(),
			new SAOScaleSliderModel(),
			new SAOOffsetSliderModel(),
			new SVValueSliderModel()
	};

	private final SliderModelValueConverter[] valueConverters =	{
			null,
			new DbToLevelValueConverter(),
			new DbToLevelValueConverter(),
			new DbToLevelValueConverter(),
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null
	};

	public InterpTesterSliderModels()
	{
	}

	public SliderDisplayModel getModelAt( final int index )
	{
		return models[index];
	}

	public SliderModelValueConverter getValueConverterAt( final int index )
	{
		return valueConverters[index];
	}
}
