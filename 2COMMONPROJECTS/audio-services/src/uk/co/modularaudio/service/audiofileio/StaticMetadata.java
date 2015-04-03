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

package uk.co.modularaudio.service.audiofileio;

import uk.co.modularaudio.service.audiofileio.AudioFileIOService.AudioFileFormat;
import uk.co.modularaudio.util.audio.format.DataRate;
import uk.co.modularaudio.util.audio.format.SampleBits;

public class StaticMetadata
{
	public StaticMetadata( final AudioFileFormat format,
			final DataRate dataRate,
			final SampleBits sampleBits,
			final int numChannels,
			final long numFrames,
			final String path )
	{
		this.format = format;
		this.dataRate = dataRate;
		this.sampleBits = sampleBits;
		this.numChannels = numChannels;
		this.numFrames = numFrames;
		this.numFloats = numFrames * numChannels;
		this.path = path;
	}
	public final AudioFileFormat format;
	public final DataRate dataRate;
	public final SampleBits sampleBits;
	public final int numChannels;
	public final long numFrames;
	public final long numFloats;
	public final String path;
}
