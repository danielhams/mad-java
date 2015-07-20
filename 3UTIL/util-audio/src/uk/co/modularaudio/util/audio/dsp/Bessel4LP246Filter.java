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

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Bessel4LP246Filter
{
	private static Log log = LogFactory.getLog( Bessel4LP246Filter.class.getName() );

	// One gain, four coefficients
	private final float[] coefs = new float[5];

	// space for the iir calculation
	private final float[] bufferlp = new float[4];

	// 24 dB / Oct rolloff
	public Bessel4LP246Filter()
	{
		clear( 44100 );
	}

	public final void clear( final int sampleRate )
	{
		Arrays.fill( bufferlp, 0.0f );
		recompute( sampleRate );
	}

	private final void recompute( final int sr )
	{
		switch( sr )
		{
			case 44100:
			{
				coefs[0] = 0.000000456820401950608356855810f;
				coefs[1] = 0.932633817195892333984375000000f;
				coefs[2] = -1.929584026336669921875000000000f;
				coefs[3] = 0.908407211303710937500000000000f;
				coefs[4] = -1.906010627746582031250000000000f;
				break;
			}
			case 48000:
			{
				coefs[0] = 0.000000327649388509598793461919f;
				coefs[1] = 0.937931060791015625000000000000f;
				coefs[2] = -1.935349345207214355468750000000f;
				coefs[3] = 0.915528237819671630859375000000f;
				coefs[4] = -1.913497567176818847656250000000f;
				break;
			}
			case 88200:
			{
				coefs[0] = 0.000000029742851737069031514693f;
				coefs[1] = 0.965722560882568359375000000000f;
				coefs[2] = -1.964946746826171875000000000000f;
				coefs[3] = 0.953112363815307617187500000000f;
				coefs[4] = -1.952498912811279296875000000000f;
				break;
			}

			case 96000:
			{
				coefs[0] = 0.000000021262872351712758245412f;
				coefs[1] = 0.968462884426116943359375000000f;
				coefs[2] = -1.967807054519653320312500000000f;
				coefs[3] = 0.956838905811309814453125000000f;
				coefs[4] = -1.956320166587829589843750000000f;
				break;
			}
			case 192000:
			{
				coefs[0] = 0.000000001354315215529311444698f;
				coefs[1] = 0.984104394912719726562500000000f;
				coefs[2] = -1.983939170837402343750000000000f;
				coefs[3] = 0.978182256221771240234375000000f;
				coefs[4] = -1.978051185607910156250000000000f;
				break;
			}
			default:
			{
				if( log.isErrorEnabled() )
				{
					log.error("No coefficients for sample rate: " + sr );
				}
				Arrays.fill( coefs, 0.0f );
			}
		}
	}

	public final void filter( final float[] lowpass, final int offset, final int length )
	{
		final int endIter = offset + length;
		for( int i = offset ; i < endIter ; ++i )
		{
			float tmplp;
			float fir;
			float iirlp;
			tmplp = bufferlp[0];
			bufferlp[0] = bufferlp[1];
			bufferlp[1] = bufferlp[2];
			bufferlp[2] = bufferlp[3];

			iirlp = lowpass[i] * coefs[0];

			iirlp -= coefs[1] * tmplp;
			fir = tmplp;
			iirlp -= coefs[2] * bufferlp[0];
			fir += bufferlp[0] + bufferlp[0];
			fir += iirlp;

			tmplp = bufferlp[1];
			bufferlp[1] = iirlp;
			lowpass[i] = fir;
			iirlp = lowpass[i];

			iirlp -= coefs[3] * tmplp;
			fir = tmplp;
			iirlp -= coefs[4] * bufferlp[2];
			fir += bufferlp[2] + bufferlp[2];
			fir += iirlp;

			bufferlp[3] = iirlp;
			lowpass[i] = fir;
		}
	}
}
