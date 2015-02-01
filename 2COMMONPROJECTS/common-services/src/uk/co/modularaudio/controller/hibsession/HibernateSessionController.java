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

package uk.co.modularaudio.controller.hibsession;

import uk.co.modularaudio.util.hibernate.NoSuchHibernateSessionException;
import uk.co.modularaudio.util.hibernate.ThreadLocalSessionResource;

/**
 * <p>Entry point for operations related to obtaining and releasing hibernate sessions.</p>
 *
 * @author dan
 */
public interface HibernateSessionController
{
	/**
	 * <p>Obtain a hibernate session and fill in the thread
	 * local structure for services to use as required.</p>
	 * <p>No transaction is started by default.</p>
	 * @see ThreadLocalSessionResource#getSessionResource()
	 */
	public void getThreadSession();
	/**
	 * <p>Return a thread local session.</p>
	 * <p>By default if a transaction is active it will be
	 * rolled back.</p>
	 * @throws NoSuchHibernateSessionException if no thread local session is found
	 */
	public void releaseThreadSession() throws NoSuchHibernateSessionException;
	/**
	 * <p>Return a thread local session.</p>
	 * <p>No exceptions will be thrown calling this method.</p>
	 */
	public void releaseThreadSessionNoException();
}
