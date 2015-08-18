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
