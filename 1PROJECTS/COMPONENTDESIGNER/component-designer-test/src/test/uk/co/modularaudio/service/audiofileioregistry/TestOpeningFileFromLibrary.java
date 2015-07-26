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

package test.uk.co.modularaudio.service.audiofileioregistry;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.context.support.GenericApplicationContext;

import test.uk.co.modularaudio.service.TestConstants;
import uk.co.modularaudio.componentdesigner.ComponentDesigner;
import uk.co.modularaudio.controller.hibsession.HibernateSessionController;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.SampleCachingService;
import uk.co.modularaudio.util.hibernate.ThreadLocalSessionResource;

public class TestOpeningFileFromLibrary extends TestCase
{
	private static Log log = LogFactory.getLog( TestOpeningFileFromLibrary.class.getName() );

	private final static String TEST_FILE_NAME = "../../../5TEST/audio-test-files/audiofiles/ExampleBeats.mp3";

	private static final long SLEEP_AFTER_FIRST_OPEN_MILLIS = 500;
	private static final long SLEEP_AFTER_SECOND_OPEN_MILLIS = 500;

	private final ComponentDesigner componentDesigner;
	private GenericApplicationContext applicationContext;

	private HibernateSessionController hsc;
	private SampleCachingService scs;

	public TestOpeningFileFromLibrary()
	{
		componentDesigner = new ComponentDesigner();
	}

	@Override
	protected void setUp() throws Exception
	{
		componentDesigner.setupApplicationContext( TestConstants.CDTEST_PROPERTIES,
				null, null,
				true, true );

		applicationContext = componentDesigner.getApplicationContext();

		// Grab the necessary controller references
		hsc = applicationContext.getBean( HibernateSessionController.class );
		scs = applicationContext.getBean( SampleCachingService.class );
	}

	@Override
	protected void tearDown() throws Exception
	{
		componentDesigner.destroyApplicationContext();
	}

	public void testReadSomeFiles() throws Exception
	{
		// Obtain a hibernate session
		hsc.getThreadSession();
		Session tls = ThreadLocalSessionResource.getSessionResource();
		Transaction t = tls.beginTransaction();

		log.debug("-- FIRST OPEN BEGIN");
		final long nb = System.nanoTime();
		final File testFile = new File(TEST_FILE_NAME);
		final SampleCacheClient scc = scs.registerCacheClientForFile( testFile.getAbsolutePath() );
		final long na = System.nanoTime();
		final long diff = na - nb;
		log.debug("-- FIRST OPEN END");

		t.commit();
		hsc.releaseThreadSessionNoException();

		Thread.sleep( SLEEP_AFTER_FIRST_OPEN_MILLIS );

		hsc.getThreadSession();
		tls = ThreadLocalSessionResource.getSessionResource();
		t = tls.beginTransaction();

		log.debug("-- SECOND OPEN BEGIN");
		final long nb2 = System.nanoTime();
		final SampleCacheClient scc2 = scs.registerCacheClientForFile( testFile.getAbsolutePath() );
		final long na2 = System.nanoTime();
		final long diff2 = na2 - nb2;
		log.debug("-- SECOND OPEN END");

		t.commit();
		hsc.releaseThreadSessionNoException();

		Thread.sleep( SLEEP_AFTER_SECOND_OPEN_MILLIS );

		scs.unregisterCacheClientForFile( scc );
		scs.unregisterCacheClientForFile( scc2 );


		log.debug("First  file took " + diff + "ns or " + (diff/1000) + "us or " + (diff/1000000) + "ms");
		log.debug("Second file took " + diff2 + "ns or " + (diff2/1000) + "us or " + (diff2/1000000) + "ms");
	}
}
