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

package uk.co.modularaudio.mads.base.specampgen.util;

import uk.co.modularaudio.util.audio.stft.StftDataFrame;
import uk.co.modularaudio.util.audio.stft.StftParameters;
import uk.co.modularaudio.util.audio.stft.tools.ComplexPolarConverter;

public class SpectralPeakAmpAccumulator
{
//	private static Log log = LogFactory.getLog( SpectralPeakAmpAccumulator.class.getName());

	private int numChannels;
	private int numBins;
	private ComplexPolarConverter complexPolarConverter;
	private float[][] computedAmps;

//	private static final float CUR_VAL_WEIGHT = 0.5f;
//	private static final float PREV_VAL_WEIGHT = 0.5f;

	private boolean ampsTaken = true;

	public SpectralPeakAmpAccumulator()
	{
	}

	public void setParams( final StftParameters params )
	{
		this.numChannels = params.getNumChannels();
		this.numBins = params.getNumBins();
		this.complexPolarConverter = new ComplexPolarConverter( params );
		this.computedAmps = new float[numChannels][];
		for( int chan = 0 ; chan < numChannels ; chan++ )
		{
			computedAmps[chan] = new float[ numBins ];
		}
	}


	public int processIncomingFrame( final StftDataFrame curFrame )
	{
		complexPolarConverter.complexToPolarAmpsOnly( curFrame );
		for( int chan = 0 ; chan < numChannels ; chan++ )
		{
			for( int s = 0 ; s < numBins ; s++ )
			{
//				final float prevValue = computedAmps[chan][s];
				final float newValue = curFrame.amps[chan][s];

//				computedAmps[chan][s] = (prevValue * PREV_VAL_WEIGHT) +
//						(newValue * CUR_VAL_WEIGHT );
				computedAmps[chan][s] = newValue;

			}
		}
		ampsTaken = false;

		return 0;
	}

	public float[][] getComputedAmpsMarkTaken()
	{
		ampsTaken = true;
		return computedAmps;
	}

	public boolean hasNewAmps()
	{
		return !ampsTaken;
	}

}
