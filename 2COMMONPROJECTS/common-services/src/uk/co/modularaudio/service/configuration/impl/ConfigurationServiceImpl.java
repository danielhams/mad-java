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
 * <p>An implementation of the configuration service interface that
 * reads the configuration key values from a filesystem file or
 * from a classloader resource path.</p>
 *
 * <p>Clients are advised to use the helper class ConfigurationServiceHelper
 * which provides methods for bulk error handling.</p>
 *
 * @see uk.co.modularaudio.service.configuration.ConfigurationServiceHelper
 *
 * @author dan
 *
 */
public class ConfigurationServiceImpl implements ConfigurationService, ComponentWithLifecycle
{
	private static Log log = LogFactory.getLog(ConfigurationServiceImpl.class.getName());

	private final static String ALGO_TYPE = "Blowfish";

	/**
	 * The classloader resource path that should be used to find
	 * a properties file containing key value pairs
	 */
	private String configResourcePath;
	/**
	 * A list of additional classloader resource paths that are used
	 * to add additional key value pairs to the config information
	 */
	private String[] additionalResourcePaths;
	/**
	 * The filesystem path that should be used to find
	 * a properties file containing key value pairs
	 */
	private String configFilePath;
	/**
	 * A list of additional filesystem paths that are used
	 * to add additional key value pairs to the config information
	 */
	private String[] additionalFilePaths;

	/**
	 * Whether the configuration service should attempt to initialise
	 * an encryption cipher. Requries setting the encryptionKey.
	 */
	private boolean useEncryption = false;
	/**
	 * A base64 encoded string used as encryption pepper.
	 */
	private String encryptionPepper;

	private SecretKeySpec keySpec;
	private Cipher cipher;

	private final HashMap<String,String> keyToValueMap = new HashMap<String,String>();
	private final HashSet<String> usedKeys = new HashSet<String>();

	/**
	 * If set to true the service will log where it is reading key value pairs from
	 */
	private final boolean logWhereConfigComesFrom;

	/**
	 * Emtpy constructor will initialise the service without it logging
	 * where it is retrieving the key values from.
	 */
	public ConfigurationServiceImpl()
	{
		this( false );
	}

