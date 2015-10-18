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

package uk.co.modularaudio.mads.subrack;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.subrack.mu.SubRackMadDefinition;
import uk.co.modularaudio.mads.subrack.mu.SubRackMadInstance;
import uk.co.modularaudio.mads.subrack.ui.SubRackMadUiDefinition;
import uk.co.modularaudio.mads.subrack.ui.SubRackMadUiInstance;
import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
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

public class SubRackComponentsUiFactory
	implements ComponentWithLifecycle, MadComponentUiFactory
{
	private static Log log = LogFactory.getLog( SubRackComponentsUiFactory.class.getName() );

	private MadComponentService componentService;
	private MadComponentUiService componentUiService;
	private SubRackComponentsFactory subRackComponentsFactory;
	private BufferedImageAllocationService bufferedImageAllocationService;
	private ComponentImageFactory componentImageFactory;

	private SubRackMadUiDefinition srMud;
	private final ArrayList<MadUiDefinition<?,?>> muds = new ArrayList<MadUiDefinition<?,?>>();

	public void setSubRackComponentsFactory( final SubRackComponentsFactory subRackComponentsFactory )
	{
		this.subRackComponentsFactory = subRackComponentsFactory;
	}

	public void setComponentUiService( final MadComponentUiService componentUiService )
	{
		this.componentUiService = componentUiService;
	}

	public void setComponentService( final MadComponentService componentService )
	{
		this.componentService = componentService;
	}

	public void setBufferedImageAllocationService( final BufferedImageAllocationService bufferedImageAllocationService )
	{
		this.bufferedImageAllocationService = bufferedImageAllocationService;
	}

	public void setComponentImageFactory( final ComponentImageFactory componentImageFactory )
	{
		this.componentImageFactory = componentImageFactory;
	}

	@Override
	public List<MadUiDefinition<?, ?>> listComponentUiDefinitions()
	{
		return muds;
	}

	@Override
	public IMadUiInstance<?, ?> createNewComponentUiInstanceForComponent( final MadInstance<?, ?> componentInstance )
			throws DatastoreException, RecordNotFoundException
	{
		if( componentInstance instanceof SubRackMadInstance )
		{
			return srMud.createNewUiInstanceUT( componentInstance );
		}
		else
		{
			throw new RecordNotFoundException( "No such mad" );
		}
	}

	@Override
	public void destroyUiInstance( final IMadUiInstance<?, ?> uiInstanceToDestroy )
			throws DatastoreException, RecordNotFoundException
	{
		((SubRackMadUiInstance)uiInstanceToDestroy).cleanup();
	}

	@Override
	public Span getUiSpanForDefinition( final MadDefinition<?, ?> definition )
			throws DatastoreException, RecordNotFoundException
	{
		if( definition.getId().equals( SubRackMadDefinition.DEFINITION_ID ) )
		{
			return srMud.getCellSpan();
		}
		else
		{
			throw new RecordNotFoundException( "No such mad" );
		}
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( componentService == null ||
				componentUiService == null ||
				subRackComponentsFactory == null ||
				bufferedImageAllocationService == null ||
				componentImageFactory == null )
		{
			throw new ComponentConfigurationException( "Service missing dependencies. Check config." );
		}

		try
		{
			final SubRackMadDefinition srMd = (SubRackMadDefinition)componentService.findDefinitionById( SubRackMadDefinition.DEFINITION_ID );
			srMud = new SubRackMadUiDefinition( bufferedImageAllocationService, srMd, componentImageFactory );

			muds.add( srMud );

			componentUiService.registerComponentUiFactory( this );
		}
		catch( final DatastoreException | RecordNotFoundException e )
		{
			throw new ComponentConfigurationException( "Unable to create UI definitions: " + e.toString(), e );
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
