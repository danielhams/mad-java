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

package uk.co.modularaudio.service.bufferedimageallocation.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.math.map.OpenLongIntHashMap;

import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.configuration.ConfigurationServiceHelper;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class AllocationCacheConfiguration
{
	private static Log log = LogFactory.getLog(AllocationCacheConfiguration.class.getName() );
	
	private static final String CONFIG_KEY_CACHE_STD_ALLOC_WIDTH = ".StdAllocWidth";
	private static final String CONFIG_KEY_CACHE_STD_ALLOC_HEIGHT= ".StdAllocHeight";
	private static final String CONFIG_KEY_CACHE_TYPES_AND_PAGES = ".TypesAndInitialPages";


	private int stdAllocImageWidth = -1;
	private int stdAllocImageHeight = -1;

	private int numBufferedImageTypes = -1;
	private AllocationBufferType[] bufferedImageTypes = null;
	private AllocationLifetime[] bufferedImageLifetimes = null;
	private OpenLongIntHashMap lifetimeAndTypeInitialPages = new OpenLongIntHashMap();

	public AllocationCacheConfiguration( String configKeyPrefix, ConfigurationService configurationService ) throws ComponentConfigurationException
	{
		Map<String,String> errors = new HashMap<String,String>();
		
		stdAllocImageWidth = ConfigurationServiceHelper.checkForIntKey( configurationService, configKeyPrefix + CONFIG_KEY_CACHE_STD_ALLOC_WIDTH, errors );
		stdAllocImageHeight = ConfigurationServiceHelper.checkForIntKey( configurationService, configKeyPrefix + CONFIG_KEY_CACHE_STD_ALLOC_HEIGHT, errors );
		String[] typesAndPages = ConfigurationServiceHelper.checkForCommaSeparatedStringValues( configurationService, 
				configKeyPrefix + CONFIG_KEY_CACHE_TYPES_AND_PAGES, errors );
		ConfigurationServiceHelper.errorCheck( errors );
		
		extractInitialPagesFromConfigPairs( typesAndPages );
		
		log.debug("Configured with standard allocation sizes of ( " + stdAllocImageWidth + ", " + stdAllocImageHeight + " )");
	}

	private void extractInitialPagesFromConfigPairs( String[] typesAndPages )
		throws ComponentConfigurationException
	{
		List<AllocationBufferType> typesFound = new ArrayList<AllocationBufferType>();
		List<Integer> numInitialPagesForType = new ArrayList<Integer>();
		List<AllocationLifetime> allocLifetimes = new ArrayList<AllocationLifetime>();
		int numFound = 0;
		try
		{
			for( int i = 0 ; i < typesAndPages.length ; i++ )
			{
				String[] sepOnEquals = typesAndPages[i].split( "=" );
				if( sepOnEquals.length != 2 )
				{
					String msg = "Malformed comma separated key value pairs (a=b,c=d) in initial pages";
					throw new ComponentConfigurationException( msg );
				}
				String id = sepOnEquals[ 0 ];
				String[] splitId = id.split( "-" );
				if( splitId.length != 2 )
				{
					String msg = "Malformed allocation lifetime and type in initial pages: " + id;
					throw new ComponentConfigurationException( msg );
				}
				String lifetimeStr = splitId[0];
				AllocationLifetime al = AllocationLifetime.valueOf( lifetimeStr );
				String typeStr = splitId[1];
				String numInitialPagesStr = sepOnEquals[ 1 ];

				int numPagesAsInt = Integer.parseInt( numInitialPagesStr );
				AllocationBufferType abt = AllocationBufferType.valueOf( typeStr );
				if( abt != null )
				{
					typesFound.add( abt );
					allocLifetimes.add( al );
					numInitialPagesForType.add( numPagesAsInt );
					numFound++;
				}
				else
				{
					String msg = "Buffer type specified in configuration not handled: " + typeStr;
					throw new ComponentConfigurationException( msg );
				}
			}
			numBufferedImageTypes = numFound;
			bufferedImageTypes = new AllocationBufferType[ numFound ];
			bufferedImageLifetimes = new AllocationLifetime[ numFound ];
			for( int bic = 0 ; bic < numFound ; bic++ )
			{
				AllocationBufferType bufferedImageType = typesFound.get( bic );
				AllocationLifetime lifetime = allocLifetimes.get( bic );
				bufferedImageTypes[ bic ] = bufferedImageType;
				bufferedImageLifetimes[ bic ] = lifetime;
				long compoundKey = calculateCompoundKey( lifetime, bufferedImageType );
				lifetimeAndTypeInitialPages.put( compoundKey, numInitialPagesForType.get( bic ) );
			}
		}
		catch ( Exception e )
		{
			String msg = "Unable to parse initial pages configuration: " + e.toString();
			throw new ComponentConfigurationException( msg, e );
		}
	}

	public int getStdAllocImageWidth()
	{
		return stdAllocImageWidth;
	}

	public int getStdAllocImageHeight()
	{
		return stdAllocImageHeight;
	}

	public int getNumBufferedImageTypes()
	{
		return numBufferedImageTypes;
	}

	public AllocationBufferType getBufferedImageTypeAt( int bic )
	{
		return bufferedImageTypes[ bic ];
	}

	public AllocationLifetime getLifetimeAt( int bic )
	{
		return bufferedImageLifetimes[ bic ];
	}
	
	public int getInitialPagesForLifetimeAndType( AllocationLifetime lifetime, AllocationBufferType bufferedImageType )
		throws RecordNotFoundException
	{
		long compoundKey = calculateCompoundKey( lifetime, bufferedImageType );
		if( lifetimeAndTypeInitialPages.containsKey( compoundKey ) )
		{
			return lifetimeAndTypeInitialPages.get( compoundKey );
		}
		else
		{
			throw new RecordNotFoundException("Unknown allocation buffer type looking up initial pages: " + bufferedImageType );
		}
	}

	private long calculateCompoundKey( AllocationLifetime lifetime,
			AllocationBufferType bufferedImageType )
	{
		long compoundKey = ((long)lifetime.ordinal() << 32) | bufferedImageType.ordinal();
		return compoundKey;
	}

}
