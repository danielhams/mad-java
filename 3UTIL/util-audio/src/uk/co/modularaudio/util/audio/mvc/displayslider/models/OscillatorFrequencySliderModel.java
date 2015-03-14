package uk.co.modularaudio.util.audio.mvc.displayslider.models;

import uk.co.modularaudio.util.mvc.displayslider.LogSliderIntToFloatConverter;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;

public class OscillatorFrequencySliderModel extends SliderDisplayModel
{
	public OscillatorFrequencySliderModel()
	{
		super( 40.0f, 22050.0f, 440.0f,
				1000,
				100,
				new LogSliderIntToFloatConverter( 22050.0f ),
				5,
				3,
				"hz" );
	}
}
