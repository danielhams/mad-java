package uk.co.modularaudio.util.audio.mvc.displayslider.models;

import uk.co.modularaudio.util.mvc.displayslider.SimpleSliderIntToFloatConverter;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;

public class CompressionOutputGainSliderModel extends SliderDisplayModel
{
	public CompressionOutputGainSliderModel()
	{
		super(  -12.0f, 12.0f, 0.0f,
				1000,
				100,
				new SimpleSliderIntToFloatConverter(),
				3,
				3,
				"dB" );
	}
}
