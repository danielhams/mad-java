package uk.co.modularaudio.service.blockresampler.impl.interpolators;



public class LinearInterpolator implements Interpolator
{
//	private static Log log = LogFactory.getLog( LinearInterpolator.class.getName() );

	@Override
	public final float interpolate( final float[] sourceBuffer, final int pos, final float frac )
	{
		final float y0 = sourceBuffer[pos];
		final float y1 = sourceBuffer[pos+1];
//		if( Math.abs(y0) > BlockResamplerService.EXCESSIVE_FLOAT ||
//				Math.abs(y1) > BlockResamplerService.EXCESSIVE_FLOAT )
//		{
//			log.error("Failed on frame " + pos + " with vals " + y0 +
//					" " + y1 );
//		}
		return ((1.0f - frac) * y0) + (frac * y1);
	}
}
