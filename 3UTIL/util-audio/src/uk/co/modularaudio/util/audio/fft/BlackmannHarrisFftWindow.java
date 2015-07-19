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

package uk.co.modularaudio.util.audio.fft;

import uk.co.modularaudio.util.math.MathDefines;

public class BlackmannHarrisFftWindow extends FftWindow
{
	private final static double A1 = 0.35875;
	private final static double A2 = 0.48829;
	private final static double A3 = 0.14128;
	private final static double A4 = 0.01168;

	public BlackmannHarrisFftWindow( final int length )
	{
		super( length );
	}

	@Override
	protected final void fillAmps( final int length )
	{
		// a1 - a2 *cos ( 2PI * i / length -1 ) + a3 * cos( 4PI * i / length - 1) - a4 * cos( 6PI * i / length - 1)
		for( int i = 0 ; i < length ; i++ )
		{
			amps[i] = (float)(A1
					- (A2 * Math.cos( ( MathDefines.TWO_PI_D * i) / (length) ) )
					+ (A3 * Math.cos( ( MathDefines.TWO_PI_D * 2 * i ) / (length) ) )
					- (A4 * Math.cos( ( MathDefines.TWO_PI_D * 3 * i ) / (length) ) )
					);
		}
	}
}
