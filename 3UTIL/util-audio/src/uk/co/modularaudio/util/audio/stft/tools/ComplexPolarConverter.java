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

package uk.co.modularaudio.util.audio.stft.tools;

import uk.co.modularaudio.util.audio.stft.StftDataFrame;
import uk.co.modularaudio.util.audio.stft.StftParameters;
import uk.co.modularaudio.util.math.FastMath;

public class ComplexPolarConverter
{
//	private static Log log = LogFactory.getLog( ComplexPolarConverter.class.getName() );

	private final int numBins;
	private final int numChannels;

	public ComplexPolarConverter( final StftParameters params )
	{
		this.numChannels = params.getNumChannels();
		this.numBins = params.getNumBins();
	}

	public final void complexToPolar( final StftDataFrame frame )
	{
		final float[][] complexFrame = frame.complexFrame;
		final float[][] amps = frame.amps;
		final float[][] phases = frame.phases;
		float real;
		float imag;
		float amp;
		float phase;

		for( int chan = 0 ; chan < numChannels ; chan++ )
		{

			for( int i = 1 ; i < numBins - 1 ; i++ )
			{
				real = complexFrame[chan][ (i*2) ];
				imag = complexFrame[chan][ (i * 2) + 1 ];
				amp = (float)Math.sqrt( ((real * real) + (imag * imag)) );
				amps[chan][i] = amp;
			    phase = FastMath.atan2( imag, real );
				phases[chan][i] = phase;
			}
			// Copy over the amps for 0 and nyquist/2
			frame.dcSign[chan] = ( complexFrame[chan][0] < 0.0f ? -1 : 1 );
			amps[chan][0] = complexFrame[chan][0] * frame.dcSign[chan];
			frame.nySign[chan] = ( complexFrame[chan][1] < 0.0f ? -1 : 1 );
			amps[chan][numBins - 1 ] = complexFrame[chan][1] * frame.nySign[chan];
		}
	}

	public final void complexToPolarAmpsOnly( final StftDataFrame frame )
	{
		final float[][] complexFrame = frame.complexFrame;
		final float[][] amps = frame.amps;
		float real;
		float imag;
		float amp;

		for( int chan = 0 ; chan < numChannels ; chan++ )
		{
			for( int i = 1 ; i < numBins - 1 ; i++ )
			{
				real = complexFrame[chan][ (i*2) ];
				imag = complexFrame[chan][ (i * 2) + 1 ];
				amp = (float)Math.sqrt( ((real * real) + (imag * imag)) );
				amps[chan][i] = amp;
			}
			// Copy over the amps for 0 and nyquist/2
			frame.dcSign[chan] = ( complexFrame[chan][0] < 0.0f ? -1 : 1 );
			amps[chan][0] = complexFrame[chan][0] * frame.dcSign[chan];
			frame.nySign[chan] = ( complexFrame[chan][1] < 0.0f ? -1 : 1 );
			amps[chan][numBins - 1 ] = complexFrame[chan][1] * frame.nySign[chan];
		}
	}

	public final void polarToComplex( final StftDataFrame frame )
	{
		final float[][] complexFrame = frame.complexFrame;
		final float[][] amps = frame.amps;
		final float[][] phases = frame.phases;
		float amp;
		float phase;

		for( int chan = 0 ; chan < numChannels ; chan++ )
		{

			// Copy over the amps for 0 and nyquist/2
			complexFrame[chan][0] = amps[chan][0] * frame.dcSign[chan];
			complexFrame[chan][1] = amps[chan][numBins - 1 ] * frame.nySign[chan];

			// Now loop over the amps and freqs, computing the needed phase before turning it
			// back to complex
			for( int i = 1 ; i < numBins - 1 ; i++ )
			{
				amp = amps[chan][ i ];
				phase = phases[chan][ i ];
				complexFrame[chan][ i*2 ] = (float)(amp * Math.cos( phase ) );
				complexFrame[chan][ (i*2) + 1 ] = (float)(amp * Math.sin( phase ) );
			}
		}
	}

	public final void onePolarToComplex( final float[] outputAmps,
			final float[] outputPhases,
			final float[] outputFftBuffer,
			final int binNumber )
	{
		final float[] complexFrame = outputFftBuffer;
		final float[] amps = outputAmps;
		final float[] phases = outputPhases;
		float amp;
		float phase;

		amp = amps[ binNumber ];
		phase = phases[ binNumber ];
		complexFrame[ binNumber*2 ] = (float)(amp * Math.cos( phase ) );
		complexFrame[ (binNumber*2) + 1 ] = (float)(amp * Math.sin( phase ) );

	}

	public final void oneComplexToPolar( final float[] complexFrame,
			final float[] amps,
			final float[] phases,
			final int binNumber )
	{
		float real;
		float imag;
		float amp;
		float phase;

		real = complexFrame[ ( binNumber*2) ];
		imag = complexFrame[ (binNumber * 2) + 1 ];
		amp = (float)( Math.sqrt( (real*real) + (imag*imag)) );
		amps[binNumber] = amp;
		phase = FastMath.atan2( imag, real );
		phases[binNumber] = phase;
	}

	public final float oneComplexToPolarPhaseOnly( final float[] complexFrame,
			final int binNumber )
	{
		float real;
		float imag;

		real = complexFrame[ ( binNumber*2) ];
		imag = complexFrame[ (binNumber * 2) + 1 ];
		return FastMath.atan2( imag, real );
	}
}
