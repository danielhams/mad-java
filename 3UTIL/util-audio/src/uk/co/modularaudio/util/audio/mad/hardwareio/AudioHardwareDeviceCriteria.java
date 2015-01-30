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
import uk.co.modularaudio.util.audio.format.SampleBits;

public class AudioHardwareDeviceCriteria
{
	private final SampleBits sampleBits;
	private final DataRate dataRate;
	private final int hardwareNumFramesPerBuffer;

	public AudioHardwareDeviceCriteria( final SampleBits sampleBits,
			final DataRate dataRate,
			final int hardwareNumFramesPerBuffer )
	{
		this.sampleBits = sampleBits;
		this.dataRate = dataRate;
		this.hardwareNumFramesPerBuffer = hardwareNumFramesPerBuffer;
	}

	public SampleBits getSampleBits()
	{
		return sampleBits;
	}

	public DataRate getDataRate()
	{
		return dataRate;
	}

	public int getHardwareNumFramesPerBuffer()
	{
		return hardwareNumFramesPerBuffer;
	}
}
