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

package uk.co.modularaudio.service.configuration.impl;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.date.DateConverter;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

/**
 * @author dan
 *
 */
public class ConfigurationServiceImpl implements ConfigurationService, ComponentWithLifecycle
{
	private static Log log = LogFactory.getLog(ConfigurationServiceImpl.class.getName());

	private String propertyFile = null;
	private String[] additionalPropertyFiles = null;

	private HashMap<String,String> keyToValueMap = null;
	private HashSet<String> usedKeys = null;

	private final static String b64EncryptionKey = "eUg0lWOSVA2vSgN/OcDz8Q==";

	private SecretKeySpec keySpec = null;

	private Cipher cipher = null;

	public ConfigurationServiceImpl()
	{
		keyToValueMap = new HashMap<String,String>();
		usedKeys = new HashSet<String>();
	}

	@Override
	public void init() throws ComponentConfigurationException
	{

		log.info("ConfigurationServiceImpl beginning. Will use '" + propertyFile + "'");
		parseOnePropertyFile( propertyFile );

		if( additionalPropertyFiles != null && additionalPropertyFiles.length > 0 )
		{
			for( String additionalPropertyFile : additionalPropertyFiles )
			{
				parseOnePropertyFile( additionalPropertyFile );
			}
		}

		try
		{
			// Initialise the secret key with our pepper (its not salt, since salt should be added to taste i.e. random).
			byte keyAsBytes[] = Base64.decodeBase64( b64EncryptionKey );

			keySpec = new SecretKeySpec(keyAsBytes, "Blowfish");
			cipher = Cipher.getInstance("Blowfish");
		}
		catch (NoSuchAlgorithmException nsae)
		{
			String msg = "Unable to initialise config key decryption: " + nsae.toString();
			log.error(msg, nsae);
			throw new ComponentConfigurationException(msg);
		}
		catch (NoSuchPaddingException nspe)
		{
			String msg = "Unable to initialise config key decryption: " + nspe.toString();
			log.error(msg, nspe);
			throw new ComponentConfigurationException(msg);
		}
	}

	private void parseOnePropertyFile( String pfToProcess ) throws ComponentConfigurationException
	{
		InputStream fis = null;
		BufferedReader br = null;
		try
		{
			fis = getClass().getResourceAsStream(pfToProcess);
			if( fis == null )
			{
				String msg = "Unable to find application properties file '" + propertyFile +
					"' in the current classpath";
				log.error(msg);
				throw new ComponentConfigurationException(msg);
			}
			br = new BufferedReader( new InputStreamReader( fis ) );

			String line = null;
			while( (line = br.readLine() ) != null )
			{
				if( line.length() == 0 || line.charAt(0) == '#' || line.charAt(0) == '/' )
				{
					continue;
				}
				else
				{
					int equalsIndex = line.indexOf( '=' );
					if( equalsIndex != -1 )
					{
						String key = line.substring(0, equalsIndex );
						String value = line.substring( equalsIndex + 1 );
						keyToValueMap.put( key, value );
					}
					else
					{
						throw new ComponentConfigurationException( "Unparsable line: " + line );
					}
				}
			}
		}
		catch (IOException ioe)
		{
			String msg = "IOException caught reading application properties file: " + ioe.toString();
			log.error(msg);
			throw new ComponentConfigurationException(msg);
		}
		finally
		{
			try
			{
				if( br != null )
				{
					br.close();
				}
				if( fis != null )
				{
					fis.close();
				}
			}
			catch (IOException e)
			{
				throw new ComponentConfigurationException( "Failed cleaning up config file stream: " + e.toString(), e );
			}
		}
	}

	@Override
	public void destroy()
	{
		for( String key : keyToValueMap.keySet() )
		{
			if( !usedKeys.contains( key ) )
			{
				log.warn("Configuration key: " + key + " never read!");
			}
		}
	}

