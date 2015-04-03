package uk.co.modularaudio.service.audiofileioregistry;

import java.util.Set;

import javax.sound.sampled.UnsupportedAudioFileException;

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
}
