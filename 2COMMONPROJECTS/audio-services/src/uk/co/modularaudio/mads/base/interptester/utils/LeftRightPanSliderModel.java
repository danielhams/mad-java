package uk.co.modularaudio.mads.base.interptester.utils;

import uk.co.modularaudio.util.mvc.displayslider.SimpleSliderIntToFloatConverter;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;

public class LeftRightPanSliderModel extends SliderDisplayModel
{
	public LeftRightPanSliderModel()
	{
		super(  -1.0f, 1.0f, 0.0f,
				2000,
				100,
				new SimpleSliderIntToFloatConverter(),
				3,
				3,
				"val" );
	}
}
