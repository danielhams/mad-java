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

package uk.co.modularaudio.service.hashedstorage.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import uk.co.modularaudio.service.hashedstorage.HashedRef;
import uk.co.modularaudio.service.hashedstorage.HashedStorageService;
import uk.co.modularaudio.service.hashedstorage.HashedWarehouse;
import uk.co.modularaudio.util.atomicio.AtomicFileUtilities;
import uk.co.modularaudio.util.atomicio.FileUtilities;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;

public class HashedStorageServiceImpl implements ComponentWithLifecycle, HashedStorageService
{
	private static final int STORAGE_BUFFER_LENGTH = 4096;

//	private static Log log = LogFactory.getLog( HashedStorageServiceImpl.class.getName() );

	private static AtomicFileUtilities atomicFileUtils = FileUtilities.getAtomicFileUtilities();

	@Override
	public void init() throws ComponentConfigurationException
	{
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public HashedWarehouse initStorage( final String rootPathOfStorage ) throws IOException
	{
		final HashedWarehouse retVal = new HashedWarehouse( rootPathOfStorage );
		return retVal;
	}

	@Override
	public HashedRef getHashedRefForFilename(final String filename) throws DatastoreException
	{
		final HashedRef retVal = new HashedRef( HashComputer.computeTextSha1( filename ) );
		return retVal;
	}

	private final byte[] storageBuf = new byte[STORAGE_BUFFER_LENGTH];

	@Override
	public void storeContentsInWarehouse( final HashedWarehouse warehouse, final HashedRef hashedRef, final InputStream contents )
			throws IOException
	{
		final String outputPath = computeWarehouseRefPath( warehouse, hashedRef );
		final File outputFile = new File( outputPath );
		final File enclosingDir = outputFile.getParentFile();
		if( !enclosingDir.exists() && !enclosingDir.mkdirs() )
		{
			throw new IOException("Failed during enclosing directory creation");
		}
		// Place into a temporary file until completely written
		final File tmpFile = File.createTempFile( "hashedstorage", ".tmp", enclosingDir );
		final String tmpFilePath = tmpFile.getAbsolutePath();
		final FileOutputStream fos = new FileOutputStream( tmpFilePath );
		int numRead = 0;
		while( (numRead = contents.read( storageBuf ) ) > 0 )
		{
			fos.write( storageBuf, 0, numRead );
		}
		fos.close();
		// Now atomically move it over to the required filename
		atomicFileUtils.moveFile( tmpFilePath, outputPath );
	}

	private String computeWarehouseRefPath(final HashedWarehouse warehouse, final HashedRef hashedRef)
	{
		final StringBuilder retVal = new StringBuilder( warehouse.getStorageRootPath() );
		final String sha1 = hashedRef.getSha1OfId();
		retVal.append( "/" );
		retVal.append( sha1.substring( 0, 2 ) );
		retVal.append( "/" );
		retVal.append( sha1.substring( 2 ) );
		retVal.append(".hwf");
		return retVal.toString();
	}

	@Override
	public InputStream getContentsFromWarehouse(final HashedWarehouse warehouse, final HashedRef hashedRef) throws IOException
	{
		final String inputPath = computeWarehouseRefPath( warehouse, hashedRef );
		final FileInputStream fis = new FileInputStream( inputPath );
		return fis;
	}

	@Override
	public void removeContentsFromWarehouse(final HashedWarehouse warehouse, final HashedRef hashedRef) throws IOException
	{
		final String removalPath = computeWarehouseRefPath( warehouse, hashedRef );
		final File removalFile = new File( removalPath );
		final File removalDir = removalFile.getParentFile();
		if( removalFile.delete() )
		{
			// Remove the directory if it's empty
			if( removalDir.listFiles().length == 0 )
			{
				if( !removalDir.delete() )
				{
					throw new IOException("Unable to remove empty directory: " + removalDir );
				}
			}
		}
		else
		{
			throw new IOException("Unable to remove file " + removalPath );
		}
	}

	@Override
	public String getPathToHashedRef(final HashedWarehouse warehouse, final HashedRef hashedRef)
	{
		return computeWarehouseRefPath( warehouse, hashedRef );
	}

}
