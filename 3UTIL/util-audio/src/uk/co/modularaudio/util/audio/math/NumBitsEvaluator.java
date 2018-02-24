/**
 *
 * Copyright (C) 2015 -> 2018 - Daniel Hams, Modular Audio Limited
 *                              daniel.hams@gmail.com
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

package uk.co.modularaudio.util.audio.math;

import uk.co.modularaudio.util.lang.StringUtils;
import uk.co.modularaudio.util.math.MathFormatter;

public interface NumBitsEvaluator
{
	public class NumBitsAndConfidence
	{
		public final int numSignificantBits;
		public final int numDataBits;
		public final float confidence;

		public NumBitsAndConfidence( final int numSignificantBits,
				final int numDataBits,
				final float confidence )
		{
			this.numSignificantBits = numSignificantBits;
			this.numDataBits = numDataBits;
			this.confidence = confidence;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + Float.floatToIntBits( confidence );
			result = prime * result + numDataBits;
			result = prime * result + numSignificantBits;
			return result;
		}

		@Override
		public boolean equals( final Object obj )
		{
			if( this == obj )
			{
				return true;
			}
			if( obj == null )
			{
				return false;
			}
			if( !(obj instanceof NumBitsAndConfidence) )
			{
				return false;
			}
			final NumBitsAndConfidence other = (NumBitsAndConfidence) obj;
			if( Float.floatToIntBits( confidence ) != Float.floatToIntBits( other.confidence ) )
			{
				return false;
			}
			if( numDataBits != other.numDataBits )
			{
				return false;
			}
			if( numSignificantBits != other.numSignificantBits )
			{
				return false;
			}
			return true;
		}

		@Override
		public String toString()
		{
			final StringBuilder sb = new StringBuilder( 256 );
			sb.append( "NSB(" );
			StringUtils.appendFormattedInt( sb, 3, numSignificantBits );
			sb.append( ") NDB(" );
			StringUtils.appendFormattedInt( sb, 3, numDataBits );
			sb.append( ") Conf(" );
			sb.append( MathFormatter.fastFloatPrint( confidence, 4, true ) );
			sb.append( ")" );

			return sb.toString();
		}
	};

	public final static int MAX_BITS = 63;

	public abstract void reset( int numSignificantBits ) throws TooManyBitsException;

	public abstract void addValue( int numSignificantBits, long sv ) throws TooManyBitsException;

	public abstract NumBitsAndConfidence getNumBitsAndConfidence();

	public abstract void dumpNFirstUniqueValues( int n );

	public abstract long getNumKeysAdded();
	public abstract long getNumTotalKeys();

	public abstract long getNumDeltas();

}
