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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.math.map.OpenLongObjectHashMap;

import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
import uk.co.modularaudio.service.bufferedimageallocation.impl.cache.AllocationCacheForImageType;
import uk.co.modularaudio.service.bufferedimageallocation.impl.cache.AllocationStrategy;
import uk.co.modularaudio.service.bufferedimageallocation.impl.cache.BraindeadAllocationStrategy;
import uk.co.modularaudio.service.bufferedimageallocation.impl.cache.TiledBufferedImageImpl;
import uk.co.modularaudio.service.bufferedimageallocation.impl.debugwindow.BufferedImageDebugWindow;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.configuration.ConfigurationServiceHelper;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.TiledBufferedImage;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;

public class BufferedImageAllocationServiceImpl implements ComponentWithLifecycle, BufferedImageAllocationService
{
	private static final String CONFIG_KEY_SHOW_DEBUG_WINDOW = BufferedImageAllocationServiceImpl.class.getSimpleName() + ".ShowDebugWindow";
	private static final String CONFIG_KEY_DEBUG_WINDOW_X = BufferedImageAllocationServiceImpl.class.getSimpleName() + ".DebugWindowX";
	private static final String CONFIG_KEY_DEBUG_WINDOW_Y = BufferedImageAllocationServiceImpl.class.getSimpleName() + ".DebugWindowY";

	private static Log log = LogFactory.getLog( BufferedImageAllocationServiceImpl.class.getName() );

	private ConfigurationService configurationService;

	private boolean showDebugWindow;
	private int debugWindowX;
	private int debugWindowY;

	private BufferedImageDebugWindow debugWindow;

	private final AllocationStrategy allocationStrategy = new BraindeadAllocationStrategy();
	private AllocationCacheConfiguration cacheConfiguration;
	//	private AllocationCacheForImageType allocationCache;
	private final OpenLongObjectHashMap<AllocationCacheForImageType> lifetimeAndImageTypeToAllocationCacheMap = new OpenLongObjectHashMap<AllocationCacheForImageType>();

	public BufferedImageAllocationServiceImpl()
	{
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		final Map<String,String> configErrors = new HashMap<String,String>();

		showDebugWindow = ConfigurationServiceHelper.checkForBooleanKey( configurationService, CONFIG_KEY_SHOW_DEBUG_WINDOW, configErrors );
		debugWindowX = ConfigurationServiceHelper.checkForIntKey( configurationService, CONFIG_KEY_DEBUG_WINDOW_X, configErrors );
		debugWindowY = ConfigurationServiceHelper.checkForIntKey( configurationService, CONFIG_KEY_DEBUG_WINDOW_Y, configErrors );

		ConfigurationServiceHelper.errorCheck( configErrors );

		cacheConfiguration = new AllocationCacheConfiguration( BufferedImageAllocationServiceImpl.class.getSimpleName(), configurationService );

		try
		{
			for( int bic = 0 ; bic < cacheConfiguration.getNumBufferedImageTypes() ; bic++ )
			{
				final AllocationLifetime lifetime = cacheConfiguration.getLifetimeAt( bic );
				final AllocationBufferType bufferedImageType = cacheConfiguration.getBufferedImageTypeAt( bic );
				final long lifetimeAndImageType = buildCompoundKey( lifetime, bufferedImageType.ordinal() );

				final AllocationCacheForImageType cacheForType = new AllocationCacheForImageType(
						lifetime.toString() + " " + bufferedImageType.toString(),
						cacheConfiguration,
						lifetime,
						bufferedImageType );

				lifetimeAndImageTypeToAllocationCacheMap.put( lifetimeAndImageType, cacheForType );
			}
		}
		catch( final DatastoreException de )
		{
			throw new ComponentConfigurationException( de );
		}

		try
		{
			if( showDebugWindow )
			{
				debugWindow = new BufferedImageDebugWindow( debugWindowX, debugWindowY, lifetimeAndImageTypeToAllocationCacheMap );
				debugWindow.setVisible( true );
			}
		}
		catch ( final Exception e)
		{
			final String msg = "Exception caught initialising debug window: " + e.toString();
			throw new ComponentConfigurationException( msg, e );
		}
	}