	/**
	 * @param propertyFile The propertyFile to set.
	 */
	public void setPropertyFile(String propertyFile)
	{
		this.propertyFile = propertyFile;
	}

	public void setAdditionalPropertyFiles( String[] additionalPropertyFiles )
	{
		this.additionalPropertyFiles = additionalPropertyFiles;
	}

	@Override
	public String getSingleStringValue(String key)
			throws RecordNotFoundException
	{
		String retVal = keyToValueMap.get(key);
		usedKeys.add( key );

		if (retVal == null)
		{
			throw new RecordNotFoundException("Unknown configuration key: " + key);
		}
		else
		{
			return (retVal);
		}
	}

	@Override
	public String[] getCommaSeparatedStringValues(String key)
			throws RecordNotFoundException
	{
		String values = getSingleStringValue(key);
		return values.split(",");
	}

	@Override
	public String getSingleEncryptedStringValue(String key)
			throws RecordNotFoundException
	{
		String retVal = keyToValueMap.get(key);
		usedKeys.add( key );

		if (retVal == null)
		{
			throw new RecordNotFoundException("Unknown configuration key: " + key);
		}
		else
		{
			try
			{
				// Decrypt it

				// Convert the text back into bytes for decrpytion
				retVal = decryptStringWithKey(retVal);
			}
			catch (Exception e)
			{
				String msg = "Exception thrown decrypting " + key + ": " + e.toString();
				log.error(msg, e);
				throw new RecordNotFoundException(msg);
			}
			return (retVal);
		}
	}

	/**
	 * @param retVal
	 * @return
	 * @throws IOException
	 * @throws IllegalStateException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws UnsupportedEncodingException
	 */
	public String decryptStringWithKey(String cipherTextString)
			throws IOException, InvalidKeyException, IllegalStateException, IllegalBlockSizeException,
			BadPaddingException, UnsupportedEncodingException
	{
		byte cipherTextBytes[] = Base64.decodeBase64(cipherTextString);

		cipher.init(Cipher.DECRYPT_MODE, keySpec);

		byte plainTextBytes[] = cipher.doFinal(cipherTextBytes);

		return (new String(plainTextBytes, "UTF-8"));
	}

	/**
	 * @param plainText
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws IllegalStateException
	 */
	public String encryptStringWithKey(String plainText)
			throws UnsupportedEncodingException, InvalidKeyException, IllegalStateException, IllegalBlockSizeException,
			BadPaddingException
	{
		byte plainTextBytes[] = plainText.getBytes("UTF-8");

		cipher.init(Cipher.ENCRYPT_MODE, keySpec);

		byte cipherTextBytes[] = cipher.doFinal(plainTextBytes);

		String b64CipherText = Base64.encodeBase64String(cipherTextBytes);

		return (b64CipherText);
	}

	@Override
	public int getSingleIntValue(String key)
			throws RecordNotFoundException
	{
		int retVal = 0;
		boolean badValue = false;

		String tmpStr = keyToValueMap.get(key);
		usedKeys.add( key );

		if (tmpStr == null)
		{
			badValue = true;
		}
		else
		{
			try
			{
				retVal = Integer.parseInt(keyToValueMap.get(key));
			}
			catch (NumberFormatException nfe)
			{
				badValue = true;
			}
		}

		if (badValue)
		{
			throw new RecordNotFoundException("Unknown or bad number for configuration key: " + key);
		}

		return (retVal);
	}

	@Override
	public long getSingleLongValue(String key)
			throws RecordNotFoundException
	{
		long retVal = 0;
		boolean badValue = false;

		String tmpStr = keyToValueMap.get(key);
		usedKeys.add( key );

		if (tmpStr == null)
		{
			badValue = true;
		}
		else
		{
			try
			{
				retVal = Long.parseLong(keyToValueMap.get(key));
			}
			catch (NumberFormatException nfe)
			{
				badValue = true;
			}
		}

		if (badValue)
		{
			throw new RecordNotFoundException("Unknown or bad number for configuration key: " + key);
		}

		return (retVal);
	}

