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

package uk.co.modularaudio.util.audio.pvoc.frame;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PvocFrameRotatorPaddedTimeDomain
{
	private static Log log = LogFactory.getLog( PvocFrameRotatorPaddedTimeDomain.class.getName() );
	
	private int halfWindowLength = -1;
	private int translation = -1;

	private int inRotateZerosStart = -1;
	private int inRotateZerosEnd = -1;
	
	public PvocFrameRotatorPaddedTimeDomain( int windowLength, int fftSize )
	{
		this.halfWindowLength = windowLength / 2;
		translation = fftSize - halfWindowLength;

		inRotateZerosStart = (windowLength + translation) % fftSize;
		inRotateZerosEnd = translation;
		
		log.debug( "Frame rotator with trans " + translation + " and half window length " + halfWindowLength );
		log.debug("ZerosStart " + inRotateZerosStart + " ZerosEnd " + inRotateZerosEnd );
	}
	
	public final void inRotate( float[] inData, float[] outData )
	{
		System.arraycopy( inData, 0, outData, inRotateZerosEnd, halfWindowLength );
		System.arraycopy( inData, halfWindowLength, outData, 0, halfWindowLength );
		Arrays.fill( outData, inRotateZerosStart, inRotateZerosEnd, 0.0f );
	}

	public final void outRotate( float[] inData, float[] outData )
	{
		System.arraycopy( inData, 0, outData, halfWindowLength, halfWindowLength );
		System.arraycopy( inData, inRotateZerosEnd, outData, 0, halfWindowLength );
	}
}