	@Override
	public void destroy()
	{
		if( showDebugWindow )
		{
			debugWindow.setVisible( false );
			debugWindow.dispose();
		}

		for( final AllocationCacheForImageType cache : lifetimeAndImageTypeToAllocationCacheMap.values() )
		{
			if( cache.getNumUsed() > 0 )
			{
				if( log.isErrorEnabled() )
				{
					log.error( "Shutting down but some buffers still allocated in " + cache.getCacheName() );
				}
				cache.errorUsedEntries();
			}
		}
	}

	@Override
	public TiledBufferedImage allocateBufferedImage( final String allocationSource,
			final AllocationMatch allocationMatchToUse,
			final AllocationLifetime lifetime,
			final AllocationBufferType bufferedImageType,
			final int imageWidthToUse,
			final int imageHeightToUse )
					throws DatastoreException
	{
		TiledBufferedImage retVal = null;
		final long compoundKey = buildCompoundKey( lifetime, bufferedImageType.ordinal() );
		final AllocationCacheForImageType cache = lifetimeAndImageTypeToAllocationCacheMap.get( compoundKey );
		if( cache != null )
		{
			//			if( log.isTraceEnabled() )
			//			{
			//				log.trace("Attempting to find and allocate buffered image for " + allocationSource + " of size " + imageWidthToUse + ", " + imageHeightToUse );
			//				log.trace("There are currently " + cache.getNumRaw() + " raw images in the cache");
			//			}
			allocationStrategy.huntForTileToUse( allocationSource, cache, cacheConfiguration, imageWidthToUse, imageHeightToUse, allocationMatchToUse );

			if( allocationMatchToUse.getFoundTile() != null )
			{
				//				log.trace("Using found tile for allocation of lifetime: " + lifetime.name() + " and imagetype: " + bufferedImageType.name());
				retVal = allocationMatchToUse.getFoundTile();
			}
			else
			{
				//				log.trace("Allocating new raw image as no existing tile suitable for allocation of lifetime: " + lifetime.name() + " and imagetype: " + bufferedImageType.name());
				retVal = cache.allocateNewRawImageMakeSubimage( allocationSource, allocationMatchToUse, imageWidthToUse, imageHeightToUse );
			}
			//			if( log.isDebugEnabled() )
			//			{
			//				cache.doConsistencyChecks();
			//			}

			return retVal;
		}
		else
		{
			throw new DatastoreException("Unsupported buffered image type: " + bufferedImageType );
		}
	}

	@Override
	public void freeBufferedImage( final TiledBufferedImage imageToFree )
			throws DatastoreException
	{
		final TiledBufferedImageImpl realThing = (TiledBufferedImageImpl)imageToFree;
		if( realThing == null )
		{
			log.error( "OOps!");
		}
		final AllocationCacheForImageType cache = realThing.getSourceCache();
		if( cache == null )
		{
			log.error("OOOPpppsss!");
		}
		//		if( log.isTraceEnabled() )
		//		{
		//			log.trace("Attempting to free buffered image for " + realThing.getUsedEntry().getAllocationSource() + " in cache: " + cache.getCacheName() );
		//			log.trace("There are currently " + cache.getNumRaw() + " raw images in the cache");
		//		}
		cache.freeTiledBufferedImage( realThing );
		//		if( log.isDebugEnabled() )
		//		{
		//			cache.doConsistencyChecks();
		//		}
	}

	public void setConfigurationService( final ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	private long buildCompoundKey( final AllocationLifetime lifetime,
			final int bufferedImageType )
	{
		final int lifetimeInt = lifetime.ordinal() & 0xffff;
		final int bitInt = bufferedImageType & 0xffff;
		final long shiftedTop = (long)lifetimeInt << 32;
		final long retVal = shiftedTop | bitInt;
		return retVal;
	}

}
