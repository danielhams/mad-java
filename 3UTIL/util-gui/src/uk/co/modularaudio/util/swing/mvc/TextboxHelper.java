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

package uk.co.modularaudio.util.swing.mvc;

import uk.co.modularaudio.util.math.MathFormatter;

public class TextboxHelper
{

	public final static float parseFloatTextbox( final String valueStr, final int numDecPlaces )
	{
		float valueAsFloat = 0.0f;
		if( valueStr.length() > 3 &&
				(valueStr.charAt( 0 ) == 'I' ||
				valueStr.charAt( 0 ) == 'i' ||
				valueStr.charAt( 1 ) == 'I' ||
				valueStr.charAt( 1 ) == 'i' ) )
		{
			final String valueLcStr = valueStr.toLowerCase();
			if( valueLcStr.equals("-inf"))
			{
				valueAsFloat = Float.NEGATIVE_INFINITY;
			}
			else if( valueLcStr.equals("inf") ||
					valueLcStr.equals("+inf"))
			{
				valueAsFloat = Float.POSITIVE_INFINITY;
			}
		}
		else
		{
			valueAsFloat = Float.parseFloat( valueStr );
			final String truncToPrecisionStr = MathFormatter.fastFloatPrint( valueAsFloat, numDecPlaces, false );
			valueAsFloat = Float.parseFloat( truncToPrecisionStr );
		}
		return valueAsFloat;
	}

}
