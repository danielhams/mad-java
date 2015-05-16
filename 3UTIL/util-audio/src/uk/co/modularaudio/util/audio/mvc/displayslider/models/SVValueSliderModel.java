package uk.co.modularaudio.util.audio.mvc.displayslider.models;

import uk.co.modularaudio.util.mvc.displayslider.SimpleSliderIntToFloatConverter;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;

public class SVValueSliderModel extends SliderDisplayModel
{

	public SVValueSliderModel()
	{
		super( -500.0f, 500.0f,
				1.0f, 1.0f,
				1000,
				100,
				new SimpleSliderIntToFloatConverter(),
				4,
				2,
				"" );
	}

}
