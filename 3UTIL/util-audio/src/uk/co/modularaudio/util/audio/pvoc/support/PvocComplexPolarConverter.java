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

package uk.co.modularaudio.util.audio.pvoc.support;

import uk.co.modularaudio.util.math.FastMath;

public strictfp class PvocComplexPolarConverter
{
//	private static Log log = LogFactory.getLog( PvocComplexPolarConverter.class.getName() );
	
	public PvocComplexPolarConverter()
	{
	}

	public strictfp final void onePolarToComplex( float[] outputAmps,
			float[] outputPhases,
			float[] outputFftBuffer,
			int binNumber )
	{
		float[] complexFrame = outputFftBuffer;
		float[] amps = outputAmps;
		float[] phases = outputPhases;
		float amp;
		float phase;

		amp = amps[ binNumber ];
		phase = phases[ binNumber ];
		complexFrame[ binNumber*2 ] = (float)(amp * Math.cos( phase ) );
		complexFrame[ (binNumber*2) + 1 ] = (float)(amp * Math.sin( phase ) );
//		log.debug("Turned polar back to complex (" + amp + ", " + phase + ")->(" + complexFrame[ binNumber * 2 ] + ", " + complexFrame[ (binNumber * 2) + 1 ] + ")");
	}
	
	public strictfp final float oneComplexToPolarPhaseOnly( float[] complexFrame,
			int binNumber )
	{
		float real;
		float imag;

		real = complexFrame[ ( binNumber*2) ];
		imag = complexFrame[ (binNumber * 2) + 1 ];
		return FastMath.atan2( imag, real );
	}
}
