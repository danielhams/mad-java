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

package uk.co.modularaudio.service.audioanalysis.impl.analysers.beatdetection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.dsp.ButterworthFilter;
import uk.co.modularaudio.util.audio.fft.FftUtils;

public class BeatDetectionRT
{
	private static Log log = LogFactory.getLog( BeatDetectionRT.class.getName() );

	ButterworthFilter butterworth = new ButterworthFilter();

	int channels;
	int winlen;
	float[] rwv;
	float[] gwv;
	float[] dfwv;
	float[] dfrev;
	float[] acf;
	float[] acfout;
	float[] phwv;
	float[] phout;
	int timesig;
	int step;
	int rayparam;
	float lastbeat;
	int counter;
	int flagstep;
	float gvar;
	float gp;
	float bp;
	float rp;
	float rp1;
	float rp2;

	public BeatDetectionRT( final int channels, final int winlen )
	{
		this.channels = channels;
		this.winlen = winlen;

		final float floatRayparam = (48.0f / 512.0f) * winlen;

		final double logOfTwoOverRayparam = Math.log(2.0) / floatRayparam;
		final float dfwvnorm = (float)(Math.exp( logOfTwoOverRayparam * (winlen + 2 )));

		final int laglen = winlen / 4;
		final int step = winlen / 4;
		lastbeat = 0;
		counter = 0;
		flagstep = 0;
		gvar = 3.901f;
		rp = 1;
		gp = 0;
		rayparam = (int)floatRayparam;
		this.step = step;
		rwv = new float[ laglen ];
		gwv = new float[ laglen ];
		dfwv = new float[ winlen ];
		dfrev = new float[ winlen ];
		acf = new float[ winlen ];
		acfout = new float[ laglen ];
		phwv = new float[ 2 * laglen ];
		phout = new float[ winlen ];
		timesig = 0;

		for( int i = 0; i < winlen ; i++ )
		{
			dfwv[i] = (float)(Math.exp( logOfTwoOverRayparam * (i+1) )) / dfwvnorm;
		}

		final float rayparamSquared = rayparam * rayparam;
		for( int i = 0 ; i < laglen ; i++ )
		{
			final int iPlusOne = i+1;
			final int iPlusOneSquared = iPlusOne * iPlusOne;
			rwv[i] = (float)(
					(iPlusOne / ( rayparamSquared ) )
					*
					Math.exp( (-1 * ( iPlusOneSquared )) / ( 2.0 * ( rayparamSquared ) ) )
				);
			if( rwv[i] == 0.0f )
			{
				log.debug("Bah, generated reverse wave is zero");
			}
		}
	}

	public float getBpm()
	{
		if( timesig != 0 && counter == 0 && flagstep == 0 )
		{
			return 5168.0f / FftUtils.quadint( acfout, (int)bp );
		}
		else
		{
			return 0.0f;
		}
	}

	public float getConfidence()
	{
		if( gp != 0.0f )
		{
			return FftUtils.maxValue( acfout );
		}
		else
		{
			return 0.0f;
		}
	}
}
