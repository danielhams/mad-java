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

public class HardwareIOConfiguration
{
	private final int numHelperThreads;
	private final int fps;
	private final AudioHardwareDevice consumerAudioDevice;
	private final AudioHardwareDevice producerAudioDevice;
	private final MidiHardwareDevice consumerMidiDevice;
	private final MidiHardwareDevice producerMidiDevice;

	public HardwareIOConfiguration(
			final int numHelperThreads,
			final int fps,
			final AudioHardwareDevice consumerHardwareStream,
			final AudioHardwareDevice producerHardwareStream,
			final MidiHardwareDevice iConsumerMidiConfiguration,
			final MidiHardwareDevice iProducerMidiConfiguration )
	{
		this.numHelperThreads = numHelperThreads;
		this.fps = fps;
		this.consumerAudioDevice = consumerHardwareStream;
		this.producerAudioDevice = producerHardwareStream;
		this.consumerMidiDevice = iConsumerMidiConfiguration;
		this.producerMidiDevice = iProducerMidiConfiguration;
	}

	public int getNumHelperThreads()
	{
		return numHelperThreads;
	}

	public int getFps()
	{
		return fps;
	}

	public AudioHardwareDevice getConsumerAudioDevice()
	{
		return consumerAudioDevice;
	}

	public AudioHardwareDevice getProducerAudioDevice()
	{
		return producerAudioDevice;
	}

	public MidiHardwareDevice getConsumerMidiDevice()
	{
		return consumerMidiDevice;
	}

	public MidiHardwareDevice getProducerMidiDevice()
	{
		return producerMidiDevice;
	}

}
