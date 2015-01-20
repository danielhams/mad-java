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

package uk.co.modularaudio.service.jnajackaudioprovider;

import java.util.Arrays;

import javax.sound.midi.InvalidMidiDataException;

import uk.co.modularaudio.util.audio.midi.MidiMessage;

public class JNAJackMIDIMessage extends MidiMessage
{
	public JNAJackMIDIMessage()
	{
		super( new byte[3] );
	}
	
	public JNAJackMIDIMessage( byte[] data )
	{
		super( data );
	}

	public void setBytes( byte[] byteArray ) throws InvalidMidiDataException
	{
		if( byteArray.length <= 3 )
		{
			Arrays.fill( messageData, (byte)0 );
			System.arraycopy( byteArray, 0, messageData, 0, byteArray.length );
		}
		else
		{
			throw new InvalidMidiDataException( "MidiProcessing only accepts up to three bytes of data" );
		}
		
	}
	
	public byte[] getBuffer()
	{
		return messageData;
	}
}
