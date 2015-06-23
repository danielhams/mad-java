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

package uk.co.modularaudio.util.math;

public class MinMaxComputer
{
	public final static void calcMinMaxForFloats( final float[] src, final int iPos, final int length, final float[] inOutMinMax )
	{
		int pos = iPos;
		final int remainder = length % 2;
		final int endPos = pos + length - remainder;
		while( pos < endPos )
		{
			final float v1 = src[pos++];
			final float v2 = src[pos++];
			if( v1 < v2 )
			{
				if( v1 < inOutMinMax[0] )
				{
					inOutMinMax[0] = v1;
				}
				if( v2 > inOutMinMax[1] )
				{
					inOutMinMax[1] = v2;
				}
			}
			else
			{
				if( v2 < inOutMinMax[0] )
				{
					inOutMinMax[0] = v2;
				}
				if( v1 > inOutMinMax[1] )
				{
					inOutMinMax[1] = v1;
				}
			}
		}
		if( remainder != 0 )
		{
			final float vr = src[pos];
			if( vr < inOutMinMax[0] )
			{
				inOutMinMax[0] = vr;
			}
			if( vr > inOutMinMax[1] )
			{
				inOutMinMax[1] = vr;
			}
		}
	}

	public final static void calcMinMaxForFloatsWithStride( final float[] src, final int iPos, final int length, final int stride, final float[] inOutMinMax )
	{
		int pos = iPos;
		final int remainder = length % 2;
		final int endPos = pos + ((length - remainder) * stride);
		while( pos < endPos )
		{
			final float v1 = src[pos];
			pos += stride;
			final float v2 = src[pos];
			pos += stride;
			if( v1 < v2 )
			{
				if( v1 < inOutMinMax[0] )
				{
					inOutMinMax[0] = v1;
				}
				if( v2 > inOutMinMax[1] )
				{
					inOutMinMax[1] = v2;
				}
			}
			else
			{
				if( v2 < inOutMinMax[0] )
				{
					inOutMinMax[0] = v2;
				}
				if( v1 > inOutMinMax[1] )
				{
					inOutMinMax[1] = v1;
				}
			}
		}
		if( remainder != 0 )
		{
			final float vr = src[pos];
			if( vr < inOutMinMax[0] )
			{
				inOutMinMax[0] = vr;
			}
			if( vr > inOutMinMax[1] )
			{
				inOutMinMax[1] = vr;
			}
		}
	}
}
