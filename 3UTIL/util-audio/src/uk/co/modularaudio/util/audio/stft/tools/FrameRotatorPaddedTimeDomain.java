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

import java.util.Arrays;

import uk.co.modularaudio.util.audio.stft.StftParameters;

public class FrameRotatorPaddedTimeDomain
{
//	private static Log log = LogFactory.getLog( FrameRotatorPaddedTimeDomain.class.getName() );
	
//	private StftParameters params = null;
	
	private int windowLength = -1;
	private int numReals = -1;
	private int translation = -1;
	
	public FrameRotatorPaddedTimeDomain( StftParameters params )
	{
//		this.params = params;
		windowLength = params.getWindowLength();
		numReals = params.getNumReals();
		translation = numReals - (windowLength / 2);
	}
	
	public final void inRotate( float[] data, int startIndex, float[] output )
	{
		for( int i = 0 ; i < windowLength ; i++ )
		{
			int inIndex = i;
			int outIndex = (i + translation ) % numReals;
			output[ outIndex ] = data[ inIndex ];
		}
		// Fill rest with zeros
		int zerosStart = (windowLength + translation) % numReals;
		int zerosEnd = translation;
		Arrays.fill( output, zerosStart, zerosEnd, 0.0f );
	}

	public final void outRotate( float[] data, int startIndex, float[] output )
	{
		for( int i = 0 ; i < windowLength ; i++ )
		{
			int inIndex = (i + translation) % numReals;
			int outIndex = i;
			output[ outIndex ] = data[ inIndex ];
		}
	}
}
