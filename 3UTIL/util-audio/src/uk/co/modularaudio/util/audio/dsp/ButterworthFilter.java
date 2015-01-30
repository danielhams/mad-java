/**
 *
 * Copyright (C) 2015 - Daniel Hams, Modular Audio Limited
 *                      daniel.hams@gmail.com
 *
 * Mad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mad.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.co.modularaudio.util.audio.dsp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.math.FastMath;
import uk.co.modularaudio.util.math.MathDefines;

public class ButterworthFilter
{
	private static Log log = LogFactory.getLog( ButterworthFilter.class.getName() );

//	private final static boolean DEBUG_NAN = false;

	public static float filter( final ButterworthFilterRT rt, final float[] input, final int offset, final int length, float freq, final float bw, final FrequencyFilterMode mode, final float sr )
	{
		boolean allZeros = true;
		for( int s = 0 ; s < length ; s++ )
		{
			final float curVal = input[ offset + s ];
			if( curVal < 0.0f && curVal > -AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
			{
				input[ offset + s ] = 0.0f;
			}
			else if( curVal > 0.0f && curVal < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
			{
				input[ offset + s ] = 0.0f;
			}
			else if( curVal == 0.0f || curVal == -0.0f )
			{
			}
			else
			{
				allZeros = false;
			}
		}

		if( allZeros )
		{
			return 0.0f;
		}

		float a, a1, a2, b1, b2, tanthe, costhe, sqrtan, tansq, w;
		if( freq < 10.0f )
		{
			freq = 10.0f;
		}

		if( mode == FrequencyFilterMode.NONE )
		{
			return input[ length - 1];
		}

		switch( mode )
		{
		case LP:
			tanthe = (float) (1.0f / FastMath.tan( MathDefines.ONE_PI_D * freq / sr ));
			if( tanthe == Float.NaN || tanthe == Float.NEGATIVE_INFINITY || tanthe == Float.POSITIVE_INFINITY )
			{
				log.debug("Something dicky is a foot");
			}

//			tanthe = 0.0f;
			sqrtan = MathDefines.SQRT_TWO_F * tanthe;
//			sqrtan = 0.0f;
			tansq = tanthe * tanthe;
			a = 1.0f / ( 1.0f + sqrtan + tansq );
			a1 = 2.0f * a;
			a2 = a;
			b1 = 2.0f * (1.0f - tansq) * a;
			b2 = (1.0f - sqrtan + tansq) * a;
			break;
		case HP:
			tanthe = (float) FastMath.tan( MathDefines.ONE_PI_D * freq / sr );
			sqrtan = MathDefines.SQRT_TWO_F * tanthe;
//			tanthe = 0.0f;
//			sqrtan = 0.0f;
			tansq = tanthe*tanthe;
			a = 1.0f / (1.0f + sqrtan + tansq );
			a1 = -2.0f * a;
			a2 = a;
			b1 = 2.0f * (tansq - 1.0f) * a;
			b2 = (1.0f - sqrtan + tansq) * a;
			break;
		case BR:
			tanthe = (float) FastMath.tan( MathDefines.ONE_PI_D * bw / sr );
			costhe = (float) (2.0f * FastMath.cos( MathDefines.TWO_PI_D * freq / sr ));
//			tanthe = 0.0f;
//			costhe = 0.0f;
			a = 1.0f  / ( 1.0f + tanthe );
			a1 = -costhe * a;
			a2 = a;
			b1 = -costhe * a;
			b2 = (1.0f - tanthe) * a;
			break;
		case BP:
			tanthe = (float) (1.0f / FastMath.tan( MathDefines.ONE_PI_D * bw / sr ));
			costhe = (float) (2.0f * FastMath.cos( MathDefines.TWO_PI_D * freq / sr ));
//			tanthe = 0.0f;
//			costhe = 0.0f;
			a = 1.0f / ( 1.0f + tanthe );
			a1 = 0;
			a2 = -a;
			b1 = -tanthe * costhe * a;
			b2 = (tanthe - 1.0f ) * a;
			break;
		default:
			a = 0;
			a1 = 0;
			a2 = 0;
			b1 = 0;
			b2 = 0;
			break;
		}

		final float[] fbds = rt.feedbackDelaySample;

		for( int i = 0 ; i < length ; i++ )
		{
			w = input[offset + i] - b1*fbds[0] - b2*fbds[1];
			float result = (a*w + a1*fbds[0] + a2*fbds[1]);
			// Clamp output samples
//			if( result > 1.0f )
//			{
//				result =1.0f;
//			}
//			else if( result < -1.0f )
//			{
//				result = -1.0f;
//			}
			if( (result > 0.0f && result < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F) ||
					(result < 0.0f && result > -AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F ) )
			{
				result = 0.0f;
			}
			if( result == -0.0f )
			{
				result = 0.0f;
			}
			input[offset + i] = result;

			fbds[1] = fbds[0];
			fbds[0] = w;

//			if( DEBUG_NAN )
//			{
//				if( Double.isNaN(rt.feedbackDelaySample[ 0 ]) )
//				{
//					log.error("FDS[0] is NaN");
//					rt.feedbackDelaySample[0] = 0.0f;
//				}
//				if( Double.isNaN(rt.feedbackDelaySample[ 1 ]) )
//				{
//					log.error("FDS[1] is NaN");
//					rt.feedbackDelaySample[1] = 0.0f;
//				}
//			}
		}

		return input[offset];
	}

	public static float filterWithFreq( final ButterworthFilterRT rt, final float[] input, final int offset, final int length, final float[] freqs, final float bw, final FrequencyFilterMode mode, final float sr )
	{
		float a, a1, a2, b1, b2, tanthe, costhe, sqrtan, tansq, w;

		if( mode == FrequencyFilterMode.NONE )
		{
			return input[ length - 1];
		}

		for( int s = 0 ; s < length ; s++ )
		{
			final float curVal = input[s];
			if( curVal < 0.0f && curVal > -AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
			{
				input[ s ] = 0.0f;
			}
			else if( curVal > 0.0f && curVal < AudioMath.MIN_FLOATING_POINT_24BIT_VAL_F )
			{
				input[ s ] = 0.0f;
			}
		}

		for( int i = 0 ; i < length ; i++ )
		{
			float freq = freqs[i];
			if( freq < 1.0f )
			{
				freq = 1.0f;
			}
			switch( mode )
			{
			case LP:
				tanthe = (float) (1.0f / FastMath.tan( Math.PI * freq / sr ));
				sqrtan = MathDefines.SQRT_TWO_F * tanthe;
				tansq = tanthe * tanthe;
				a = 1.0f / ( 1.0f + sqrtan + tansq );
				a1 = 2.0f * a;
				a2 = a;
				b1 = 2.0f * (1.0f - tansq) * a;
				b2 = (1.0f - sqrtan + tansq) * a;
				break;
			case HP:
				tanthe = (float) FastMath.tan( Math.PI * freq / sr );
				sqrtan = MathDefines.SQRT_TWO_F * tanthe;
				tansq = tanthe*tanthe;
				a = 1.0f / (1.0f + sqrtan + tansq );
				a1 = -2.0f * a;
				a2 = a;
				b1 = 2.0f * (tansq - 1.0f) * a;
				b2 = (1.0f - sqrtan + tansq) * a;
				break;
			case BR:
				tanthe = (float) FastMath.tan( Math.PI * bw / sr );
				costhe = (float) (2.0f * FastMath.cos( 2 * Math.PI * freq / sr ));
				a = 1.0f  / ( 1.0f + tanthe );
				a1 = -costhe * a;
				a2 = a;
				b1 = -costhe * a;
				b2 = (1.0f - tanthe) * a;
				break;
			case BP:
				tanthe = (float) (1.0f / FastMath.tan( Math.PI * bw / sr ));
				costhe = (float) (2.0f * FastMath.cos( 2 * Math.PI * freq / sr ));
				a = 1.0f / ( 1.0f + tanthe );
				a1 = 0;
				a2 = -a;
				b1 = -tanthe * costhe * a;
				b2 = (tanthe - 1.0f ) * a;
				break;
			default:
				a = 0;
				a1 = 0;
				a2 = 0;
				b1 = 0;
				b2 = 0;
				break;
			}

			final float[] fbds = rt.feedbackDelaySample;

			w = input[offset + i] - b1*fbds[0] - b2*fbds[1];
			final float result = (a*w + a1*fbds[0] + a2*fbds[1]);
			// Clamp output samples
//			if( result > 1.0f )
//			{
//				result =1.0f;
//			}
//			else if( result < -1.0f )
//			{
//				result = -1.0f;
//			}
			input[offset + i] = result;

			fbds[1] = fbds[0];
			fbds[0] = w;

//			if( DEBUG_NAN )
//			{
//				if( Float.isNaN(fbds[ 0 ]) )
//				{
//					log.error("FDS[0] is NaN");
//					fbds[0] = 0.0f;
//				}
//				if( Float.isNaN(fbds[ 1 ]) )
//				{
//					log.error("FDS[1] is NaN");
//					fbds[1] = 0.0f;
//				}
//			}
		}

		return input[offset];
	}
}
