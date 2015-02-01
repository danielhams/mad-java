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

package uk.co.modularaudio.util.springhibernate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericApplicationContext;

import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.hibernate.component.ComponentWithHibernatePersistence;
import uk.co.modularaudio.util.hibernate.component.HibernatePersistedBeanDefinition;
import uk.co.modularaudio.util.spring.BeanInstantiationListAsPostProcessor;
import uk.co.modularaudio.util.spring.SpringContextHelper;

public class SpringHibernateContextHelper implements SpringContextHelper
{
	private static Log log = LogFactory.getLog( SpringHibernateContextHelper.class.getName() );

	@Override
	public void preContextDoThings() throws DatastoreException
	{
		// Do nothing
	}

	@Override
	public void preRefreshDoThings(final GenericApplicationContext appContext)
			throws DatastoreException
	{
		// Do nothing
	}

	@Override
	public void postRefreshDoThings( final GenericApplicationContext appContext, final BeanInstantiationListAsPostProcessor beanInstatiationList )
			throws DatastoreException
	{
		final Map<String,ComponentWithHibernatePersistence> components = appContext.getBeansOfType(ComponentWithHibernatePersistence.class);

		final List<HibernatePersistedBeanDefinition> hpbdList = new ArrayList<HibernatePersistedBeanDefinition>();

		for (final Iterator<ComponentWithHibernatePersistence> iter = components.values().iterator(); iter.hasNext();)
		{
			final ComponentWithHibernatePersistence component = iter.next();
			final List<HibernatePersistedBeanDefinition> hbmDefinitions = component.listHibernatePersistedBeanDefinitions();
			hpbdList.addAll( hbmDefinitions );
		}
		// Now make sure the hibernate session service is configured
		try
		{
			final HibernateSessionFactory hssi = appContext.getBean( HibernateSessionFactory.class );
			hssi.configureHibernate( hpbdList );
		}
		catch( final Exception e )
		{
			final String msg = "Exception caught initialising hibernate persisted components: " + e.toString();
			log.error( msg, e );
			throw new DatastoreException( msg, e );
		}
	}

	@Override
	public void preShutdownDoThings(final GenericApplicationContext appContext, final BeanInstantiationListAsPostProcessor beanInstatiationList )
			throws DatastoreException
	{
		// Do nothing
	}

}
