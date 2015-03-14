package uk.co.modularaudio.mads.base.interptester.utils;

import uk.co.modularaudio.util.mvc.displayslider.SimpleSliderIntToFloatConverter;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;

public class CompressionThresholdSliderModel extends SliderDisplayModel
{

	public CompressionThresholdSliderModel()
	{
		super(  -36.0f, 0.0f, 0.0f,
				3600,
				100,
				new SimpleSliderIntToFloatConverter(),
				3,
				3,
				"dB" );
	}
}
