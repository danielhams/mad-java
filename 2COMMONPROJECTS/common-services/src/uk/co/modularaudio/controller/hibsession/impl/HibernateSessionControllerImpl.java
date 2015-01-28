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

package uk.co.modularaudio.controller.hibsession.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import uk.co.modularaudio.controller.hibsession.HibernateSessionController;
import uk.co.modularaudio.service.hibsession.HibernateSessionService;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.hibernate.NoSuchHibernateSessionException;
import uk.co.modularaudio.util.hibernate.ThreadLocalSessionResource;

/**
 * Vertical controller managing hibernate session It is in charge of configuring hibernate and providing fresh sessions
 * it depends of the session service obviously, but it depends too on the configuration service.
 *
 */
public class HibernateSessionControllerImpl implements HibernateSessionController
{
	private static Log log = LogFactory.getLog( HibernateSessionControllerImpl.class.getName() );

	private HibernateSessionService hibernateSessionService;

	public void setHibernateSessionService(final HibernateSessionService sessionService)
	{
		this.hibernateSessionService = sessionService;
	}

	/**
	 * Return a session in the ThreadLocalRessource class ready to be used by the components
	 */
	@Override
	public void getThreadSession()
	{
		final Session tmpSession = hibernateSessionService.getSession();
		ThreadLocalSessionResource.setSessionResource( tmpSession );
	}

	@Override
	public void releaseThreadSession() throws NoSuchHibernateSessionException
	{
		final Session currentThreadSession = ThreadLocalSessionResource.getSessionResource();
		ThreadLocalSessionResource.setSessionResource( null );
		hibernateSessionService.releaseSession( currentThreadSession );
	}

	@Override
	public void releaseThreadSessionNoException()
	{
		try
		{
			final Session currentThreadSession = ThreadLocalSessionResource.getSessionResource();
			ThreadLocalSessionResource.setSessionResource( null );
			hibernateSessionService.releaseSession( currentThreadSession );
		}
		catch( final Throwable t )
		{
			if( log.isErrorEnabled() )
			{
				log.error("Throwable caught releasing thread session: " + t.toString(), t );
			}
		}
	}

	public void init() throws ComponentConfigurationException
	{
	}

	public void destroy()
	{
	}
}
