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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.bufferedimageallocation.impl.BufferedImageAllocationServiceImpl;
import uk.co.modularaudio.service.bufferedimageallocation.impl.cache.TiledBufferedImageImpl;
import uk.co.modularaudio.service.configuration.impl.ConfigurationServiceImpl;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.TiledBufferedImage;

public class TestUsingService
{
	private static Log log = LogFactory.getLog( TestUsingService.class.getName() );

	private ConfigurationServiceImpl configurationService;
	private BufferedImageAllocationServiceImpl bufferedImageAllocationService;

	public TestUsingService()
	{
	}

	protected void setUp() throws Exception
	{
		log.debug("Setting up..");

		configurationService = new ConfigurationServiceImpl();
		configurationService.setConfigResourcePath( "/bias.properties" );
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
		final int[][] blocksToAllocate = new int[][] {
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

		final AllocationMatch match = new AllocationMatch();
		final int numImages = blocksToAllocate.length;
		final TiledBufferedImage[] bis = new TiledBufferedImageImpl[ numImages ];
		for( int i = 0 ; i < numImages ; i++ )
		{
			final int[] dimsToAllocate = blocksToAllocate[ i ];
			final int width = dimsToAllocate[0];
			final int height = dimsToAllocate[1];
			final TiledBufferedImage bi = bufferedImageAllocationService.allocateBufferedImage( "TestUsingService",
					match,
					AllocationLifetime.LONG,
					AllocationBufferType.TYPE_INT_ARGB,
					width,
					height );

			bis[ i ] = bi;
		}

		final int[] indexesToRemoveFirst = { 4, 8 };
		for( int r = 0; r < indexesToRemoveFirst.length ; r++ )
		{
			final int itr = indexesToRemoveFirst[r];
			final TiledBufferedImage tiledBufferedImageToRemove = bis[ itr ];
			bufferedImageAllocationService.freeBufferedImage( tiledBufferedImageToRemove );
			bis[ itr ] = null;
		}

		for( int i = 0 ; i < numImages ; i++ )
		{
			final int reverseOrder = (numImages-1) - i;
			final TiledBufferedImage tiledBufferedImageToRemove = bis[ reverseOrder ];
			if( tiledBufferedImageToRemove != null )
			{
				bufferedImageAllocationService.freeBufferedImage( tiledBufferedImageToRemove );
			}
		}

	}

	public static void main( final String[] args ) throws Exception
	{
		final TestUsingService tus = new TestUsingService();
		tus.setUp();
		try
		{
			tus.testAllocationLoop();
		}
		finally
		{
			tus.tearDown();
		}
	}
}
