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

package uk.co.modularaudio.util.audio.io;

public class ArrayHelper
{

	public static final void copyWithOffsetAndStride( float[] src,
			int srcIndex,
			float[] dest,
			int destIndex,
			int destStride,
			int length )
	{
		for( int i = 0 ; i < length ; i++) 
		{
			int srcPos = srcIndex + i;
			int destPos  = destIndex + (i * destStride );
			dest[ destPos ] = src[ srcPos ];
		}
	}

	public static final void setZeroWithOffsetAndStride( float[] dest,
			int destIndex,
			int destStride,
			int length )
	{
		for( int i = 0 ; i < length ; i++) 
		{
			int destPos  = destIndex + (i * destStride );
			dest[ destPos ] = 0.0f;
		}
	}

	public static final void extractWithOffsetAndStride( float[] src,
			int srcOffset,
			int srcStride,
			float[] dest,
			int destOffset,
			int length )
	{
		for( int i = 0 ; i < length ; i++ )
		{
			int srcPos = srcOffset + (srcStride * i);
			int destPos = destOffset + i;
			dest[ destPos ] = src[ srcPos ];
		}
		
	}
}
