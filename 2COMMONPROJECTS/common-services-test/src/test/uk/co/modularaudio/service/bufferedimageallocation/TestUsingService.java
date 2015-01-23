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

package test.uk.co.modularaudio.service.bufferedimageallocation;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.bufferedimageallocation.impl.BufferedImageAllocationServiceImpl;
import uk.co.modularaudio.service.bufferedimageallocation.impl.cache.TiledBufferedImageImpl;
import uk.co.modularaudio.service.configuration.impl.ConfigurationServiceImpl;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.TiledBufferedImage;
import uk.co.modularaudio.util.io.StringStreamUtils;

public class TestUsingService extends TestCase
{
	private static Log log = LogFactory.getLog( TestUsingService.class.getName() );
	
	private ConfigurationServiceImpl configurationService = null;
	private BufferedImageAllocationServiceImpl bufferedImageAllocationService = null;

	protected void setUp() throws Exception
	{
		log.debug("Setting up..");
		
		configurationService = new ConfigurationServiceImpl();
		configurationService.setConfigResourcePath( "bias.properties" );
		bufferedImageAllocationService = new BufferedImageAllocationServiceImpl();
		bufferedImageAllocationService.setConfigurationService( configurationService );
		configurationService.init();
		bufferedImageAllocationService.init();
	}

	protected void tearDown() throws Exception
	{
		log.debug("Tearing down..");
		bufferedImageAllocationService.destroy();
		configurationService.destroy();
	}
	
	public void testAllocationLoop() throws Exception
	{
		int[][] blocksToAllocate = new int[][] { 
				new int[] { 80, 80 },
				new int[] { 80, 70 },
				new int[] { 80, 60 },
				new int[] { 80, 50 },
				new int[] { 80, 40 },
				new int[] { 80, 30 },
				new int[] { 80, 20 },
				new int[] { 60, 80 },
				new int[] { 60, 70 },
				new int[] { 60, 60 },
				new int[] { 60, 50 },
				new int[] { 60, 40 },
				new int[] { 60, 30 },
				new int[] { 60, 20 },
				new int[] { 80, 70 }
		};

		AllocationMatch match = new AllocationMatch();
		int numImages = blocksToAllocate.length;
		TiledBufferedImage[] bis = new TiledBufferedImageImpl[ numImages ];
		for( int i = 0 ; i < numImages ; i++ )
		{
			int[] dimsToAllocate = blocksToAllocate[ i ];
			int width = dimsToAllocate[0];
			int height = dimsToAllocate[1];
			TiledBufferedImage bi = bufferedImageAllocationService.allocateBufferedImage( "TestUsingService",
					match, 
					AllocationLifetime.LONG,
					AllocationBufferType.TYPE_INT_ARGB,
					width,
					height );
			
			bis[ i ] = bi;
		}
		
		StringStreamUtils.readStringBlock( System.in );
		
		int[] indexesToRemoveFirst = { 4, 8 };
		for( int r = 0; r < indexesToRemoveFirst.length ; r++ )
		{
			int itr = indexesToRemoveFirst[r];
			TiledBufferedImage tiledBufferedImageToRemove = bis[ itr ];
			bufferedImageAllocationService.freeBufferedImage( tiledBufferedImageToRemove );
			bis[ itr ] = null;
		}
		
		for( int i = 0 ; i < numImages ; i++ )
		{
			int reverseOrder = (numImages-1) - i;
			TiledBufferedImage tiledBufferedImageToRemove = bis[ reverseOrder ];
			if( tiledBufferedImageToRemove != null )
			{
				bufferedImageAllocationService.freeBufferedImage( tiledBufferedImageToRemove );
			}
		}
		
	}
	
//	public void testAllocatingOne() throws Exception
//	{
//		log.debug("Will attempt to allocate nice large image.");
//		
//		AllocationMatch allocationMatchToUse = new AllocationMatch();
//		TiledBufferedImage eightByThreeImage = bufferedImageAllocationService.allocateBufferedImage( "TUS",
//				allocationMatchToUse,
//				AllocationLifetime.LONG,
//				AllocationBufferType.TYPE_INT_ARGB,
//				800,
//				300 );
//		
//		log.debug("After 800x300 alloc");
//		
//		TiledBufferedImage threeByEightImage = bufferedImageAllocationService.allocateBufferedImage( "TUS",
//				allocationMatchToUse,
//				AllocationLifetime.LONG,
//				AllocationBufferType.TYPE_INT_ARGB,
//				300,
//				800 );
//		
//		log.debug("After 300x800 alloc");
//		
//		TiledBufferedImage halfImage = bufferedImageAllocationService.allocateBufferedImage( "TUS",
//				allocationMatchToUse,
//				AllocationLifetime.LONG,
//				AllocationBufferType.TYPE_INT_ARGB,
//				1024,
//				2048 );
//		
//		log.debug("After 1024x2048 alloc");
//		
//		TiledBufferedImage twoFourEightImage = bufferedImageAllocationService.allocateBufferedImage( "TUS",
//				allocationMatchToUse,
//				AllocationLifetime.LONG,
//				AllocationBufferType.TYPE_INT_ARGB,
//				2048,
//				2048 );
//		
//		log.debug("After 2048x2048 alloc");
//		
//		TiledBufferedImage fourNineSixImage = bufferedImageAllocationService.allocateBufferedImage( "TUS",
//				allocationMatchToUse,
//				AllocationLifetime.SHORT,
//				AllocationBufferType.TYPE_INT_ARGB,
//				4096,
//				4096 );
//		
//		log.debug("After 4096x4096 alloc");
//		
//		StringStreamUtils.readStringBlock( System.in );
//
//		bufferedImageAllocationService.freeBufferedImage( fourNineSixImage );
//		bufferedImageAllocationService.freeBufferedImage( twoFourEightImage );
//		bufferedImageAllocationService.freeBufferedImage( halfImage );
//		bufferedImageAllocationService.freeBufferedImage( threeByEightImage );
//		bufferedImageAllocationService.freeBufferedImage( eightByThreeImage );
//
//		StringStreamUtils.readStringBlock( System.in );
//	}

}
