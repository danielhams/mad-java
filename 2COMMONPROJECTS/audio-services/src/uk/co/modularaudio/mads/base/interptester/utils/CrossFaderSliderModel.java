package uk.co.modularaudio.mads.base.interptester.utils;

import uk.co.modularaudio.util.mvc.displayslider.SimpleSliderIntToFloatConverter;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;

public class CrossFaderSliderModel extends SliderDisplayModel
{
	public CrossFaderSliderModel()
	{
		super( -1.0f, 1.0f, 0.0f,
				128,
				1,
				new SimpleSliderIntToFloatConverter(),
				3,
				3,
				"val");
	}

}
