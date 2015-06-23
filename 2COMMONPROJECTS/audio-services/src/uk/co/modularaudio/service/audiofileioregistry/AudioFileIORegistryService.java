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

package uk.co.modularaudio.service.audiofileioregistry;

import java.io.IOException;
import java.util.Set;

import javax.sound.sampled.UnsupportedAudioFileException;

import uk.co.modularaudio.service.audiofileio.AudioFileHandleAtom;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService.AudioFileDirection;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService.AudioFileFormat;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public interface AudioFileIORegistryService
{
	void registerAudioFileIOService( AudioFileIOService audioFileIOService );
	void unregisterAudioFileIOService( AudioFileIOService audioFileIOService );

	AudioFileIOService getAudioFileIOServiceForFormatAndDirection(
			AudioFileFormat format,
			AudioFileDirection direction )
		throws DatastoreException, RecordNotFoundException, UnsupportedAudioFileException;
	AudioFileFormat sniffFileFormatOfFile( String path )
		throws DatastoreException, RecordNotFoundException, UnsupportedAudioFileException;
	Set<AudioFileFormat> listSupportedEncodingFormats();
	Set<AudioFileFormat> listSupportedDecodingFormats();

	AudioFileHandleAtom openFileForRead( String path )
		throws DatastoreException, IOException, UnsupportedAudioFileException;

	// To allow services to use relative paths for files that live under the
	// user specified music dir.
	String getUserMusicDir();
}
