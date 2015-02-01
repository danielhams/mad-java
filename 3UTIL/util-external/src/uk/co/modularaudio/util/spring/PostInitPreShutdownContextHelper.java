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

package uk.co.modularaudio.util.spring;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericApplicationContext;

import uk.co.modularaudio.util.component.ComponentWithPostInitPreShutdown;
import uk.co.modularaudio.util.exception.DatastoreException;

public class PostInitPreShutdownContextHelper implements SpringContextHelper
{
	private static Log log = LogFactory.getLog( PostInitPreShutdownContextHelper.class.getName() );

	@Override
	public void preContextDoThings() throws DatastoreException {}

	@Override
	public void preRefreshDoThings(final GenericApplicationContext appContext)
		throws DatastoreException
	{}

	@Override
	public void postRefreshDoThings( final GenericApplicationContext appContext, final BeanInstantiationListAsPostProcessor beanInstantiationList )
			throws DatastoreException
	{
		// Process them in the order that beans were created
		for( final Object o : beanInstantiationList.asList() )
		{
			if( o instanceof ComponentWithPostInitPreShutdown )
			{
				final ComponentWithPostInitPreShutdown bean = (ComponentWithPostInitPreShutdown)o;
				try
				{
					bean.postInit();
				}
				catch(final Exception e)
				{
					final String msg = "Exception caught calling postInit() method of bean " +
						bean.getClass().getName() + ": " + e.toString();
					log.error( msg, e );
					throw new DatastoreException( msg, e );
				}
			}
		}
	}

	@Override
	public void preShutdownDoThings(final GenericApplicationContext appContext, final BeanInstantiationListAsPostProcessor beanInstantiationList )
			throws DatastoreException
	{
		// Process them in reverse order to which the beans were created
		final List<Object> normalOrder = beanInstantiationList.asList();
		final ArrayList<Object> reverseOrder = new ArrayList<Object>();
		for( int i = normalOrder.size() - 1 ; i > 0 ; i-- )
		{
			final Object component = normalOrder.get( i );
			if( component instanceof ComponentWithPostInitPreShutdown )
			{
				reverseOrder.add( component );
			}
		}

		for( final Object o : reverseOrder )
		{
			if( o instanceof ComponentWithPostInitPreShutdown )
			{
				final ComponentWithPostInitPreShutdown bean = (ComponentWithPostInitPreShutdown)o;
				bean.preShutdown();
			}
		}
	}

}
