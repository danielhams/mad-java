package uk.co.modularaudio.mads.base.interptester.utils;

import uk.co.modularaudio.util.audio.mvc.displayslider.MixdownSliderIntToFloatConverter;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;

public class MixdownSliderModel extends SliderDisplayModel
{

	private final static MixdownSliderIntToFloatConverter MIXDOWN_INT_TO_FLOAT_CONVERTER = new MixdownSliderIntToFloatConverter();

	public MixdownSliderModel()
	{
		super( Float.NEGATIVE_INFINITY, MIXDOWN_INT_TO_FLOAT_CONVERTER.getLinearHighestDb(),
				0.0f,
				MIXDOWN_INT_TO_FLOAT_CONVERTER.getNumTotalSteps(),
				1,
				MIXDOWN_INT_TO_FLOAT_CONVERTER,
				3,
				3,
				"dB" );
	}

}
