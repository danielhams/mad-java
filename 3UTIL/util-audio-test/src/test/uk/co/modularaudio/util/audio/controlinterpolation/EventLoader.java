package test.uk.co.modularaudio.util.audio.controlinterpolation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class EventLoader
{
	public final static TestEvent[] loadEventsFromFile( final String fileName )
		throws IOException
	{
		final ArrayList<TestEvent> retArray = new ArrayList<TestEvent>();

		final BufferedReader br = new BufferedReader( new FileReader( new File(fileName) ) );

		try
		{

			String line;

			while( (line = br.readLine() ) != null )
			{
				final String[] vals = line.split( "," );
				final int sampleOffset = Integer.valueOf( vals[0] );
				final float sampleValue = Float.valueOf( vals[1] );

				final TestEvent te = new TestEvent( sampleOffset, sampleValue );
				retArray.add( te );
			}
		}
		finally
		{
			try
			{
				br.close();
			}
			catch(final Exception e )
			{
				// Only a test class
			}
		}

		return retArray.toArray( new TestEvent[retArray.size()] );
	}
}
