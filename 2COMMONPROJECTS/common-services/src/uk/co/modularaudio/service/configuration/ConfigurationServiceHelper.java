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
import java.util.HashMap;
import java.util.Map;

import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class ConfigurationServiceHelper
{
	private ConfigurationServiceHelper()
	{
	}
	
	public static String checkForSingleStringKey( ConfigurationService configurationService, String key, Map<String,String> errors )
	{
		try
		{
			String value = configurationService.getSingleStringValue( key );
			return value;
		}
		catch(RecordNotFoundException rnfe )
		{
			errors.put(key, rnfe.toString() );
			return null;
		}
	}

	public static boolean checkForBooleanKey( ConfigurationService configurationService, String key, Map<String,String> errors )
	{
		try
		{
			boolean value = configurationService.getSingleBooleanValue( key );
			return value;
		}
		catch(RecordNotFoundException rnfe )
		{
			errors.put(key, rnfe.toString() );
			return false;
		}
	}

	public static long checkForLongKey( ConfigurationService configurationService,
			String key, HashMap<String, String> errors)
	{
		try
		{
			long value = configurationService.getSingleLongValue( key );
			return value;
		}
		catch(RecordNotFoundException rnfe )
		{
			errors.put(key, rnfe.toString() );
			return -1;
		}
	}

	public static String[] checkForCommaSeparatedStringValues( ConfigurationService configurationService,
			String key,
			Map<String, String> errors )
	{
		try
		{
			String[] value = configurationService.getCommaSeparatedStringValues( key );
			return value;
		}
		catch(RecordNotFoundException rnfe )
		{
			errors.put(key, rnfe.toString() );
			return null;
		}
	}
	
	public static void errorCheck( Map<String, String> errors ) throws ComponentConfigurationException
	{
		if( errors.size() > 0 )
		{
			String msg = "Configuration errors: " + Arrays.toString( errors.values().toArray( new String[]{} ) );
			throw new ComponentConfigurationException( msg );
		}
	}

	public static int checkForIntKey( ConfigurationService configurationService, String key, Map<String, String> errors )
	{
		try
		{
			int value = configurationService.getSingleIntValue( key );
			return value;
		}
		catch(RecordNotFoundException rnfe )
		{
			errors.put(key, rnfe.toString() );
			return -1;
		}
	}
	
	public static float checkForFloatKey( ConfigurationService configurationService, String key, Map<String, String> errors )
	{
		try
		{
			float value = configurationService.getSingleFloatValue( key );
			return value;
		}
		catch(RecordNotFoundException rnfe )
		{
			errors.put(key, rnfe.toString() );
			return -1;
		}
	}


}
