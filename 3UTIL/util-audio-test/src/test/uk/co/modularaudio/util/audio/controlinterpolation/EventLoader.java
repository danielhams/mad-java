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
