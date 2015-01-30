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


public class MadChannelBuffer
{
	public float[] floatBuffer;
	public MadChannelNoteEvent[] noteBuffer;
	public final int bufferSize;
	public int numElementsInBuffer;

	public MadChannelBuffer( final MadChannelType channelType, final int iBufferSize )
	{
		bufferSize = iBufferSize;
		switch( channelType )
		{
			case AUDIO:
			case CV:
			{
				origFloatBuffer = new float[ bufferSize ];
				floatBuffer = origFloatBuffer;
				break;
			}
			case NOTE:
			{
				origNoteBuffer = new MadChannelNoteEvent[ bufferSize ];
				for( int i = 0 ; i < bufferSize ; i++ )
				{
					origNoteBuffer[ i ] = new MadChannelNoteEvent();
				}
				noteBuffer = origNoteBuffer;
				break;
			}
		}
	}

	private float[] origFloatBuffer;
	private MadChannelNoteEvent[] origNoteBuffer;
}