	@Override
	public double getSingleDoubleValue(String key)
			throws RecordNotFoundException
	{
		double retVal = 0;
		boolean badValue = false;

		String tmpStr = keyToValueMap.get(key);
		usedKeys.add( key );

		if (tmpStr == null)
		{
			badValue = true;
		}
		else
		{
			try
			{
				retVal = Double.parseDouble(keyToValueMap.get(key));
			}
			catch (NumberFormatException nfe)
			{
				badValue = true;
			}
		}

		if (badValue)
		{
			throw new RecordNotFoundException("Unknown or bad number for configuration key: " + key);
		}

		return (retVal);
	}

	@Override
	public float getSingleFloatValue(String key)
			throws RecordNotFoundException
	{
		float retVal = 0;
		boolean badValue = false;

		String tmpStr = keyToValueMap.get(key);
		usedKeys.add( key );

		if (tmpStr == null)
		{
			badValue = true;
		}
		else
		{
			try
			{
				retVal = Float.parseFloat(keyToValueMap.get(key));
			}
			catch (NumberFormatException nfe)
			{
				badValue = true;
			}
		}

		if (badValue)
		{
			throw new RecordNotFoundException("Unknown or bad number for configuration key: " + key);
		}

		return (retVal);
	}

	@Override
	public boolean getSingleBooleanValue(String key)
			throws RecordNotFoundException
	{
		boolean retVal = false;
		boolean badValue = false;

		String tmpStr = keyToValueMap.get(key);
		usedKeys.add( key );

		if (tmpStr == null)
		{
			badValue = true;
		}
		else
		{
			if (tmpStr.equals("true"))
			{
				retVal = true;
			}
			else if (tmpStr.equals("false"))
			{
				retVal = false;
			}
			else
			{
				badValue = true;
			}
		}

		if (badValue)
		{
			throw new RecordNotFoundException("Bad boolean specified for configuration key: " + key);
		}

		return (retVal);
	}

	@Override
	public boolean getSingleBooleanValue(String key, boolean defaultValue)
	{
		boolean retVal = false;

		String tmpStr = keyToValueMap.get(key);
		usedKeys.add( key );

		if (tmpStr == null)
		{
			return defaultValue;
		}
		else
		{
			if (tmpStr.trim().equals("true"))
			{
				retVal = true;
			}
			else if (tmpStr.trim().equals("false"))
			{
				retVal = false;
			}
			else
			{
				retVal = defaultValue;
			}
		}
		return retVal;
	}

	@Override
	public String[] getKeysBeginningWith(String keyStart)
	{
		ArrayList<String> retVal = new ArrayList<String>();

		Set<String> keys = keyToValueMap.keySet();
		for( String curKey : keys )
		{
			if (curKey.indexOf(keyStart) == 0)
			{
				usedKeys.add( curKey );
				retVal.add(curKey);
			}
		}

		return (retVal.toArray(new String[0]));
	}

	@Override
	public String getSingleStringValue(String key, String defaultValue)
	{
		String retVal = keyToValueMap.get(key);
		usedKeys.add( key );

		if (retVal == null)
		{
			return defaultValue;
		}
		else
		{
			return retVal;
		}
	}

	@Override
	public Date getSingleDateValue(String key)
			throws RecordNotFoundException
	{
		String strVal = this.getSingleStringValue(key);

		return DateConverter.customDateTimeStrToJavaDate( strVal, DateConverter.MA_USER_DATE_TIME_FORMAT);
	}

	public Map<String,String> getKeyValues()
	{
		return keyToValueMap;
	}

	@Override
	public Color getSingleColorValue(String key )
		throws RecordNotFoundException
	{
		String strVal = this.getSingleStringValue( key );
		return Color.decode( strVal );
	}
}
