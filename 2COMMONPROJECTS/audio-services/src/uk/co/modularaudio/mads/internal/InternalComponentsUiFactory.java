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

package uk.co.modularaudio.mads.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.internal.audiosystemtester.mu.AudioSystemTesterMadDefinition;
import uk.co.modularaudio.mads.internal.audiosystemtester.ui.AudioSystemTesterMadUiDefinition;
import uk.co.modularaudio.mads.internal.blockingwritering.mu.BlockingWriteRingMadDefinition;
import uk.co.modularaudio.mads.internal.blockingwritering.ui.BlockingWriteRingMadUiDefinition;
import uk.co.modularaudio.mads.internal.fade.mu.FadeInMadDefinition;
import uk.co.modularaudio.mads.internal.fade.mu.FadeOutMadDefinition;
import uk.co.modularaudio.mads.internal.fade.ui.FadeInMadUiDefinition;
import uk.co.modularaudio.mads.internal.fade.ui.FadeOutMadUiDefinition;
import uk.co.modularaudio.mads.internal.feedbacklink.mu.FeedbackLinkConsumerMadDefinition;
import uk.co.modularaudio.mads.internal.feedbacklink.mu.FeedbackLinkProducerMadDefinition;
import uk.co.modularaudio.mads.internal.feedbacklink.ui.FeedbackLinkConsumerMadUiDefinition;
import uk.co.modularaudio.mads.internal.feedbacklink.ui.FeedbackLinkProducerMadUiDefinition;
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

public class InternalComponentsUiFactory
	implements ComponentWithLifecycle, MadComponentUiFactory
{
	private static Log log = LogFactory.getLog( InternalComponentsUiFactory.class.getName() );

	private MadComponentService componentService;
	private MadComponentUiService componentUiService;
	private BufferedImageAllocationService bufferedImageAllocationService;
	private ComponentImageFactory componentImageFactory;

	private InternalComponentsFactory internalComponentsFactory;

	private final ArrayList<MadUiDefinition<?,?>> muds = new ArrayList<MadUiDefinition<?,?>>();

	private FeedbackLinkConsumerMadUiDefinition flConsumerMud;
	private FeedbackLinkProducerMadUiDefinition flProducerMud;
	private AudioSystemTesterMadUiDefinition asTesterMud;
	private BlockingWriteRingMadUiDefinition bwrMud;
	private FadeInMadUiDefinition fadeInMud;
	private FadeOutMadUiDefinition fadeOutMud;

	private final HashMap<String, MadUiDefinition<?,?>> mdIdToMudMap = new HashMap<String, MadUiDefinition<?,?>>();

	public InternalComponentsUiFactory()
	{
	}

	public void setInternalComponentsFactory( final InternalComponentsFactory internalComponentsFactory )
	{
		this.internalComponentsFactory = internalComponentsFactory;
	}

	public void setComponentService( final MadComponentService componentService )
	{
		this.componentService = componentService;
	}

	public void setComponentUiService( final MadComponentUiService componentUiService )
	{
		this.componentUiService = componentUiService;
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
		final MadUiDefinition<?, ?> mud = mdIdToMudMap.get( componentInstance.getDefinition().getId() );
		if( mud != null )
		{
			return mud.createNewUiInstanceUT( componentInstance );
		}
		else
		{
			throw new RecordNotFoundException( "Unknown mad definition: " + componentInstance.getDefinition().getName() );
		}
	}

	@Override
	public void destroyUiInstance( final IMadUiInstance<?, ?> uiInstanceToDestroy )
			throws DatastoreException, RecordNotFoundException
	{
	}

	@Override
	public Span getUiSpanForDefinition( final MadDefinition<?, ?> definition )
			throws DatastoreException, RecordNotFoundException
	{
		final MadUiDefinition<?, ?> madUiDefinition = mdIdToMudMap.get( definition.getId() );
		if( madUiDefinition != null )
		{
			return madUiDefinition.getCellSpan();
		}
		else
		{
			throw new RecordNotFoundException();
		}
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( componentService == null ||
				componentUiService == null ||
				bufferedImageAllocationService == null ||
				componentImageFactory == null ||
				internalComponentsFactory == null )
		{
			throw new ComponentConfigurationException( "Service missing dependencies. Check config." );
		}

		try
		{
			final FeedbackLinkConsumerMadDefinition flConsumerMd =
					(FeedbackLinkConsumerMadDefinition)componentService.findDefinitionById(
							FeedbackLinkConsumerMadDefinition.DEFINITION_ID );
			flConsumerMud = new FeedbackLinkConsumerMadUiDefinition( bufferedImageAllocationService,
					flConsumerMd );
			muds.add( flConsumerMud );
			mdIdToMudMap.put( FeedbackLinkConsumerMadDefinition.DEFINITION_ID, flConsumerMud );

			final FeedbackLinkProducerMadDefinition flProducerMd =
					(FeedbackLinkProducerMadDefinition)componentService.findDefinitionById(
							FeedbackLinkProducerMadDefinition.DEFINITION_ID );
			flProducerMud = new FeedbackLinkProducerMadUiDefinition( bufferedImageAllocationService,
 					flProducerMd );
			muds.add( flProducerMud );
			mdIdToMudMap.put( FeedbackLinkProducerMadDefinition.DEFINITION_ID, flProducerMud );

			final AudioSystemTesterMadDefinition asMd =
					(AudioSystemTesterMadDefinition)componentService.findDefinitionById(
							AudioSystemTesterMadDefinition.DEFINITION_ID );
			asTesterMud = new AudioSystemTesterMadUiDefinition( bufferedImageAllocationService,
					asMd );
			muds.add( asTesterMud );
			mdIdToMudMap.put( AudioSystemTesterMadDefinition.DEFINITION_ID, asTesterMud );

			final BlockingWriteRingMadDefinition bwrMd =
					(BlockingWriteRingMadDefinition)componentService.findDefinitionById(
							BlockingWriteRingMadDefinition.DEFINITION_ID );
			bwrMud = new BlockingWriteRingMadUiDefinition( bufferedImageAllocationService,
					bwrMd );
			muds.add( bwrMud );
			mdIdToMudMap.put( BlockingWriteRingMadDefinition.DEFINITION_ID, bwrMud );

			final FadeInMadDefinition fiMd =
					(FadeInMadDefinition)componentService.findDefinitionById(
							FadeInMadDefinition.DEFINITION_ID );
			fadeInMud = new FadeInMadUiDefinition( bufferedImageAllocationService,
					fiMd );
			muds.add( fadeInMud );
			mdIdToMudMap.put( FadeInMadDefinition.DEFINITION_ID, fadeInMud );

			final FadeOutMadDefinition foMd =
					(FadeOutMadDefinition)componentService.findDefinitionById(
							FadeOutMadDefinition.DEFINITION_ID );
			fadeOutMud = new FadeOutMadUiDefinition( bufferedImageAllocationService,
					foMd );
			muds.add( fadeOutMud );
			mdIdToMudMap.put( FadeOutMadDefinition.DEFINITION_ID, fadeOutMud );

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
