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

import java.awt.Color;
import java.util.Date;

import uk.co.modularaudio.util.exception.RecordNotFoundException;

/**
 * @author dan
 *
 */
public interface ConfigurationService
{
	String getSingleStringValue( String key ) throws RecordNotFoundException;

	String getSingleStringValue( String key, String defaultValue );

	int getSingleIntValue( String key ) throws RecordNotFoundException;

	long getSingleLongValue( String key ) throws RecordNotFoundException;

	float getSingleFloatValue( String key ) throws RecordNotFoundException;

	String getSingleEncryptedStringValue( String key ) throws RecordNotFoundException;

	String[] getCommaSeparatedStringValues( String key ) throws RecordNotFoundException;

	boolean getSingleBooleanValue( String key ) throws RecordNotFoundException;

	boolean getSingleBooleanValue( String key, boolean defaultValue );

	double getSingleDoubleValue( String key ) throws RecordNotFoundException;

	String[] getKeysBeginningWith( String keyStart );

	Date getSingleDateValue( String key ) throws RecordNotFoundException;

	Color getSingleColorValue( String string ) throws RecordNotFoundException;

}
