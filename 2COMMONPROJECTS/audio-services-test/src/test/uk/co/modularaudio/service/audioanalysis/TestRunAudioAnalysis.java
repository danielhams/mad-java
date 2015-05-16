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

import uk.co.modularaudio.controller.hibsession.HibernateSessionController;
import uk.co.modularaudio.service.audioanalysis.AnalysedData;
import uk.co.modularaudio.service.audioanalysis.AnalysisFillCompletionListener;
import uk.co.modularaudio.service.audioanalysis.AudioAnalysisException;
import uk.co.modularaudio.service.audioanalysis.AudioAnalysisService;
import uk.co.modularaudio.service.audiofileio.AudioFileHandleAtom;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService.AudioFileDirection;
import uk.co.modularaudio.service.audiofileio.AudioFileIOService.AudioFileFormat;
import uk.co.modularaudio.service.audiofileioregistry.AudioFileIORegistryService;
import uk.co.modularaudio.service.library.LibraryEntry;
import uk.co.modularaudio.service.library.LibraryService;
import uk.co.modularaudio.util.audio.format.UnknownDataRateException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.hibernate.NoSuchHibernateSessionException;
import uk.co.modularaudio.util.spring.SpringComponentHelper;
import uk.co.modularaudio.util.spring.SpringContextHelper;
import uk.co.modularaudio.util.springhibernate.SpringHibernateContextHelper;

public class TestRunAudioAnalysis
{
	private static Log log = LogFactory.getLog( TestRunAudioAnalysis.class.getName() );

	private final GenericApplicationContext gac;

	private final AudioAnalysisService aas;
	private final AudioFileIORegistryService afirs;
	private final LibraryService ls;
	private final HibernateSessionController hsc;

	public TestRunAudioAnalysis()
		throws RecordNotFoundException, DatastoreException, IOException,
		AudioAnalysisException, UnsupportedAudioFileException, UnknownDataRateException,
		NoSuchHibernateSessionException, MAConstraintViolationException
	{
		// Setup components
		final List<SpringContextHelper> schs = new ArrayList<SpringContextHelper>();
		schs.add( new SpringHibernateContextHelper());
		final SpringComponentHelper sch = new SpringComponentHelper( schs );
		gac = sch.makeAppContext();
		gac.start();
		aas = gac.getBean( "audioAnalysisService", AudioAnalysisService.class );
		afirs = gac.getBean( "audioFileIORegistryService", AudioFileIORegistryService.class );
		ls = gac.getBean( "libraryService", LibraryService.class );
		hsc = gac.getBean( "hibernateSessionController", HibernateSessionController.class );

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

			@Override
			public void receiveAnalysisBegin()
			{
				log.debug("Received analysis begin");
			}
		};

//		final String filePath = "/home/dan/Music/PreferNotToLoseMusic/SetSources/Mp3Repository/20131121/4713773_Burning_Bright_feat__Kim_Ann_Foxman_Dense___Pika_Remix.mp3";
//		final String filePath = "/home/dan/Music/PreferNotToLoseMusic/SetSources/Mp3Repository/20131121/4569954_Collision_Original_Mix.mp3";
//		final String filePath = "/home/dan/Music/CanLoseMusic/Albums/LFO - Frequencies (1991)/03 - Simon From Sydney.mp3";
		final String filePath = "/home/dan/Music/CanLoseMusic/Albums/LFO - Frequencies (1991)/11 - Mentok 1.mp3";
//		final String filePath = "/home/dan/Music/CanLoseMusic/Albums/Regular/ORB - The Orb's Adventures Beyond The Ultraworld/CD 1/02 - Earth Orbit Two- Earth (Gaia).flac";
//		final String filePath = "/home/dan/Music/CanLoseMusic/Albums/Regular/ORB - The Orb's Adventures Beyond The Ultraworld/CD 1/01 - Earth Orbit One- Little Fluffy Clouds.flac";
//		final String filePath = "/home/dan/Music/CanLoseMusic/Albums/Regular/Bobby McFerrin/Simple Pleasures/07 - Susie-Q.flac";
//		final String filePath = "/home/dan/Music/PreferNotToLoseMusic/SetSources/Mp3Repository/20131121/4823846_Bengala_Joel_Mull_Remix.mp3";
//		final String filePath = "/home/dan/Music/PreferNotToLoseMusic/SetSources/Mp3Repository/20131121/4820093_Black_Deep_Original_Mix.mp3";
//		final String filePath = "/home/dan/Music/PreferNotToLoseMusic/SetSources/Mp3Repository/200903/139230_True__The_Faggot_Is_You__Deep_Dish_Poof_Daddy_Remix.mp3";
//		final String filePath = "/home/dan/Music/PreferNotToLoseMusic/SetSources/Mp3Repository/200903/139229_True__The_Faggot_Is_You__Deep_Dish_Poof_Daddy_Dub.mp3";
//		final String filePath = "/home/dan/Music/CanLoseMusic/DJMixes/EricSneoLGT/LGT Podcast 101 master.mp3";

		final AudioFileFormat format = afirs.sniffFileFormatOfFile( filePath );

		final AudioFileIOService decoderService = afirs.getAudioFileIOServiceForFormatAndDirection( format,
				AudioFileDirection.DECODE );

		final AudioFileHandleAtom afha = decoderService.openForRead( filePath );

		LibraryEntry le = null;

		hsc.getThreadSession();

		try
		{
			le = ls.findLibraryEntryByAudioFile( afha );
		}
		catch( final RecordNotFoundException rnfe )
		{
			le = ls.addAudioFileToLibrary( afha );
		}

		final AnalysedData analysedData = aas.analyseFileHandleAtom( le, afha, acl );

		hsc.releaseThreadSession();

		gac.destroy();

		log.debug("Analysed data contains: " + analysedData.toString() );

	}

	public static void main( final String args[] )
		throws RecordNotFoundException, DatastoreException, IOException,
		AudioAnalysisException, UnsupportedAudioFileException, UnknownDataRateException,
		NoSuchHibernateSessionException, MAConstraintViolationException
	{
		final TestRunAudioAnalysis traa = new TestRunAudioAnalysis();
		log.debug("Run completed on " + traa.toString() );
	}
}
