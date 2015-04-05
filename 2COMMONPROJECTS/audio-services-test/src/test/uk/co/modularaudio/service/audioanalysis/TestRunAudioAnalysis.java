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

package test.uk.co.modularaudio.service.audioanalysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericApplicationContext;

import uk.co.modularaudio.service.audioanalysis.AnalysedData;
import uk.co.modularaudio.service.audioanalysis.AudioAnalysisException;
import uk.co.modularaudio.service.audioanalysis.AudioAnalysisService;
import uk.co.modularaudio.util.audio.format.UnknownDataRateException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.spring.SpringComponentHelper;
import uk.co.modularaudio.util.spring.SpringContextHelper;
import uk.co.modularaudio.util.unitOfWork.ProgressListener;

public class TestRunAudioAnalysis
{
	private static Log log = LogFactory.getLog( TestRunAudioAnalysis.class.getName() );

	private GenericApplicationContext gac = null;

	private AudioAnalysisService aas = null;

	public TestRunAudioAnalysis() throws RecordNotFoundException, DatastoreException, IOException, AudioAnalysisException, UnsupportedAudioFileException, UnknownDataRateException
	{
		// Setup components
		final List<SpringContextHelper> schs = new ArrayList<SpringContextHelper>();
//		schs.add();
		final SpringComponentHelper sch = new SpringComponentHelper( schs );
		gac = sch.makeAppContext();
		aas = gac.getBean( "audioAnalysisService", AudioAnalysisService.class );

		// Now do the tests
		final ProgressListener pl = new ProgressListener()
		{

			@Override
			public void receivePercentageComplete(final String statusMessage, final int percentageComplete)
			{
				log.debug( statusMessage + " - percentage complete: " + percentageComplete );
			}
		};
//		AnalysedData analysedData = aas.analyseFile( "testfiles/wavs/hc.wav", pl );
//		log.debug("Analysed data contains: " + analysedData.toString() );
//		AnalysedData analysedData2 = aas.analyseFile( "testfiles/wavs/examplebeats.wav", pl );
//		log.debug("Analysed data 2 contains: " + analysedData2.toString() );
//		AnalysedData analysedData3 = aas.analyseFile( "testfiles/wavs/hc.mp3", pl );
//		log.debug("Analysed data 3 contains: " + analysedData3.toString() );
//		AnalysedData analysedData4 = aas.analyseFile( "testfiles/wavs/sr.mp3", pl );
//		log.debug("Analysed data 4 contains: " + analysedData4.toString() );
//		AnalysedData analysedData5 = aas.analyseFile( "testfiles/wavs/ds.wav", pl );
//		log.debug("Analysed data 5 contains: " + analysedData5.toString() );
//		AnalysedData analysedData6 = aas.analyseFile( "testfiles/wavs/tfiy.mp3", pl );
//		log.debug("Analysed data 6 contains: " + analysedData6.toString() );
//		AnalysedData analysedData7 = aas.analyseFile( "testfiles/wavs/freqshift112.mp3", pl );
//		log.debug("Analysed data 7 contains: " + analysedData7.toString() );
//		AnalysedData analysedData8 = aas.analyseFile( "/media/663099F83099D003/Music/Mp3Repository/20131121/4569954_Collision_Original_Mix.mp3", pl );
//		log.debug("Analysed data 8 contains: " + analysedData8.toString() );
//		AnalysedData analysedData9 = aas.analyseFile( "/media/663099F83099D003/Music/Mp3Repository/20131121/4820093_Black_Deep_Original_Mix.mp3", pl );
//		log.debug("Analysed data 9 contains: " + analysedData9.toString() );
		final AnalysedData analysedData10 = aas.analyseFile( "/home/dan/Music/PreferNotToLoseMusic/SetSources/Mp3Repository/20131121/4713773_Burning_Bright_feat__Kim_Ann_Foxman_Dense___Pika_Remix.mp3", pl );
		log.debug("Analysed data 10 contains: " + analysedData10.toString() );

	}

	public static void main( final String args[] ) throws RecordNotFoundException, DatastoreException, IOException, AudioAnalysisException, UnsupportedAudioFileException, UnknownDataRateException
	{
		final TestRunAudioAnalysis traa = new TestRunAudioAnalysis();
		log.debug("Run completed on " + traa.toString() );
	}
}
