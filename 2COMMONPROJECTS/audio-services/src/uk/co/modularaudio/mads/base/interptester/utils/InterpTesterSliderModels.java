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

import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;

public class InterpTesterSliderModels
{
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog( InterpTesterSliderModels.class.getName() );

	private final SliderDisplayModel[] models = new SliderDisplayModel[9];
	private final SliderModelValueConverter[] valueConverters = new SliderModelValueConverter[9];
	{
	};

	public InterpTesterSliderModels()
	{
		// Cross fader
		models[0] = new CrossFaderSliderModel();
		valueConverters[0] = null;

		// Mixer fader and converter
		models[1] = new MixdownSliderModel();
		valueConverters[1] = new DbToLevelValueConverter();

		// Speed
		models[2] = new PlaybackSpeedSliderModel();
		valueConverters[2] = null;

		// Frequency
		models[3] = new OscillatorFrequencySliderModel();
		valueConverters[3] = null;

		// Left Right
		models[4] = new LeftRightPanSliderModel();
		valueConverters[4] = null;

		// Compression Threshold
		models[5] = new CompressionThresholdSliderModel();
		valueConverters[5] = null;

		// Compression Ratio
		models[6] = new CompressionRatioSliderModel();
		valueConverters[6] = null;

		// Compression Output Gain
		models[7] = new CompressionOutputGainSliderModel();
		valueConverters[7] = null;

		// Time (1->5000 ms)
		models[8] = new LogarithmicTimeMillisSliderModel();
		valueConverters[8] = null;
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
