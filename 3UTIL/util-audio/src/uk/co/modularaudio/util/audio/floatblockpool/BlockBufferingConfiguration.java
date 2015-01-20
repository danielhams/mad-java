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

package uk.co.modularaudio.util.audio.floatblockpool;


public class BlockBufferingConfiguration
{
	public int maxBlocksToBuffer;
	public int blockLengthInFloats;
	public float minSecsBeforePosition;
	public float minSecsAfterPosition;
	
	public BlockBufferingConfiguration( int maxBlocksToBuffer, 
			int blockLengthInFloats,
			float minSecsBeforePosition,
			float minSecsAfterPosition )
	{
		this.maxBlocksToBuffer = maxBlocksToBuffer;
		this.blockLengthInFloats = blockLengthInFloats;
		this.minSecsBeforePosition = minSecsBeforePosition;
		this.minSecsAfterPosition = minSecsAfterPosition;
	}

	public int floatPositionToBlockNumber( long floatPosition )
	{
		int remainder = (int)(floatPosition % blockLengthInFloats);
		return (int)(floatPosition / blockLengthInFloats) + ( remainder > 0 ? 1 : 0 );
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( "MaxBlocksToBuffer(");
		sb.append( maxBlocksToBuffer);
		sb.append( ") BlockLengthFloats(");
		sb.append( blockLengthInFloats );
		sb.append( ") minSecsBefore(" );
		sb.append( minSecsBeforePosition );
		sb.append( ") minSecsAfter(" );
		sb.append( minSecsAfterPosition );
		sb.append( ")");
		return sb.toString();
	}
}
