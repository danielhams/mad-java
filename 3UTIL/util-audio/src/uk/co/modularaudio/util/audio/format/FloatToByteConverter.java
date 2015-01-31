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

package uk.co.modularaudio.util.audio.format;


public class FloatToByteConverter
{
//	private static Log log = LogFactory.getLog( FloatToByteConverter.class.getName());
	
	public static int floatToByteConversion( float[] sourceFloats, int position, int numFloats, byte[] destination, int bpos, boolean bigEndian )
		throws ArrayIndexOutOfBoundsException
	{
		int numConverted = 0;
		
		for( int i = 0 ; i < numFloats ; i++ )
		{
			int sourcePosition = position + i;
			int destPosition = bpos + (i * 2);
			
			float fSample = sourceFloats[ sourcePosition ];
			
			// scaling and conversion to integer
			int nSample =(int)(fSample * 32767.0F);

			if( bigEndian )
			{
				destination[ destPosition ] = (byte) ((nSample >> 8) & 0xFF);
				destination[ destPosition + 1 ] = (byte) (nSample & 0xFF);
			}
			else
			{
				destination[ destPosition ] = (byte) (nSample & 0xFF);
				destination[ destPosition + 1 ] = (byte) ((nSample >> 8) & 0xFF);
			}
			numConverted++;
		}
		
		return numConverted;
	}
	
	public static int byteToFloatConversion( byte[] sourceBytes, int position, float[] destination, int fpos, int numFloats, boolean bigEndian )
		throws ArrayIndexOutOfBoundsException
	{
		int numConverted = 0;
		
		for( int i = 0 ; i < numFloats ; i++ )
		{
			int sourcePosition = position + ( i * 2 );
			int destPosition = fpos + i;
			
			byte b1 = sourceBytes[ sourcePosition ];
			byte b2 = sourceBytes[ sourcePosition + 1 ];
			int iVal = 0;
			if( bigEndian )
			{
				iVal = ( b1 << 8 ) | ( b2 & 0xFF );
			}
			else
			{
				iVal = ( b2 << 8 ) | ( b1 & 0xFF );
			}
			// Conversion to float
			float fSample = ((float)iVal) / 32767.0F;
			destination[ destPosition ] = fSample;
			numConverted++;
		}
		return numConverted;
	}
	
}
