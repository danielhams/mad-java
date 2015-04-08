package test.uk.co.modularaudio.service.audiofileioregistry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.context.support.GenericApplicationContext;

import uk.co.modularaudio.componentdesigner.ComponentDesigner;
import uk.co.modularaudio.controller.hibsession.HibernateSessionController;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.SampleCachingService;
import uk.co.modularaudio.util.hibernate.ThreadLocalSessionResource;

public class TestOpeningFileFromLibrary
{
	private static Log log = LogFactory.getLog( TestOpeningFileFromLibrary.class.getName() );

	private final static String TEST_FILE = "/home/dan/Music/CanLoseMusic/DJMixes/EricSneoLGT/LGTP001_ERIC_SNEO.mp3";

	private static final long SLEEP_AFTER_FIRST_OPEN_MILLIS = 500;
	private static final long SLEEP_AFTER_SECOND_OPEN_MILLIS = 500;

	private final ComponentDesigner componentDesigner;
	private GenericApplicationContext applicationContext;

	private HibernateSessionController hsc;
	private SampleCachingService scs;

	public TestOpeningFileFromLibrary() throws Exception
	{
		componentDesigner = new ComponentDesigner();
	}

	public void go() throws Exception
	{
		componentDesigner.setupApplicationContext( true, true, null, null );

		applicationContext = componentDesigner.getApplicationContext();

		// Grab the necessary controller references
		hsc = applicationContext.getBean( HibernateSessionController.class );
		scs = applicationContext.getBean( SampleCachingService.class );

		// Obtain a hibernate session
		hsc.getThreadSession();
		Session tls = ThreadLocalSessionResource.getSessionResource();
		Transaction t = tls.beginTransaction();

		log.debug("-- FIRST OPEN BEGIN");
		final long nb = System.nanoTime();
		final SampleCacheClient scc = scs.registerCacheClientForFile( TEST_FILE );
		final long na = System.nanoTime();
		final long diff = na - nb;
		log.debug("-- FIRST OPEN END");

		t.commit();
		hsc.releaseThreadSessionNoException();

		hsc.getThreadSession();
		tls = ThreadLocalSessionResource.getSessionResource();
		t = tls.beginTransaction();

		log.debug("-- SECOND OPEN BEGIN");
		final long nb2 = System.nanoTime();
		final SampleCacheClient scc2 = scs.registerCacheClientForFile( TEST_FILE );
		final long na2 = System.nanoTime();
		final long diff2 = na2 - nb2;
		log.debug("-- SECOND OPEN END");

		t.commit();
		hsc.releaseThreadSessionNoException();

		Thread.sleep( SLEEP_AFTER_FIRST_OPEN_MILLIS );

		scs.unregisterCacheClientForFile( scc );
		scs.unregisterCacheClientForFile( scc2 );


		log.debug("First  file took " + diff + "ns or " + (diff/1000) + "us or " + (diff/1000000) + "ms");
		log.debug("Second file took " + diff2 + "ns or " + (diff2/1000) + "us or " + (diff2/1000000) + "ms");

		componentDesigner.destroyApplicationContext();
	}

	public static void main( final String[] args ) throws Exception
	{
		final TestOpeningFileFromLibrary offl = new TestOpeningFileFromLibrary();

		offl.go();
	}

}
