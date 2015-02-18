package uk.co.modularaudio.mads.base.controlprocessingtester.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.fft.HannFftWindow;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.timing.AudioTimingUtils;

public class HalfHannWindowInterpolator implements ControlValueInterpolator
{
	private static Log log = LogFactory.getLog( HalfHannWindowInterpolator.class.getName() );

	private HannFftWindow fullHannWindow;
	private float[] hannBuffer;

	private int curWindowPos;
	private int lastWindowPos;

	private float curVal;
	private float desVal;
	private boolean haveValWaiting = false;
	private float nextVal;

	public HalfHannWindowInterpolator()
	{
	}

	public void reset( final int sampleRate, final float valueChaseMillis )
	{
		final int halfWindowLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( sampleRate, valueChaseMillis );
		fullHannWindow = new HannFftWindow( halfWindowLength * 2 );
		hannBuffer = fullHannWindow.getAmps();
		curWindowPos = 0;
		lastWindowPos = halfWindowLength;
	}

	@Override
	public void generateControlValues( final float[] output, final int outputIndex, final int length )
	{
//		if( length == 0 )
//		{
//			log.error("Have zero length control value generation");
//		}
		final int numLeftInHann = lastWindowPos - curWindowPos;

		final int numWithHann = (numLeftInHann < length ? numLeftInHann : length );
		final int numAfter = length - numWithHann;

//		if( numLeftInHann > 0 )
//		{
//			log.trace("Val(" + desVal + ") - have " + numLeftInHann + " left to fade of " + lastWindowPos +
//					" doing " + numWithHann + " this time around" );
//		}
		for( int i = 0 ; i < numWithHann ; ++i )
		{
			final float hannVal = hannBuffer[curWindowPos++];
			final float nonHannVal = 1.0f - hannVal;
			final float newVal = (curVal * nonHannVal) + (desVal * hannVal );
			output[ outputIndex + i ] = newVal;
		}

		if( curWindowPos == lastWindowPos )
		{
			curVal = desVal;
		}

		if( numAfter > 0 )
		{
//			if( curWindowPos < lastWindowPos )
//			{
//				log.error("Fell into num after but window wasn't complete :-(");
//			}
			if( haveValWaiting )
			{
//				log.trace("Val(" + desVal + ") complete have a waiting val (" + nextVal + ") with " + numAfter + " samples this round");
				desVal = nextVal;
				haveValWaiting = false;
				curWindowPos = 0;

				generateControlValues( output, outputIndex + numWithHann, numAfter );
			}
			else
			{
				for( int i = numWithHann ; i < length ; ++i )
				{
					output[ outputIndex + i ] = curVal;
				}
			}
		}
	}

	@Override
	public void notifyOfNewIncomingAmp( final float amp )
	{
		if( curWindowPos < lastWindowPos )
		{
//			log.trace("Queued - Val(" + desVal + ") not yet complete (" +
//					(lastWindowPos - curWindowPos) + " left) having to delay(" +
//					amp + ")" );
//			if( haveValWaiting )
//			{
//				log.trace("Override of Val(" + nextVal + ")");
//			}
			haveValWaiting = true;
			nextVal = amp;
		}
		else
		{
//			log.trace("Val(" + amp + ") queued alone");
			// Restart the hann half window fade
			curWindowPos = 0;
			desVal = amp;
		}
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
