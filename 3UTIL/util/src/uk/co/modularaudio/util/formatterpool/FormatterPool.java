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

package uk.co.modularaudio.util.formatterpool;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.pooling.common.FactoryProductionException;
import uk.co.modularaudio.util.pooling.common.FixedSizePool;
import uk.co.modularaudio.util.pooling.common.Resource;
import uk.co.modularaudio.util.pooling.common.ResourceNotAvailableException;

public final class FormatterPool
{
	private static Log log = LogFactory.getLog( FormatterPool.class.getName() );

	private static FormatterPool sp = new FormatterPool();

	public final static FormatterPool getFormatterPool()
	{
		return sp;
	}

	public Formatter getFormatter()
	{
		try
		{
			final FormatterResource fr = (FormatterResource) poolObj.useResource();
			final Formatter f = fr.getFormatter();
			resourceToFormatterMap.put( fr, f );
			formatterToResourceMap.put( f, fr );
			return f;
		}
		catch (final ResourceNotAvailableException e)
		{
			return new Formatter( Locale.ENGLISH );
		}
	}

	public void returnFormatter( final Formatter f )
	{
		final FormatterResource fr = formatterToResourceMap.get( f );
		if( fr == null )
		{
			log.error("How the..");
		}
		fr.reset();
//		formatterToResourceMap.remove( f );
//		resourceToFormatterMap.remove( fr );
		poolObj.releaseResource( fr );
	}

	private FormatterPool()
	{
		poolObj = new FixedSizePool( 40, new FormatterFactory() );
		try
		{
			poolObj.init();
		}
		catch (final FactoryProductionException e)
		{
			log.error( e );
		}
	}

	private FixedSizePool poolObj = null;
	private final Map<Resource,Formatter> resourceToFormatterMap = new HashMap<Resource, Formatter>();
	private final Map<Formatter, FormatterResource> formatterToResourceMap = new HashMap<Formatter, FormatterResource>();

	@Override
	protected void finalize() throws Throwable
	{
		poolObj.shutdown();
		super.finalize();
	}
}
