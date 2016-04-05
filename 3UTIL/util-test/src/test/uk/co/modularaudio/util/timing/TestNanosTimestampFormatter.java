package test.uk.co.modularaudio.util.timing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import uk.co.modularaudio.util.timing.NanosTimestampFormatter;

public class TestNanosTimestampFormatter
{
	private static Log log = LogFactory.getLog( TestNanosTimestampFormatter.class.getName() );

	@Test
	public void test()
	{
		final long zeros = 0;
		final String zerosString = NanosTimestampFormatter.formatTimestampForLogging( zeros, true );

		log.trace( "For zeros got the string '" + zerosString + "'" );

		final long timestampValue = System.nanoTime();
		final String timestampString = NanosTimestampFormatter.formatTimestampForLogging( timestampValue, true );

		log.trace( "For the value " + timestampValue + " got '" + timestampString + "'" );


	}

}
