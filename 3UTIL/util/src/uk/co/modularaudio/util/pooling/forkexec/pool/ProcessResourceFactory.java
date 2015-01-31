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

package uk.co.modularaudio.util.pooling.forkexec.pool;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.pooling.common.Factory;
import uk.co.modularaudio.util.pooling.common.FactoryProductionException;
import uk.co.modularaudio.util.pooling.common.Resource;

/**
 * @author dan
 *
 */
public class ProcessResourceFactory implements Factory
{
	private static Log log = LogFactory.getLog( ProcessResourceFactory.class.getName() );

	private final String[] resourceCmdArray;
	private final String resourceOutputTerminator;

	public ProcessResourceFactory( final String[] cmdArray, final String outputTerminator )
	{
		if (log.isTraceEnabled())
		{
			log.trace( "Creating process resource factory term is " + outputTerminator );
			for (int i = 0; i < cmdArray.length; i++)
			{
				log.trace( "The cmdArray " + i + " is " + cmdArray[i] );
			}
		}
		this.resourceCmdArray = cmdArray;
		this.resourceOutputTerminator = outputTerminator;
	}

	/**
	 * @see uk.co.modularaudio.util.pooling.common.Factory#createResource()
	 */
	@Override
	public Resource createResource() throws FactoryProductionException
	{
		MultishotProcessResource retVal;
		try
		{
			log.trace( "creating resource.." );
			retVal = new MultishotProcessResource( resourceCmdArray, resourceOutputTerminator );
			log.trace( "created resource.." );
		}
		catch (final IOException ioe)
		{
			throw new FactoryProductionException( ioe.toString() );
		}
		return retVal;
	}

	/**
	 * @see uk.co.modularaudio.util.pooling.common.Factory#init()
	 */
	@Override
	public void init() throws FactoryProductionException
	{
		boolean wasError = false;
		// Try and create one to see if the configuration is good.
		MultishotProcessResource tester = null;
		try
		{
			tester = new MultishotProcessResource( resourceCmdArray, resourceOutputTerminator );
		}
		catch (final IOException ioe)
		{
			ioe.printStackTrace();
			wasError = true;
		}

		if (wasError)
		{
			throw new FactoryProductionException( "Factory unable to create process resource" );
		}
		else
		{
			try
			{
				tester.close();
			}
			catch (final Exception e)
			{
			}
		}
	}

	public void shutdown()
	{
	}

}
