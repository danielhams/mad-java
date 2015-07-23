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

package uk.co.modularaudio.service.imagefactory.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.configuration.ConfigurationServiceHelper;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;

public class ComponentImageFactoryImpl implements ComponentWithLifecycle, ComponentImageFactory
{
	private static Log log = LogFactory.getLog( ComponentImageFactoryImpl.class.getName() );

	private final static String CONFIG_KEY_FROM_PATH = ComponentImageFactoryImpl.class.getSimpleName() + ".FromPath";

	private ConfigurationService configurationService;

	private final Map<String, BufferedImage> biCache = new HashMap<String, BufferedImage>();

	private boolean getImagesFromPath;

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( configurationService == null )
		{
			throw new ComponentConfigurationException( "Service missing dependencies - check configuration" );
		}

		final Map<String,String> errors = new HashMap<String,String>();
		getImagesFromPath = ConfigurationServiceHelper.checkForBooleanKey( configurationService, CONFIG_KEY_FROM_PATH, errors );
		ConfigurationServiceHelper.errorCheck( errors );
	}

	@Override
	public void destroy()
	{
	}

	private BufferedImage getBufferedImageFromPath( final String directory, final String filename) throws DatastoreException
	{
		final String pathToLoad = directory + "/" + filename;
		BufferedImage retVal = biCache.get( pathToLoad );

		if( retVal == null )
		{
			try {
				final File input = new File( pathToLoad );
				retVal = ImageIO.read(input);
				if( log.isDebugEnabled() )
				{
					log.debug( "Adding " + pathToLoad + " to buffered image cache" );
				}
				if( retVal.getColorModel().hasAlpha() )
				{
					if( log.isWarnEnabled() )
					{
						log.warn("Image " + pathToLoad + " has alpha, and probably shouldn't");
					}
				}
				biCache.put( pathToLoad, retVal );
			} catch (final IOException ie) {
				final String msg = "Exception caught loading image " + pathToLoad + ": " + ie.toString();
				log.error( msg, ie );
				throw new DatastoreException( msg, ie );
			}
		}

		return retVal;
	}

	private BufferedImage getBufferedImageFromClasspath(  final String directory, final String filename) throws DatastoreException
	{
		throw new DatastoreException( "NI" );
	}

	@Override
	public synchronized BufferedImage getBufferedImage( final String directory, final String filename) throws DatastoreException
	{
		if( getImagesFromPath )
		{
			return getBufferedImageFromPath( directory, filename );
		}
		else
		{
			return getBufferedImageFromClasspath( directory, filename );
		}
	}

	public void setConfigurationService( final ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}
}
