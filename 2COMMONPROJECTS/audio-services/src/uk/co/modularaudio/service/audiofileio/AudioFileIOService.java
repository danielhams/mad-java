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

import java.io.IOException;
import java.util.Set;

import javax.sound.sampled.UnsupportedAudioFileException;

import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public interface AudioFileIOService
{
	public enum AudioFileFormat
	{
		UNKNOWN,
		WAV,
		AIFF,
		FLAC,
		OGG,
		MP3
	};

	public enum AudioFileDirection
	{
		ENCODE,
		DECODE
	};

	int getFormatSniffPriority();

	Set<AudioFileFormat> listSupportedEncodingFormats();
	Set<AudioFileFormat> listSupportedDecodingFormats();

	AudioFileFormat sniffFileFormatOfFile( String path ) throws DatastoreException, RecordNotFoundException, UnsupportedAudioFileException;

	AudioFileHandleAtom openForWrite( String absPath ) throws DatastoreException,  IOException, UnsupportedAudioFileException;

	AudioFileHandleAtom openForRead( String absPath ) throws DatastoreException, IOException, UnsupportedAudioFileException;

	void closeHandle( AudioFileHandleAtom handle ) throws DatastoreException, IOException;

	int readFrames( AudioFileHandleAtom handle, float[] destFloats, int destPositionFrames, int numFrames, long frameReadOffset ) throws DatastoreException, IOException;

	int writeFrames( AudioFileHandleAtom handle, float[] srcFloats, long srcPositionFrames, int numFrames ) throws DatastoreException, IOException;

	// Some metadata methods I won't implement in Java
	DynamicMetadata readMetadata( AudioFileHandleAtom handle ) throws DatastoreException, IOException;
	void writeMetadata( AudioFileHandleAtom handle, DynamicMetadata outDynamicMetadata ) throws DatastoreException, IOException ;
}
