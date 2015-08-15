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

package uk.co.modularaudio.util.audio.lookuptable;


public class LookupTableUtils
{
//	private static Log log = LogFactory.getLog( WaveTableUtils.class.getName() );

	public static void normaliseFloats(final float[] table, final int position, final int length )
	{
		int n;
		float absMax = 0.f;
		for(n=0; n < length; n++)
		{
			final float curVal = table[ position + n];
			final float curAbs = ( curVal < 0.0f ? -curVal : curVal );
			absMax = curAbs > absMax ? curAbs : absMax;
		}

		for(n=0; n < length; n++)
		{
			table[ position + n] /= absMax;
		}
	}

	public static void normaliseFloatsToMax(final float[] table, final int position, final int length, final float maxValue )
	{
		int n;
		float absMax = 0.f;
		for( n=0; n < length; n++ )
		{
			final float curVal = table[ position + n ];
			final float curAbs = ( curVal < 0.0f ? -curVal : curVal );
			absMax = curAbs > absMax ? curAbs : absMax;
		}

		absMax = absMax / maxValue;

		for( n=0; n < length; n++ )
		{
			table[ position + n ] /= absMax;
		}
	}

	public static void normaliseDoublesToMax(final double[] table,
			final int position,
			final int length,
			final double maxValue )
	{
		int n;
		double absMax = 0.0;
		for( n=0; n < length; n++ )
		{
			final double curVal = table[ position + n ];
			final double curAbs = ( curVal < 0.0 ? -curVal : curVal );
			absMax = curAbs > absMax ? curAbs : absMax;
		}

		absMax = absMax / maxValue;

		for( n=0; n < length; n++ )
		{
			table[ position + n ] /= absMax;
		}
	}
}
