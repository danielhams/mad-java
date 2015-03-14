package uk.co.modularaudio.mads.base.interptester.utils;

import uk.co.modularaudio.util.mvc.displayslider.LogSliderIntToFloatConverter;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;

public class LogarithmicTimeMillisSliderModel extends SliderDisplayModel
{
	public LogarithmicTimeMillisSliderModel()
	{
		super( 1.0f, 5000.0f, 60.0f,
				4999,
				100,
				new LogSliderIntToFloatConverter( 5000.0f ),
				5,
				3,
				"ms" );
	}
}
