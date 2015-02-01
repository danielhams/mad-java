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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class BeanInstantiationListAsPostProcessor implements BeanFactoryPostProcessor, BeanPostProcessor
{
//	private static Log log = LogFactory.getLog( BeanInstantiationListAsPostProcessor.class.getName() );

	private final ArrayList<Object> beansInInitOrder = new ArrayList<Object>();

	@Override
	public void postProcessBeanFactory( final ConfigurableListableBeanFactory bf )
			throws BeansException
	{
		// Add a bean post processor that will store the order in which things should be instantiated.
		bf.addBeanPostProcessor( this );
	}

	@Override
	public Object postProcessAfterInitialization( final Object beanObject, final String beanName )
			throws BeansException
	{
//		log.debug("postProcessBean with " + beanObject.toString() + " and " + beanName );
		beansInInitOrder.add( beanObject );
		return beanObject;
	}

	@Override
	public Object postProcessBeforeInitialization( final Object arg0, final String arg1 )
			throws BeansException
	{
		// Don't do anything.
		return arg0;
	}

	public List<Object> asList()
	{
		return beansInInitOrder;
	}

}
