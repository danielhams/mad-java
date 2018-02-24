/**
 *
 * Copyright (C) 2015 -> 2018 - Daniel Hams, Modular Audio Limited
 *                              daniel.hams@gmail.com
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

package test.uk.co.modularaudio.service.hashedstorage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hsqldb.lib.StringInputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.co.modularaudio.service.hashedstorage.HashedRef;
import uk.co.modularaudio.service.hashedstorage.HashedStorageService;
import uk.co.modularaudio.service.hashedstorage.HashedWarehouse;
import uk.co.modularaudio.service.hashedstorage.impl.HashedStorageServiceImpl;

public class HashedStorageServiceTester
{
	private final static Log LOG = LogFactory.getLog( HashedStorageServiceTester.class );

	private HashedStorageServiceImpl hssi;
	private HashedStorageService hss;

	@Before
	public void setUp() throws Exception
	{
		hssi = new HashedStorageServiceImpl();
		hssi.init();

		hss = hssi;
	}

	@After
	public void tearDown() throws Exception
	{
		hss = null;
		hssi.destroy();
	}

	@Test
	public void testGetHashedRefForFilename() throws Exception
	{
		final String uniqStr = Long.toString( System.nanoTime() );
		final HashedWarehouse testWarehouse = hss.initStorage( "/tmp/hsstestdir" + uniqStr );

		final String[] testRefStrings = new String[] { "1", "2", "2000", "4000", "10000000" };

		for( final String testRefInput : testRefStrings )
		// DONT DO THIS, CREATES LOTS OF LITTLE FILES THAT ARE A PAIN TO
		// REMOVE AFTERWARDS
		// for( int i = 0 ; i < 1000000 ; ++i )
		{
			// final String testRefInput = Integer.toString( i );
			final HashedRef ref = hss.getHashedRefForFilename( testRefInput );
			final String sha1 = ref.getSha1OfId();
			// LOG.info( "For " + testRefInput + " sha1 ref is " + sha1 );

			final String path = hss.getPathToHashedRef( testWarehouse, ref );
			// LOG.info( "This maps to the path " + path );

			final StringInputStream sis = new StringInputStream( "bananas" );
			hss.storeContentsInWarehouse( testWarehouse, ref, sis );
		}
	}

}
