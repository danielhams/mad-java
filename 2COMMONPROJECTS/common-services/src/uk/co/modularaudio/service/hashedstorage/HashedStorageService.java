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

package uk.co.modularaudio.service.hashedstorage;

import java.io.IOException;
import java.io.InputStream;

import uk.co.modularaudio.util.exception.DatastoreException;

public interface HashedStorageService
{
	// CRUD on the storage references
	HashedWarehouse initStorage( String rootPathOfStorage ) throws IOException;

	// CRUD on entries in the warehouse
	// Internal housekeeping, really
	HashedRef getHashedRefForFilename( String filename ) throws DatastoreException;

	String getPathToHashedRef( HashedWarehouse warehouse, HashedRef hashedRef );

	void storeContentsInWarehouse( HashedWarehouse warehouse, HashedRef hashedRef, InputStream contents )
		throws IOException;
	InputStream getContentsFromWarehouse( HashedWarehouse warehouse, HashedRef hashedRef )
		throws IOException;
	void removeContentsFromWarehouse( HashedWarehouse warehouse, HashedRef hashedRef )
		throws IOException;
}
