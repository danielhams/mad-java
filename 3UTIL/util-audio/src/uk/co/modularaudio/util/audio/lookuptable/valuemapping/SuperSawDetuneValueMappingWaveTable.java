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

package uk.co.modularaudio.util.audio.lookuptable.valuemapping;

public class SuperSawDetuneValueMappingWaveTable extends ValueMappingWaveTable
{

	private final static float POWER11_MULTIPLIER = 10028.7312891634f;
	private final static float POWER10_MULTIPLIER = 50818.8652045924f;
	private final static float POWER09_MULTIPLIER = 111363.4808729368f;
	private final static float POWER08_MULTIPLIER = 138150.6761080548f;
	private final static float POWER07_MULTIPLIER = 106649.6679158292f;
	private final static float POWER06_MULTIPLIER = 53046.9642751875f;
	private final static float POWER05_MULTIPLIER = 17019.9518580080f;
	private final static float POWER04_MULTIPLIER = 3425.0836591318f;
	private final static float POWER03_MULTIPLIER = 404.2703938388f;
	private final static float POWER02_MULTIPLIER = 24.1878824391f;
	private final static float POWER01_MULTIPLIER = 0.6717417634f;
	private final static float POWER00_MULTIPLIER = 0.0030115596f;

//	y = (10028.7312891634*x^11)
//			-(50818.8652045924*x^10)
//			+(111363.4808729368*x^9)
//			-(138150.6761080548*x^8)
//			+(106649.6679158292*x^7)
//			-(53046.9642751875*x^6)
//			+(17019.9518580080*x^5)
//			-(3425.0836591318*x^4)
//			+(404.2703938388*x^3)
//			-(24.1878824391*x^2)
//			+(0.6717417634*x)
//			+0.0030115596;

	public SuperSawDetuneValueMappingWaveTable( final int capacity )
	{
		super( capacity );

		// It's cubic so data goes from 1
		for( int s = 0 ; s < capacity ; s++ )
		{
			final int index = s+1;
			final float mappedValue = doPolyMap((float)s / (capacity-1));
			floatBuffer[ index ] = mappedValue;
		}

		completeCubicWaveTableSetupForOneShot();
	}

	private float doPolyMap( final float x )
	{
		return (float)(
				POWER11_MULTIPLIER * Math.pow( x, 11 )
				- POWER10_MULTIPLIER * Math.pow( x, 10 )
				+ POWER09_MULTIPLIER * Math.pow( x, 9 )
				- POWER08_MULTIPLIER * Math.pow( x, 8 )
				+ POWER07_MULTIPLIER * Math.pow( x, 7 )
				- POWER06_MULTIPLIER * Math.pow( x, 6 )
				+ POWER05_MULTIPLIER * Math.pow( x, 5 )
				- POWER04_MULTIPLIER * Math.pow( x, 4 )
				+ POWER03_MULTIPLIER * Math.pow( x, 3 )
				- POWER02_MULTIPLIER * Math.pow( x, 2 )
				+ POWER01_MULTIPLIER * x
				+ POWER00_MULTIPLIER
				);
	}
}
