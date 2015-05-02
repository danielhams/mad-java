package uk.co.modularaudio.service.blockresampler.impl.interpolators;

public class InterpolationHelper
{

	public static int getNumSourceSamplesForVarispeed( final float curFpOffset,
			final float[] replaySpeeds,
			final int numFramesRequired )
	{
		return 0;
	}

	public static int getNumSourceSamplesForSpeed( final float curFpOffset,
			final float replaySpeed,
			final int numFramesRequired )
	{
		if( replaySpeed > 0.0f )
		{
			return (int)Math.ceil((numFramesRequired - curFpOffset) * replaySpeed);
		}
		else if( replaySpeed < 0.0f )
		{
			return (int)Math.ceil((numFramesRequired - (1.0f - curFpOffset)) * replaySpeed);
		}
		else // if( replaySpeed == 0.0f )
		{
			return 1;
		}
	}

}
