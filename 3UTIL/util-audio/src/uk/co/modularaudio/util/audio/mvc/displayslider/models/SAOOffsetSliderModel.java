package uk.co.modularaudio.util.audio.mvc.displayslider.models;

import uk.co.modularaudio.util.mvc.displayslider.SimpleSliderIntToFloatConverter;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;

public class SAOOffsetSliderModel extends SliderDisplayModel
{

	public SAOOffsetSliderModel()
	{
		super( -500.0f, 500.0f,
				0.0f, 0.0f,
				1000,
				100,
				new SimpleSliderIntToFloatConverter(),
				4,
				2,
				"" );
	}

}
