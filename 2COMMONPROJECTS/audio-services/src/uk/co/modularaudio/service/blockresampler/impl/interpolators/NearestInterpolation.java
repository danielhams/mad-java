package uk.co.modularaudio.service.blockresampler.impl.interpolators;


public class NearestInterpolation implements Interpolator
{
	@Override
	public final float interpolate( final float[] sourceBuffer, final int pos, final float frac )
	{
		return sourceBuffer[pos];
	}
}
