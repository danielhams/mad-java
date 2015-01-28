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

package uk.co.modularaudio.util.audio.mad.hardwareio;

import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelType;

public class IOBuffers
{
	public final int numAudioBuffers;
	public final int audioBufferLength;
	public final int numMidiBuffers;
	public final int noteBufferLength;
	public final MadChannelBuffer[] audioBuffers;
	public final MadChannelBuffer[] noteBuffers;

	public IOBuffers( final int numAudioBuffers,
			final int audioBufferLength,
			final int numNoteBuffers,
			final int noteBufferLength )
	{
		this.numAudioBuffers = numAudioBuffers;
		this.audioBufferLength = audioBufferLength;
		audioBuffers = new MadChannelBuffer[ numAudioBuffers ];
		for( int i = 0 ; i < numAudioBuffers ; i++ )
		{
			audioBuffers[ i ] = new MadChannelBuffer( MadChannelType.AUDIO, audioBufferLength );
		}
		this.numMidiBuffers = numNoteBuffers;
		this.noteBufferLength = noteBufferLength;
		noteBuffers = new MadChannelBuffer[ numNoteBuffers ];
		for( int n = 0 ; n < numNoteBuffers ; n++ )
		{
			noteBuffers[ n ] = new MadChannelBuffer( MadChannelType.NOTE, noteBufferLength );
		}
	}
}
