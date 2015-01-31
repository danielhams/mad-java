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
 * <p>Definition of a service providing configuration information (key value store)
 * via some mechanism.</p>
 *
 * @author dan
 *
 */
public interface ConfigurationService
{
	/**
	 * Obtain the string value for a particular key
	 * @param key
	 * @return string value
	 * @throws RecordNotFoundException if key missing
	 */
	String getSingleStringValue( String key ) throws RecordNotFoundException;

	/**
	 * Obtain the string value for a particular key using a default value
	 * if none exists
	 * @param key
	 * @param defaultValue
	 * @return value or default if missing
	 */
	String getSingleStringValue( String key, String defaultValue );

	/**
	 * Obtain an int value for a particular key
	 * @param key
	 * @return int value
	 * @throws RecordNotFoundException if key missing
	 */
	int getSingleIntValue( String key ) throws RecordNotFoundException;

	/**
	 * Obtain a long value for a particular key
	 * @param key
	 * @return long value
	 * @throws RecordNotFoundException if key missing
	 */
	long getSingleLongValue( String key ) throws RecordNotFoundException;

	/**
	 * Obtain a 32 bit floating point value for a particular key
	 * @param key
	 * @return float value
	 * @throws RecordNotFoundException if key missing
	 */
	float getSingleFloatValue( String key ) throws RecordNotFoundException;

	/**
	 * Obtain the decrypted string contents of a value
	 * base64 encrypted within the key value store for a particular key
	 * @param key
	 * @return plaintext decrypted value
	 * @throws RecordNotFoundException if key missing
	 */
	String getSingleEncryptedStringValue( String key ) throws RecordNotFoundException;

	/**
	 * Obtain an array of string values for a particular key where
	 * the value is a comma separated list
	 * @param key
	 * @return string array
	 * @throws RecordNotFoundException if key missing
	 */
	String[] getCommaSeparatedStringValues( String key ) throws RecordNotFoundException;

	/**
	 * Obtain a boolean value for a particular key
	 * @param key
	 * @return boolean value
	 * @throws RecordNotFoundException if key missing
	 */
	boolean getSingleBooleanValue( String key ) throws RecordNotFoundException;

	/**
	 * Obtain a boolean value for a particular key using a
	 * default if no value exists
	 * @param key
	 * @param defaultValue
	 * @return boolean value
	 */
	boolean getSingleBooleanValue( String key, boolean defaultValue );

	/**
	 * Obtain a 64 bit floating point value for a particular key
	 * @param key
	 * @return double floating point value
	 * @throws RecordNotFoundException if key missing
	 */
	double getSingleDoubleValue( String key ) throws RecordNotFoundException;

	/**
	 * Obtain a list of all knows keys that begin with a particular prefx
	 * @param keyStart prefix to use when matching keys
	 * @return an array of known keys
	 */
	String[] getKeysBeginningWith( String keyStart );

	/**
	 * Obtain a date value for a particular key
	 * @param key
	 * @return java date value
	 * @throws RecordNotFoundException if key missing
	 */
	Date getSingleDateValue( String key ) throws RecordNotFoundException;

	/**
	 * Obtain a colour value for a particular key
	 * @param string
	 * @return java colour value
	 * @throws RecordNotFoundException if key missing
	 */
	Color getSingleColorValue( String string ) throws RecordNotFoundException;

}
