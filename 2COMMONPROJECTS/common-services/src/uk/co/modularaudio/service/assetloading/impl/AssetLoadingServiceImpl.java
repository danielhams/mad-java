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

package uk.co.modularaudio.service.assetloading.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import uk.co.modularaudio.service.assetloading.AssetLoadingService;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.configuration.ConfigurationServiceHelper;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;

public class AssetLoadingServiceImpl implements ComponentWithLifecycle, AssetLoadingService
{
//	private static Log log = LogFactory.getLog( AssetLoadingServiceImpl.class.getName() );

	private final static String CONFIG_KEY_ASSET_ROOT = AssetLoadingServiceImpl.class.getSimpleName() + ".AssetRoots";

	private ConfigurationService configurationService;

	private String[] assetRoots;

	@Override
	public void init() throws ComponentConfigurationException
	{
		Map<String,String> errors = new HashMap<String,String>();

		assetRoots = ConfigurationServiceHelper.checkForCommaSeparatedStringValues( configurationService, CONFIG_KEY_ASSET_ROOT, errors );

		ConfigurationServiceHelper.errorCheck( errors );
	}

	@Override
	public void destroy()
	{
	}

	public void setConfigurationService( ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	@Override
	public InputStream getAssetFromPath( String pathInAssetLibrary ) throws IOException
	{
		File fileToStream = null;
		for( int i = 0 ; i < assetRoots.length ; i++ )
		{
			String fullPathToAsset = assetRoots[i] + File.pathSeparatorChar + pathInAssetLibrary;
			File testFile = new File( fullPathToAsset );
			if( testFile.exists() && testFile.canRead() )
			{
				fileToStream = testFile;
				break;
			}

		}
		if( fileToStream == null )
		{
			throw new IOException("Unable to find path " + pathInAssetLibrary + " under any asset roots." );
		}
		else
		{
			FileInputStream fis = new FileInputStream( fileToStream );
			return fis;
		}
	}

}
