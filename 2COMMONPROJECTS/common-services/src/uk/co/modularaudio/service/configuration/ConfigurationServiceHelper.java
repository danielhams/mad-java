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

package uk.co.modularaudio.service.configuration;

import java.util.Arrays;
import java.util.Map;

import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

/**
 * <p>The ConfigurationServiceHelper is intended to allow convenient extraction of
 * configuration parameters and simplified error handling.</p>
 * <p>An example of usage:</p>
 * <pre>
 * {@code
 * Map<String,String> errors = new HashMap<String,String>();
 * String contentsDir = ConfigurationServiceHelper.checkForSingleStringKey( configService, "MyService.ContentsDir", errors );
 * boolean useThreads = ConfigurationServiceHelper.checkForSingleBooleanKey( configService, "MyService.UseThreads", errors );
 * ConfigurationServiceHelper.errorCheck(errors);
 * }
 * </pre>
 * <p>This will delay throwing any {@link RecordNotFoundException} until the
 * call to errorCheck and will throw a single exception which lists all
 * discovered errors.</p>
 *
 * @author dan
 *
 */
public final class ConfigurationServiceHelper
{
	private ConfigurationServiceHelper()
	{
	}

	public static String checkForSingleStringKey( final ConfigurationService configurationService, final String key, final Map<String,String> errors )
	{
		try
		{
			final String value = configurationService.getSingleStringValue( key );
			return value;
		}
		catch(final RecordNotFoundException rnfe )
		{
			errors.put(key, rnfe.toString() );
			return null;
		}
	}

	public static boolean checkForBooleanKey( final ConfigurationService configurationService, final String key, final Map<String,String> errors )
	{
		try
		{
			final boolean value = configurationService.getSingleBooleanValue( key );
			return value;
		}
		catch(final RecordNotFoundException rnfe )
		{
			errors.put(key, rnfe.toString() );
			return false;
		}
	}

	public static long checkForLongKey( final ConfigurationService configurationService,
			final String key,
			final Map<String, String> errors)
	{
		try
		{
			final long value = configurationService.getSingleLongValue( key );
			return value;
		}
		catch(final RecordNotFoundException rnfe )
		{
			errors.put(key, rnfe.toString() );
			return -1;
		}
	}

	public static String[] checkForCommaSeparatedStringValues( final ConfigurationService configurationService,
			final String key,
			final Map<String, String> errors )
	{
		try
		{
			final String[] value = configurationService.getCommaSeparatedStringValues( key );
			return value;
		}
		catch(final RecordNotFoundException rnfe )
		{
			errors.put(key, rnfe.toString() );
			return null;
		}
	}

	public static void errorCheck( final Map<String, String> errors ) throws ComponentConfigurationException
	{
		if( errors.size() > 0 )
		{
			final String msg = "Configuration errors: " + Arrays.toString( errors.values().toArray( new String[]{} ) );
			throw new ComponentConfigurationException( msg );
		}
	}

	public static int checkForIntKey( final ConfigurationService configurationService, final String key, final Map<String, String> errors )
	{
		try
		{
			final int value = configurationService.getSingleIntValue( key );
			return value;
		}
		catch(final RecordNotFoundException rnfe )
		{
			errors.put(key, rnfe.toString() );
			return -1;
		}
	}

	public static float checkForFloatKey( final ConfigurationService configurationService, final String key, final Map<String, String> errors )
	{
		try
		{
			final float value = configurationService.getSingleFloatValue( key );
			return value;
		}
		catch(final RecordNotFoundException rnfe )
		{
			errors.put(key, rnfe.toString() );
			return -1;
		}
	}


}
