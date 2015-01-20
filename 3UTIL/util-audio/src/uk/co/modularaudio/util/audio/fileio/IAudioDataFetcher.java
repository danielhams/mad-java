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

package uk.co.modularaudio.util.audio.fileio;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

public interface IAudioDataFetcher
{
	enum DetectedFormat
	{
		NONE,
		MP3,
		FLAC
	};

	public abstract void open( File inputFile ) throws UnsupportedAudioFileException, IOException;

	public abstract void open(AudioFormat desiredAudioFormat, File inputFile) throws UnsupportedAudioFileException,
	IOException;

	public abstract int read(float[] destBuf, int destOffset, long startPos, int length) throws IOException,
			ArrayIndexOutOfBoundsException, UnsupportedAudioFileException;

	public abstract void close();

	public abstract long getNumTotalFloats();
//	public abstract long getNumTotalFrames();
	
	public DetectedFormat getDetectedFormat();

	public abstract int getNumChannels();

	public abstract int getSampleRate();

}
