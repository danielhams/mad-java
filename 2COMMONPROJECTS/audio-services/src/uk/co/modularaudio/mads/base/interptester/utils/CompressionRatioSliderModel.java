package uk.co.modularaudio.mads.base.interptester.utils;

import uk.co.modularaudio.util.mvc.displayslider.SimpleSliderIntToFloatConverter;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;

public class CompressionRatioSliderModel extends SliderDisplayModel
{
	public CompressionRatioSliderModel()
	{
		super(  1.0f, 20.0f, 2.0f,
				1900,
				100,
				new SimpleSliderIntToFloatConverter(),
				3,
				3,
				"val" );
	}
}
