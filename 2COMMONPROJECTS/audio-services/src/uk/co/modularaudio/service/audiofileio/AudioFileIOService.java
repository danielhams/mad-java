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
	
	Set<AudioFileFormat> listSupportedEncodingFormats();
	Set<AudioFileFormat> listSupportedDecodingFormats();

	StaticMetadata sniffFileFormatOfFile( String path ) throws DatastoreException, RecordNotFoundException;
	
	AudioFileHandleAtom openForWrite( String path ) throws DatastoreException,  IOException;
	
	AudioFileHandleAtom openForRead( String path ) throws DatastoreException, IOException;
	
	void closeHandle( AudioFileHandleAtom handle ) throws DatastoreException, IOException;
	
	void readFloats( AudioFileHandleAtom handle, float[] destFloats, int destPosition, int numFrames, long frameReadOffset ) throws DatastoreException, IOException;
	
	void writeFloats( AudioFileHandleAtom handle, float[] srcFloats, long writePosition, int numFrames ) throws DatastoreException, IOException;
	
	// Some metadata methods I won't implement in Java
	DynamicMetadata readMetadata( AudioFileHandleAtom handle ) throws DatastoreException, IOException;
	void writeMetadata( AudioFileHandleAtom handle, DynamicMetadata outDynamicMetadata ) throws DatastoreException, IOException ;
	// .. readMetadata
	// .. writeMetadata
}
