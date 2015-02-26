package uk.co.modularaudio.mads.base.interptester.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.mvc.displayslider.SimpleSliderIntToFloatConverter;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;

public class InterpTesterSliderModels
{
	private static Log log = LogFactory.getLog( InterpTesterSliderModels.class.getName() );

	private final SliderDisplayModel[] models = new SliderDisplayModel[9];

	public InterpTesterSliderModels()
	{
//		 float minValue,
//			float maxValue,
//			float initialValue,
//			int numSliderSteps,
//			int sliderMajorTickSpacing,
//			SliderIntToFloatConverter sliderIntToFloatConverter,
//			int displayNumSigPlaces,
//			int displayNumDecPlaces,
//			String displayUnitsStr

//		cbm.addElement( "Cross Fader" );
//		cbm.addElement( "Mixer" );
//		cbm.addElement( "Speed" );
//		cbm.addElement( "Frequency" );
//		cbm.addElement( "Left Right" );
//		cbm.addElement( "Compression Threshold" );
//		cbm.addElement( "Compression Ratio" );
//		cbm.addElement( "Output Gain" );
//		cbm.addElement( "Time (1->5000 ms)" );

		// Cross fader
		models[0] = new SliderDisplayModel( -1.0f, 1.0f, 0.0f,
				1000,
				100,
				new SimpleSliderIntToFloatConverter(),
				3,
				3,
				"val");

		// Mixer fader
//		MixdownSliderDbToLevelComputer dbToLevelComputer = new MixdownSliderDbToLevelComputer( numTotalSteps )
		models[1] = new SliderDisplayModel( 0.0f, 5.0f, 1.0f,
				1000,
				100,
				new SimpleSliderIntToFloatConverter(),
				3,
				3,
				"val");

		// Speed
		models[2] = new SliderDisplayModel( -1.5f, 1.5f, 1.0f,
				3000,
				100,
				new SimpleSliderIntToFloatConverter(),
				3,
				3,
				"val");

		// Frequency
		models[3] = new SliderDisplayModel( 0.0f, 22000.0f, 400.0f,
				22000,
				100,
				new SimpleSliderIntToFloatConverter(),
				5,
				3,
				"val");

		// Left Right
		models[4] = new SliderDisplayModel( -1.0f, 1.0f, 0.0f,
				2000,
				100,
				new SimpleSliderIntToFloatConverter(),
				3,
				3,
				"val");
		// Compression Threshold
		models[5] = new SliderDisplayModel( -36.0f, 0.0f, 0.0f,
				3600,
				100,
				new SimpleSliderIntToFloatConverter(),
				3,
				3,
				"val");
		// Compression Ratio
		models[6] = new SliderDisplayModel( 1.0f, 20.0f, 2.0f,
				1900,
				100,
				new SimpleSliderIntToFloatConverter(),
				3,
				3,
				"val");
		// Compression Output Gain
		models[7] = new SliderDisplayModel( -12.0f, 12.0f, 0.0f,
				1000,
				100,
				new SimpleSliderIntToFloatConverter(),
				3,
				3,
				"val");
		// Time (1->5000 ms)
		models[8] = new SliderDisplayModel( 1.0f, 5000.0f, 60.0f,
				49990,
				100,
				new SimpleSliderIntToFloatConverter(),
				3,
				3,
				"val");

	}

	public SliderDisplayModel getModelAt( final int index )
	{
		return models[index];
	}
}
