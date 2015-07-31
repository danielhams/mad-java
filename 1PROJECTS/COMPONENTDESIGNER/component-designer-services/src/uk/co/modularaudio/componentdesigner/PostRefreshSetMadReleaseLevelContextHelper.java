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

package uk.co.modularaudio.componentdesigner;

import org.springframework.context.support.GenericApplicationContext;

import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.spring.BeanInstantiationListAsPostProcessor;
import uk.co.modularaudio.util.spring.SpringContextHelper;

public class PostRefreshSetMadReleaseLevelContextHelper implements SpringContextHelper
{
	private final boolean showAlpha;
	private final boolean showBeta;

	public PostRefreshSetMadReleaseLevelContextHelper( final boolean showAlpha, final boolean showBeta )
	{
		this.showAlpha = showAlpha;
		this.showBeta = showBeta;
	}

	@Override
	public void preContextDoThings() throws DatastoreException
	{
	}

	@Override
	public void preRefreshDoThings( final GenericApplicationContext appContext ) throws DatastoreException
	{
	}

	@Override
	public void postRefreshDoThings( final GenericApplicationContext appContext,
			final BeanInstantiationListAsPostProcessor beanInstantiationList ) throws DatastoreException
	{
		final MadComponentService componentService = appContext.getBean( MadComponentService.class );
		componentService.setReleaseLevel( showAlpha, showBeta );
	}

	@Override
	public void preShutdownDoThings( final GenericApplicationContext appContext,
			final BeanInstantiationListAsPostProcessor beanInstantiationList ) throws DatastoreException
	{
	}

}
