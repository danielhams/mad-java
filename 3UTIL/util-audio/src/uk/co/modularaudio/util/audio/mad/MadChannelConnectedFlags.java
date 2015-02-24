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

package uk.co.modularaudio.util.audio.mad;


public class MadChannelConnectedFlags
{
	private final byte[] storage;
	private final int numBytes;

	public MadChannelConnectedFlags( final int numChannelInstances )
	{
		numBytes = (numChannelInstances / 8) + 1;
		storage = new byte[ numBytes ];
	}

	public void set( final int idx )
	{
		final int storageIndex = idx / 8;
		storage[ storageIndex ] = (byte)(storage[ storageIndex ] | ( 1 << (idx%8)));
	}

	public boolean get( final int idx )
	{
		return (storage[ (idx/8) ] & (1 << (idx % 8))) != 0;
	}

	public boolean logicalAnd( final byte[] valsToAnd )
	{
		boolean retVal = true;

		final int numToTest = (numBytes < valsToAnd.length ? numBytes : valsToAnd.length );
		for( int b = 0 ; b < numToTest ; ++b )
		{
			retVal = retVal & ((storage[b] & valsToAnd[b]) == valsToAnd[b] );
		}
		return retVal;
	}

	public byte[] createMaskForSetChannels()
	{
		final byte[] copy = new byte[numBytes];
		System.arraycopy( storage, 0, copy, 0, numBytes );
		return copy;
	}
}
