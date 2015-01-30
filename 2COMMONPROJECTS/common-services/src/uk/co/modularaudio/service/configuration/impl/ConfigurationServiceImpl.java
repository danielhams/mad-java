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
import java.io.File;
import java.io.FileInputStream;
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

	private String configResourcePath;
	private String[] additionalResourcePaths;
	private String configFilePath;
	private String[] additionalFilePaths;

	private String encryptionKey;
	private boolean useEncryption = false;

	private final HashMap<String,String> keyToValueMap = new HashMap<String,String>();
	private final HashSet<String> usedKeys = new HashSet<String>();

//	private final static String B64_ENCRYPTION_KEY = "eUg0lWOSVA2vSgN/OcDz8Q==";

	private SecretKeySpec keySpec;

	private Cipher cipher;

	private final boolean logFileUsed;

	public ConfigurationServiceImpl()
	{
		this( false );
	}

	public ConfigurationServiceImpl( final boolean logFileUsed )
	{
		this.logFileUsed = logFileUsed;
	}

	@Override
	public void init() throws ComponentConfigurationException
	{

		if( configFilePath != null )
		{
			if( logFileUsed )
			{
				if( log.isInfoEnabled() )
				{
					log.info("ConfigurationServiceImpl beginning. Will use '" + configFilePath + "'");
				}
			}
			parseOneFilePath( configFilePath );

			if( additionalFilePaths != null && additionalFilePaths.length > 0 )
			{
				for( final String additionalFilePath : additionalFilePaths )
				{
					parseOneFilePath( additionalFilePath );
				}
			}
		}
		else if( configResourcePath != null )
		{
			if( logFileUsed )
			{
				if( log.isInfoEnabled() )
				{
					log.info("ConfigurationServiceImpl beginning. Will use '" + configResourcePath + "'");
				}
			}
			parseOneResourcePath( configResourcePath );

			if( additionalResourcePaths != null && additionalResourcePaths.length > 0 )
			{
				for( final String additionalResourcePath : additionalResourcePaths )
				{
					parseOneResourcePath( additionalResourcePath );
				}
			}
		}
		else
		{
		}


		if( useEncryption && encryptionKey != null )
		{
			try
			{
				// Initialise the secret key with our pepper (its not salt, since salt should be added to taste i.e. random).
				final byte keyAsBytes[] = Base64.decodeBase64( encryptionKey );

				keySpec = new SecretKeySpec(keyAsBytes, "Blowfish");
				cipher = Cipher.getInstance("Blowfish");
			}
			catch (final NoSuchAlgorithmException nsae)
			{
				final String msg = "Unable to initialise config key decryption: " + nsae.toString();
				log.error(msg, nsae);
				throw new ComponentConfigurationException(msg);
			}
			catch (final NoSuchPaddingException nspe)
			{
				final String msg = "Unable to initialise config key decryption: " + nspe.toString();
				log.error(msg, nspe);
				throw new ComponentConfigurationException(msg);
			}
		}
		else if( useEncryption )
		{
			throw new ComponentConfigurationException( "No encryption key specified yet component expected one" );
		}
	}

	private void parseOneFilePath( final String fpToProcess ) throws ComponentConfigurationException
	{
		InputStream fis = null;
		final BufferedReader br = null;
		try
		{
			fis = new FileInputStream( new File( fpToProcess ) );
			parseOneInputStream( fis );
		}
		catch (final IOException ioe)
		{
			final String msg = "IOException caught reading config file: " + ioe.toString();
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
			catch (final IOException e)
			{
				throw new ComponentConfigurationException( "Failed cleaning up config file stream: " + e.toString(), e );
			}
		}
	}

	private void parseOneResourcePath( final String rpToProcess ) throws ComponentConfigurationException
	{
		InputStream fis = null;
		final BufferedReader br = null;
		try
		{
			fis = getClass().getResourceAsStream( rpToProcess );
			if( fis == null )
			{
				final String msg = "Unable to find config file '" + rpToProcess + "'";
				log.error(msg);
				throw new ComponentConfigurationException(msg);
			}
			parseOneInputStream( fis );
		}
		catch (final IOException ioe)
		{
			final String msg = "IOException caught reading config file: " + ioe.toString();
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
			catch (final IOException e)
			{
				throw new ComponentConfigurationException( "Failed cleaning up config file stream: " + e.toString(), e );
			}
		}
	}

	private void parseOneInputStream( final InputStream is ) throws ComponentConfigurationException, IOException
	{
		final BufferedReader br = new BufferedReader( new InputStreamReader( is ) );

		String line = null;
		while( (line = br.readLine() ) != null )
		{
			if( line.length() == 0 || line.charAt(0) == '#' || line.charAt(0) == '/' )
			{
				continue;
			}
			else
			{
				final int equalsIndex = line.indexOf( '=' );
				if( equalsIndex != -1 )
				{
					final String key = line.substring(0, equalsIndex );
					final String value = line.substring( equalsIndex + 1 );
					keyToValueMap.put( key, value );
				}
				else
				{
					throw new ComponentConfigurationException( "Unparsable line: " + line );
				}
			}
		}
	}

	@Override
	public void destroy()
	{
		for( final String key : keyToValueMap.keySet() )
		{
			if( !usedKeys.contains( key ) )
			{
				if( log.isWarnEnabled() )
				{
					log.warn("Configuration key: " + key + " never read!");
				}
			}
		}
	}

	/**
	 * @param configResource The property file to find in the classpath
	 */
	public void setConfigResourcePath( final String configResource )
	{
		this.configResourcePath = configResource;
	}

	public void setAdditionalResourcePaths( final String[] additionalResourcePaths )
	{
		this.additionalResourcePaths = additionalResourcePaths;
	}

	public void setUseEncryption( final boolean useEncryption )
	{
		this.useEncryption = useEncryption;
	}

	public void setEncryptionKey( final String encryptionKey )
	{
		this.encryptionKey = encryptionKey;
	}

	@Override
	public String getSingleStringValue(final String key)
			throws RecordNotFoundException
	{
		final String retVal = keyToValueMap.get(key);
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

	public void setConfigFilePath( final String configFile )
	{
		this.configFilePath = configFile;
	}

	public void setAdditionalFilePaths( final String[] additionalFilePaths )
	{
		this.additionalFilePaths = additionalFilePaths;
	}

	@Override
	public String[] getCommaSeparatedStringValues(final String key)
			throws RecordNotFoundException
	{
		final String values = getSingleStringValue(key);
		if( values.length() > 0 )
		{
			return values.split(",");
		}
		else
		{
			return new String[0];
		}
	}

	@Override
	public String getSingleEncryptedStringValue(final String key)
			throws RecordNotFoundException
	{
		if( !useEncryption )
		{
			throw new RecordNotFoundException("No encryption configured");
		}
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
			catch (final Exception e)
			{
				final String msg = "Exception thrown decrypting " + key + ": " + e.toString();
				log.error(msg, e);
				throw new RecordNotFoundException(msg);
			}
			return (retVal);
		}
	}

	public String decryptStringWithKey(final String cipherTextString)
			throws IOException, InvalidKeyException, IllegalStateException, IllegalBlockSizeException,
			BadPaddingException, UnsupportedEncodingException
	{
		if( !useEncryption )
		{
			throw new IllegalStateException("No encryption configured");
		}
		final byte cipherTextBytes[] = Base64.decodeBase64(cipherTextString);

		cipher.init(Cipher.DECRYPT_MODE, keySpec);

		final byte plainTextBytes[] = cipher.doFinal(cipherTextBytes);

		return new String(plainTextBytes, "UTF-8");
	}

	public String encryptStringWithKey(final String plainText)
			throws UnsupportedEncodingException, InvalidKeyException, IllegalStateException, IllegalBlockSizeException,
			BadPaddingException
	{
		if( !useEncryption )
		{
			throw new IllegalStateException("No encryption configured");
		}
		final byte plainTextBytes[] = plainText.getBytes("UTF-8");

		cipher.init(Cipher.ENCRYPT_MODE, keySpec);

		final byte cipherTextBytes[] = cipher.doFinal(plainTextBytes);

		final String b64CipherText = Base64.encodeBase64String(cipherTextBytes);

		return b64CipherText;
	}

	@Override
	public int getSingleIntValue(final String key)
			throws RecordNotFoundException
	{
		int retVal = 0;
		boolean badValue = false;

		final String tmpStr = keyToValueMap.get(key);
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
			catch (final NumberFormatException nfe)
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
	public long getSingleLongValue(final String key)
			throws RecordNotFoundException
	{
		long retVal = 0;
		boolean badValue = false;

		final String tmpStr = keyToValueMap.get(key);
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
			catch (final NumberFormatException nfe)
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
	public double getSingleDoubleValue(final String key)
			throws RecordNotFoundException
	{
		double retVal = 0;
		boolean badValue = false;

		final String tmpStr = keyToValueMap.get(key);
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
			catch (final NumberFormatException nfe)
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
	public float getSingleFloatValue(final String key)
			throws RecordNotFoundException
	{
		float retVal = 0;
		boolean badValue = false;

		final String tmpStr = keyToValueMap.get(key);
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
			catch (final NumberFormatException nfe)
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
	public boolean getSingleBooleanValue(final String key)
			throws RecordNotFoundException
	{
		boolean retVal = false;
		boolean badValue = false;

		final String tmpStr = keyToValueMap.get(key);
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
	public boolean getSingleBooleanValue(final String key, final boolean defaultValue)
	{
		boolean retVal = false;

		final String tmpStr = keyToValueMap.get(key);
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
	public String[] getKeysBeginningWith(final String keyStart)
	{
		final ArrayList<String> retVal = new ArrayList<String>();

		final Set<String> keys = keyToValueMap.keySet();
		for( final String curKey : keys )
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
	public String getSingleStringValue(final String key, final String defaultValue)
	{
		final String retVal = keyToValueMap.get(key);
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
	public Date getSingleDateValue(final String key)
			throws RecordNotFoundException
	{
		final String strVal = this.getSingleStringValue(key);

		return DateConverter.customDateTimeStrToJavaDate( strVal, DateConverter.MA_USER_DATE_TIME_FORMAT);
	}

	public Map<String,String> getKeyValues()
	{
		return keyToValueMap;
	}

	@Override
	public Color getSingleColorValue(final String key )
			throws RecordNotFoundException
	{
		final String strVal = this.getSingleStringValue( key );
		return Color.decode( strVal );
	}
}
