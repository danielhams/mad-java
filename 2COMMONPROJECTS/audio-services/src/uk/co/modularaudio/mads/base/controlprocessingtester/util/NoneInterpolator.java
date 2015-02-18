package uk.co.modularaudio.mads.base.controlprocessingtester.util;

public class NoneInterpolator implements ControlValueInterpolator
{

	private float curVal;
	private float desVal;

	public NoneInterpolator()
	{
	}

	public void reset()
	{
	}


	@Override
	public void generateControlValues( final float[] output, final int outputIndex, final int length )
	{
		curVal = desVal;

		final int lastIndex = outputIndex + length;
		for( int i = outputIndex ; i < lastIndex ; ++i )
		{
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
	}
}
