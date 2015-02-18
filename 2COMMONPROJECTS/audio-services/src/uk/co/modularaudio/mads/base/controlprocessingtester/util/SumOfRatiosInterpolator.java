package uk.co.modularaudio.mads.base.controlprocessingtester.util;

import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;

public class SumOfRatiosInterpolator implements ControlValueInterpolator
{

	private float curValueRatio = 0.5f;
	private float newValueRatio = 0.5f;

	private float curVal;
	private float desVal;

	public SumOfRatiosInterpolator()
	{
	}

	public void reset( final long sampleRate, final float valueChaseMillis )
	{
		newValueRatio = AudioTimingUtils.calculateNewValueRatioHandwaveyVersion( sampleRate, valueChaseMillis );
		curValueRatio = 1.0f - newValueRatio;
	}


	@Override
	public void generateControlValues( final float[] output, final int outputIndex, final int length )
	{
		final int lastIndex = outputIndex + length;
		for( int i = outputIndex ; i < lastIndex ; ++i )
		{
			curVal = (curVal * curValueRatio) + (desVal * newValueRatio);
			output[i] = curVal;
		}
	}

	@Override
	public void notifyOfNewIncomingAmp( final float amp )
	{
		this.desVal = amp;
	}

	@Override
	public void checkForDenormal()
	{
		if( curVal > -AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F && curVal < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
		{
			curVal = 0.0f;
		}
	}
}
