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
import uk.co.modularaudio.service.audioanalysis.AnalysisFillCompletionListener;
import uk.co.modularaudio.service.audioanalysis.AudioAnalysisException;
import uk.co.modularaudio.service.audioanalysis.AudioAnalysisService;
import uk.co.modularaudio.util.audio.format.UnknownDataRateException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.spring.SpringComponentHelper;
import uk.co.modularaudio.util.spring.SpringContextHelper;

public class TestRunAudioAnalysis
{
	private static Log log = LogFactory.getLog( TestRunAudioAnalysis.class.getName() );

	private final GenericApplicationContext gac;

	private final AudioAnalysisService aas;

	public TestRunAudioAnalysis() throws RecordNotFoundException, DatastoreException, IOException, AudioAnalysisException, UnsupportedAudioFileException, UnknownDataRateException
	{
		// Setup components
		final List<SpringContextHelper> schs = new ArrayList<SpringContextHelper>();
//		schs.add();
		final SpringComponentHelper sch = new SpringComponentHelper( schs );
		gac = sch.makeAppContext();
		aas = gac.getBean( "audioAnalysisService", AudioAnalysisService.class );

		// Now do the tests
		final AnalysisFillCompletionListener acl = new AnalysisFillCompletionListener()
		{
			@Override
			public void receivePercentageComplete(final int percentageComplete)
			{
				log.debug( "Percentage complete: " + percentageComplete );
			}

			@Override
			public void receiveAnalysedData( final AnalysedData analysedData )
			{
				log.debug("Received data: " + analysedData.toString() );

			}

			@Override
			public void notifyAnalysisFailure()
			{
				log.debug("Received analysis failure");

			}
		};
		final AnalysedData analysedData1 = aas.analyseFile(
				"/home/dan/Music/CanLoseMusic/DJMixes/EricSneoLGT/LGT Podcast 99 master.mp3",
				acl );
		log.debug("Analysed data 1 contains: " + analysedData1.toString() );
		final AnalysedData analysedData2 = aas.analyseFile(
				"/home/dan/Music/PreferNotToLoseMusic/SetSources/Mp3Repository/20120504/3350150_Party_Party_Harvey_McKay_Remix.mp3",
				acl );
		log.debug("Analysed data 2 contains: " + analysedData2.toString() );
		final AnalysedData analysedData10 = aas.analyseFile(
				"/home/dan/Music/PreferNotToLoseMusic/SetSources/Mp3Repository/20131121/4713773_Burning_Bright_feat__Kim_Ann_Foxman_Dense___Pika_Remix.mp3",
				acl );
		log.debug("Analysed data 10 contains: " + analysedData10.toString() );

	}

	public static void main( final String args[] ) throws RecordNotFoundException, DatastoreException, IOException, AudioAnalysisException, UnsupportedAudioFileException, UnknownDataRateException
	{
		final TestRunAudioAnalysis traa = new TestRunAudioAnalysis();
		log.debug("Run completed on " + traa.toString() );
	}
}
