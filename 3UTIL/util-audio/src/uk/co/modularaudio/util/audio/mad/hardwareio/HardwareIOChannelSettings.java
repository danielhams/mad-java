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

import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.mad.MadChannelType;

public class HardwareIOChannelSettings
{
	private final static DataRate STANDARD_DATA_RATE = DataRate.SR_44100;
	private final static int STANDARD_BUF_LENGTH = 1024;
	private final static int STANDARD_NOTE_BUFFER_LENGTH_AT_44100_1024 = 512;
	private final HardwareIOOneChannelSetting audioChannelSetting;
	private final HardwareIOOneChannelSetting noteChannelSetting;

	private final long nanosOutputLatency;
	private final int sampleFramesOutputLatency;

	public HardwareIOChannelSettings( final HardwareIOOneChannelSetting coreEngineAudioChannelSetting,
			final long nanosOutputLatency,
			final int sampleFramesOutputLatency )
	{
		audioChannelSetting = coreEngineAudioChannelSetting;
		this.nanosOutputLatency = nanosOutputLatency;
		this.sampleFramesOutputLatency = sampleFramesOutputLatency;

		// Note data is transmitted at 22khz
		final DataRate defaultNoteDataRate = DataRate.SR_22050;

		final float ratio = ((STANDARD_DATA_RATE.getValue() / (float)STANDARD_BUF_LENGTH)) /
				(coreEngineAudioChannelSetting.getDataRate().getValue() / (float)coreEngineAudioChannelSetting.getChannelBufferLength() );

		int noteDataBufferLengthAsInt = (int)(STANDARD_NOTE_BUFFER_LENGTH_AT_44100_1024 * ratio);
		noteDataBufferLengthAsInt = (noteDataBufferLengthAsInt < STANDARD_NOTE_BUFFER_LENGTH_AT_44100_1024 ? STANDARD_NOTE_BUFFER_LENGTH_AT_44100_1024 : noteDataBufferLengthAsInt );

		noteChannelSetting = new HardwareIOOneChannelSetting( defaultNoteDataRate, noteDataBufferLengthAsInt );
	}

	public HardwareIOOneChannelSetting getAudioChannelSetting()
	{
		return audioChannelSetting;
	}

	public HardwareIOOneChannelSetting getNoteChannelSetting()
	{
		return noteChannelSetting;
	}

	public int getChannelBufferLengthForChannelType( final MadChannelType iType )
	{
		switch( iType )
		{
			case AUDIO:
			case CV:
			{
				return audioChannelSetting.getChannelBufferLength();
			}
			case NOTE:
			{
				return noteChannelSetting.getChannelBufferLength();
			}
			default:
			{
				return -1;
			}
		}
	}

	public long getNanosOutputLatency()
	{
		return nanosOutputLatency;
	}

	public int getSampleFramesOutputLatency()
	{
		return sampleFramesOutputLatency;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("AudioChannelSetting: ");
		sb.append( audioChannelSetting.toString() );
		sb.append(" NoteChannelSetting: ");
		sb.append( noteChannelSetting.toString() );
		sb.append(" NanosOutputLatency: ");
		sb.append( nanosOutputLatency );
		sb.append(" SampleFramesOutputLatency: ");
		sb.append( sampleFramesOutputLatency );

		return sb.toString();
	}
}