	/**
	 * Constructor that allows you to specify if you want the
	 * sources of key value pairs logged.
	 * @param logWhereConfigComesFrom set to yes for logging of config sources
	 */
	public ConfigurationServiceImpl( final boolean logWhereConfigComesFrom )
	{
		this.logWhereConfigComesFrom = logWhereConfigComesFrom;
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.util.component.ComponentWithLifecycle#init()
	 */
	@Override
	public void init() throws ComponentConfigurationException
	{

		if( configFilePath != null )
		{
			if( logWhereConfigComesFrom )
			{
				if( log.isInfoEnabled() )
				{
					log.info("ConfigurationServiceImpl beginning. Will use '" + configFilePath + "'"); // NOPMD by dan on 02/02/15 11:51
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
			if( logWhereConfigComesFrom )
			{
				if( log.isInfoEnabled() )
				{
					log.info("ConfigurationServiceImpl beginning. Will use '" + configResourcePath + "'"); // NOPMD by dan on 02/02/15 11:51
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


		if( useEncryption && encryptionPepper != null )
		{
			try
			{
				// Initialise the secret key with our pepper (its not salt,
				// since salt should be added to taste i.e. random).
				final byte keyAsBytes[] = Base64.decodeBase64( encryptionPepper );

				keySpec = new SecretKeySpec(keyAsBytes, ALGO_TYPE);
				cipher = Cipher.getInstance(ALGO_TYPE);
			}
			catch (final NoSuchAlgorithmException nsae)
			{
				final String msg = "Unable to initialise config key decryption: " + nsae.toString();
				log.error(msg, nsae);
				throw new ComponentConfigurationException(msg, nsae);
			}
			catch (final NoSuchPaddingException nspe)
			{
				final String msg = "Unable to initialise config key decryption: " + nspe.toString();
				log.error(msg, nspe);
				throw new ComponentConfigurationException(msg, nspe);
			}
		}
		else if( useEncryption )
		{
			throw new ComponentConfigurationException( "No encryption key specified yet component expected one" );
		}
	}

	/**
	 * Internal method that opens the supplied file and reads
	 * it as though it is a properties file.
	 * @param fpToProcess path of the file to read
	 * @throws ComponentConfigurationException
	 */
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

	/**
	 * Internal method that find the supplied resource using
	 * the current class classloader and reads
	 * it as though it is a properties file.
	 * @param rpToProcess resource path to open
	 * @throws ComponentConfigurationException
	 */
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

	/**
	 * Read the contents of a properties file as an input stream
	 * and populate the internal key value pairs with the contents.
	 * @param is input stream to parse
	 * @throws ComponentConfigurationException on malformed content
	 * @throws IOException on file IO errors
	 */
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

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.util.component.ComponentWithLifecycle#destroy()
	 */
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
	 * <p>Specify a classloader resource path as the source of configuration
	 * information.</p>
	 * <p>If both a filesystem path and classloader resource path are specified,
	 * the filesystem path wins.</p>
	 * @param configResource the resource path
	 */
	public void setConfigResourcePath( final String configResource )
	{
		this.configResourcePath = configResource;
	}

	/**
	 * Additional classloader resource paths from which to pull key value
	 * pairs.
	 * @param additionalResourcePaths an array of classloader resource paths
	 */
	public void setAdditionalResourcePaths( final String[] additionalResourcePaths )
	{
		this.additionalResourcePaths = additionalResourcePaths;
	}

	/**
	 * If encryption of values inside the properties file is required
	 * this field must be set to true
	 * @param useEncryption set to true for use of encryption
	 */
	public void setUseEncryption( final boolean useEncryption )
	{
		this.useEncryption = useEncryption;
	}

	/**
	 * <p>The encryption seed passed during initialisation of the cipher.</p>
	 * <p>This field must be set if useEncryption is true.</p>
	 * <p>Use of the method {@link ConfigurationServiceImpl#encryptStringWithKey(String)}
	 * is a good way to generate the necessary base64 encoded string.</p>
	 * @param encryptionPepper base64 encoded string of the required encryption pepper
	 */
	public void setEncryptionPepper( final String encryptionPepper )
	{
		this.encryptionPepper = encryptionPepper;
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.configuration.ConfigurationService#getSingleStringValue(java.lang.String)
	 */
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

	/**
	 * <p>Specify a filesystem path as the source of configuration
	 * information.</p>
	 * <p>If both a filesystem path and classloader resource path are specified,
	 * the filesystem path wins.</p>
	 * @param configFile path to the properties file on the filesystem
	 */
	public void setConfigFilePath( final String configFile )
	{
		this.configFilePath = configFile;
	}

	/**
	 * Additional filesystem paths from which to pull key value
	 * pairs.
	 * @param additionalFilePaths array of filesystem paths
	 */
	public void setAdditionalFilePaths( final String[] additionalFilePaths )
	{
		this.additionalFilePaths = additionalFilePaths;
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.configuration.ConfigurationService#getCommaSeparatedStringValues(java.lang.String)
	 */
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

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.configuration.ConfigurationService#getSingleEncryptedStringValue(java.lang.String)
	 */
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

	/**
	 * A method to decrypt a base64 encoded string ciphertext previous encrypted
	 * with the corresponding encryptStringWithKey method.
	 * @param cipherTextString base64 encoded cipher text
	 * @return decrypted plain text
	 * @throws InvalidKeyException on failure during cipher initialisation
	 * @throws IllegalBlockSizeException on failure during cipher initialisation
	 * @throws BadPaddingException on failure during cipher initialisation
	 * @throws UnsupportedEncodingException on failure during cipher initialisation
	 */
	public String decryptStringWithKey(final String cipherTextString)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException
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

	/**
	 * A method to encrypt a given plaintext string using the configuration service internal
	 * cipher mechanism.
	 * @param plainText plaintext string to encrypt
	 * @return cipher text encrypted contents as a base64 encoded string
	 * @throws InvalidKeyException on failure during cipher initialisation
	 * @throws IllegalBlockSizeException on failure during cipher initialisation
	 * @throws BadPaddingException on failure during cipher initialisation
	 * @throws UnsupportedEncodingException on failure during cipher initialisation
	 */
	public String encryptStringWithKey(final String plainText)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException
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

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.configuration.ConfigurationService#getSingleIntValue(java.lang.String)
	 */
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

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.configuration.ConfigurationService#getSingleLongValue(java.lang.String)
	 */
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

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.configuration.ConfigurationService#getSingleDoubleValue(java.lang.String)
	 */
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

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.configuration.ConfigurationService#getSingleFloatValue(java.lang.String)
	 */
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

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.configuration.ConfigurationService#getSingleBooleanValue(java.lang.String)
	 */
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

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.configuration.ConfigurationService#getSingleBooleanValue(java.lang.String, boolean)
	 */
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

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.configuration.ConfigurationService#getKeysBeginningWith(java.lang.String)
	 */
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

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.configuration.ConfigurationService#getSingleStringValue(java.lang.String, java.lang.String)
	 */
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

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.configuration.ConfigurationService#getSingleDateValue(java.lang.String)
	 */
	@Override
	public Date getSingleDateValue(final String key)
			throws RecordNotFoundException
	{
		final String strVal = this.getSingleStringValue(key);

		return DateConverter.customDateTimeStrToJavaDate( strVal, DateConverter.MA_USER_DATE_TIME_FORMAT);
	}

	/* (non-Javadoc)
	 * @see uk.co.modularaudio.service.configuration.ConfigurationService#getSingleColorValue(java.lang.String)
	 */
	@Override
	public Color getSingleColorValue(final String key )
			throws RecordNotFoundException
	{
		final String strVal = this.getSingleStringValue( key );
		return Color.decode( strVal );
	}
}
