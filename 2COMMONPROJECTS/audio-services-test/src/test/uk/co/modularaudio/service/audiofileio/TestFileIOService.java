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

package test.uk.co.modularaudio.service.audiofileio;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.audiofileio.AudioFileHandleAtom;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService.AudioFileFormat;
import uk.co.modularaudio.service.audiofileio.DynamicMetadata;
import uk.co.modularaudio.service.audiofileio.StaticMetadata;
import uk.co.modularaudio.service.brokenaudiofileio.BrokenAudioFileIOService;

public class TestFileIOService extends TestCase
{
	private static Log log = LogFactory.getLog( TestFileIOService.class.getName());

	private static final int TMP_ARRAY_SIZE = 1024;

	public void testReadingAFile() throws Exception
	{
		log.debug("Here we go.");

		final BrokenAudioFileIOService testService = new BrokenAudioFileIOService();

		testService.init();

		final AudioFileIOService serviceInterface = testService;

//		String fileToRead = "/music/Samples/House/VocalStabs/haha.flac";
//		String fileToRead = "/home/dan/tmp/TestExampleBeats96000.wav";
//		String fileToRead = "/home/dan/tmp/TestExampleBeats48000.mp3";
		final String fileToRead = "/home/dan/Music/PreferNotToLoseMusic/SetSources/Mp3Repository/200911/974684_She_Came_Along_feat__Kid_Cudi_Sharam_s_Ecstasy_Of_Ibiza_Edit.mp3";

		final AudioFileFormat sniffedFormat = serviceInterface.sniffFileFormatOfFile( fileToRead );

		assert( sniffedFormat != AudioFileFormat.UNKNOWN );

		final AudioFileHandleAtom readAtom = serviceInterface.openForRead( fileToRead );

		final StaticMetadata sm = readAtom.getStaticMetadata();

		final DynamicMetadata metadata = serviceInterface.readMetadata( readAtom );
		log.debug("Found the title: " + metadata.title );

		final int numChannels = sm.numChannels;

		final float[] destArray = new float[ TMP_ARRAY_SIZE ];

		serviceInterface.readFrames( readAtom, destArray, 0,  TMP_ARRAY_SIZE / numChannels, 0 );

		serviceInterface.closeHandle(  readAtom );

		testService.destroy();
	}
}
