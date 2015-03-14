package uk.co.modularaudio.mads.base.interptester.utils;

import uk.co.modularaudio.util.mvc.displayslider.SimpleSliderIntToFloatConverter;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;

public class PlaybackSpeedSliderModel extends SliderDisplayModel
{

	public PlaybackSpeedSliderModel()
	{
		super(  -1.5f, 1.5f, 1.0f,
				300,
				100,
				new SimpleSliderIntToFloatConverter(),
				3,
				3,
				"val" );
	}
}
