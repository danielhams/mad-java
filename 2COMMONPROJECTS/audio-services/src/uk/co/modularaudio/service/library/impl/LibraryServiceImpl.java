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

package uk.co.modularaudio.service.library.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import uk.co.modularaudio.service.audiofileio.AudioFileIOService;
import uk.co.modularaudio.service.audiofileio.StaticMetadata;
import uk.co.modularaudio.service.hibsession.HibernateSessionService;
import uk.co.modularaudio.service.library.LibraryService;
import uk.co.modularaudio.service.library.vos.CuePoint;
import uk.co.modularaudio.service.library.vos.LibraryEntry;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.hibernate.HibernateExceptionHandler;
import uk.co.modularaudio.util.hibernate.HibernateQueryBuilder;
import uk.co.modularaudio.util.hibernate.NoSuchHibernateSessionException;
import uk.co.modularaudio.util.hibernate.ReflectionUtils;
import uk.co.modularaudio.util.hibernate.ThreadLocalSessionResource;
import uk.co.modularaudio.util.hibernate.common.ComponentWithHibernatePersistence;
import uk.co.modularaudio.util.hibernate.common.HibernatePersistedBeanDefinition;

public class LibraryServiceImpl implements ComponentWithLifecycle, ComponentWithHibernatePersistence, LibraryService
{
	private static Log log = LogFactory.getLog( LibraryServiceImpl.class.getName() );

	private String databaseTablePrefix = null;

	private HibernateSessionService hibernateSessionService = null;
	private AudioFileIOService audioFileIOService = null;

	public LibraryServiceImpl()
	{
	}

	private List<HibernatePersistedBeanDefinition> hibernateBeanDefs = new ArrayList<HibernatePersistedBeanDefinition>();

	@Override
	public List<HibernatePersistedBeanDefinition> listHibernatePersistedBeanDefinitions()
	{
		hibernateBeanDefs.add( new HibernatePersistedBeanDefinition( ReflectionUtils.getClassPackageAsPath( this ) +
				"/hbm/LibraryEntry.hbm.xml",
				databaseTablePrefix ) );

		hibernateBeanDefs.add( new HibernatePersistedBeanDefinition( ReflectionUtils.getClassPackageAsPath( this ) +
				"/hbm/CuePoint.hbm.xml",
				databaseTablePrefix ) );

		return hibernateBeanDefs;
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( hibernateSessionService == null || audioFileIOService == null )
		{
			throw new ComponentConfigurationException( "Missing dependencies. Cannot init()");
		}
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public LibraryEntry addFileToLibrary( File fileForEntry ) throws UnsupportedAudioFileException, DatastoreException, MAConstraintViolationException, NoSuchHibernateSessionException
	{
		LibraryEntry newEntry = null;
		try
		{
			Session hibernateSession = ThreadLocalSessionResource.getSessionResource();
			ArrayList<CuePoint> cueList = new ArrayList<CuePoint>();
			CuePoint trackStartCuePoint = new CuePoint( -1, 0, "track_start" );
			cueList.add( trackStartCuePoint );

			String title = fileForEntry.getName();
			String location = fileForEntry.getAbsolutePath();

			// This will let us test if it's a valid file format
			StaticMetadata sm = audioFileIOService.sniffFileFormatOfFile( location );

			int numChannels = sm.numChannels;
			long numFrames = sm.numFrames;
			int sampleRate = sm.sampleRate;

			newEntry = new LibraryEntry( -1, cueList, numChannels, sampleRate, numFrames, title, location );
			hibernateSession.save( trackStartCuePoint );
			hibernateSession.save( newEntry );
			return newEntry;
		}
		catch( HibernateException he )
		{
			String msg = "HibernateException caught adding file to library: " + he.toString();
			HibernateExceptionHandler.rethrowJdbcAsDatastoreAndConstraintAsItself( he, log, msg, HibernateExceptionHandler.ALL_ARE_ERRORS );
		}
		catch( Exception e )
		{
			String msg = "Exception caught attempting to add file to library:" + e.toString();
			log.error( msg, e );
			throw new DatastoreException( msg, e );
		}
		return newEntry;
	}

	@Override
	public LibraryEntry findLibraryEntryByFile( File file ) throws RecordNotFoundException, NoSuchHibernateSessionException, DatastoreException
	{
		LibraryEntry retVal = null;
		try
		{
			HibernateQueryBuilder queryBuilder = new HibernateQueryBuilder( log );
			Session hibernateSession = ThreadLocalSessionResource.getSessionResource();
			String hqlString = "from LibraryEntry where location = :location";
			queryBuilder.initQuery( hibernateSession, hqlString );
			String locationToFind = file.getAbsolutePath();
			queryBuilder.setString( "location", locationToFind );
			Query q = queryBuilder.buildQuery();
			retVal = (LibraryEntry)q.uniqueResult();
			if( retVal == null )
			{
				throw new RecordNotFoundException();
			}
		}
		catch( HibernateException he )
		{
			String msg = "HibernateException caught adding file to library: " + he.toString();
			HibernateExceptionHandler.rethrowAsDatastoreLogAll( he, log, msg );
		}

		return retVal;
	}

	public void setDatabaseTablePrefix( String databaseTablePrefix )
	{
		this.databaseTablePrefix = databaseTablePrefix;
	}

	public void setHibernateSessionService(
			HibernateSessionService hibernateSessionService )
	{
		this.hibernateSessionService = hibernateSessionService;
	}

	public void setAudioFileIOService( AudioFileIOService audioFileIOService )
	{
		this.audioFileIOService = audioFileIOService;
	}

}
