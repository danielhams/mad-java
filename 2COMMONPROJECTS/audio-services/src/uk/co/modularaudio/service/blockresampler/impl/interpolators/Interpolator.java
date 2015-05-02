package uk.co.modularaudio.service.blockresampler.impl.interpolators;

public interface Interpolator
{
	float interpolate( float[] sourceBuffer, int pos, float frac );
}
