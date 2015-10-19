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

package uk.co.modularaudio.mads.rackmasterio;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.rackmasterio.mu.RackMasterIOMadDefinition;
import uk.co.modularaudio.mads.rackmasterio.ui.RackMasterIOMadUiDefinition;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.madcomponentui.MadComponentUiFactory;
import uk.co.modularaudio.service.madcomponentui.MadComponentUiService;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.table.Span;

public class RackMasterIOComponentsUiFactory
	implements ComponentWithLifecycle, MadComponentUiFactory
{
	private static Log log = LogFactory.getLog( RackMasterIOComponentsUiFactory.class.getName() );


	private MadComponentService componentService;
	private MadComponentUiService componentUiService;
	private RackMasterIOComponentsFactory rackMasterIOComponentsFactory;

	private final ArrayList<MadUiDefinition<?,?>> muds = new ArrayList<MadUiDefinition<?,?>>();
	private RackMasterIOMadUiDefinition rmMud;

	public void setRackMasterIOComponentsFactory( final RackMasterIOComponentsFactory internalComponentsFactory )
	{
		this.rackMasterIOComponentsFactory = internalComponentsFactory;
	}

	public void setComponentService( final MadComponentService componentService )
	{
		this.componentService = componentService;
	}

	public void setComponentUiService( final MadComponentUiService componentUiService )
	{
		this.componentUiService = componentUiService;
	}

	@Override
	public List<MadUiDefinition<?, ?>> listComponentUiDefinitions()
	{
		return muds;
	}

	@Override
	public IMadUiInstance<?, ?> createUiInstanceForMad( final MadInstance<?, ?> componentInstance )
			throws DatastoreException, RecordNotFoundException
	{
		final IMadUiInstance<?, ?> muui = rmMud.createNewUiInstanceUT( componentInstance );
		return muui;
	}

	@Override
	public void cleanupUiInstance( final IMadUiInstance<?, ?> uiInstanceToDestroy )
			throws DatastoreException, RecordNotFoundException
	{
	}

	@Override
	public Span getUiSpanForDefinition( final MadDefinition<?, ?> definition )
			throws DatastoreException, RecordNotFoundException
	{
		return rmMud.getCellSpan();
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( componentUiService == null ||
				rackMasterIOComponentsFactory == null )
		{
			throw new ComponentConfigurationException( "Service missing dependencies. Check config." );
		}

		try
		{
			final RackMasterIOMadDefinition rmMd = (RackMasterIOMadDefinition) componentService.findDefinitionById( RackMasterIOMadDefinition.DEFINITION_ID );
			rmMud = new RackMasterIOMadUiDefinition( rmMd );
			muds.add( rmMud );

			componentUiService.registerComponentUiFactory( this );
		}
		catch( final DatastoreException | RecordNotFoundException e )
		{
			throw new ComponentConfigurationException( "Unable to create muds: " + e.toString(), e );
		}
	}

	@Override
	public void destroy()
	{
		try
		{
			componentUiService.unregisterComponentUiFactory( this );
		}
		catch( final DatastoreException e )
		{
			log.error( e );
		}
	}

}
