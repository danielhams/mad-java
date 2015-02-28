package uk.co.modularaudio.mads.base.interptester.utils;

import uk.co.modularaudio.util.audio.math.AudioMath;

public class DbToLevelValueConverter implements SliderModelValueConverter
{
	@Override
	public float convertValue( final float sliderValue )
	{
		final float convertedValue = AudioMath.dbToLevelF( sliderValue );
		return convertedValue;
	}

}
