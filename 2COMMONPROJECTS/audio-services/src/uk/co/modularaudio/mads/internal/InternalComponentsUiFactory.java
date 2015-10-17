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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.internal.audiosystemtester.mu.AudioSystemTesterMadDefinition;
import uk.co.modularaudio.mads.internal.audiosystemtester.mu.AudioSystemTesterMadInstance;
import uk.co.modularaudio.mads.internal.audiosystemtester.ui.AudioSystemTesterMadUiDefinition;
import uk.co.modularaudio.mads.internal.audiosystemtester.ui.AudioSystemTesterMadUiInstance;
import uk.co.modularaudio.mads.internal.blockingwritering.mu.BlockingWriteRingMadDefinition;
import uk.co.modularaudio.mads.internal.blockingwritering.mu.BlockingWriteRingMadInstance;
import uk.co.modularaudio.mads.internal.blockingwritering.ui.BlockingWriteRingMadUiDefinition;
import uk.co.modularaudio.mads.internal.blockingwritering.ui.BlockingWriteRingMadUiInstance;
import uk.co.modularaudio.mads.internal.fade.mu.FadeInMadDefinition;
import uk.co.modularaudio.mads.internal.fade.mu.FadeInMadInstance;
import uk.co.modularaudio.mads.internal.fade.mu.FadeOutMadDefinition;
import uk.co.modularaudio.mads.internal.fade.mu.FadeOutMadInstance;
import uk.co.modularaudio.mads.internal.fade.ui.FadeInMadUiDefinition;
import uk.co.modularaudio.mads.internal.fade.ui.FadeInMadUiInstance;
import uk.co.modularaudio.mads.internal.fade.ui.FadeOutMadUiDefinition;
import uk.co.modularaudio.mads.internal.fade.ui.FadeOutMadUiInstance;
import uk.co.modularaudio.mads.internal.feedbacklink.mu.FeedbackLinkConsumerMadDefinition;
import uk.co.modularaudio.mads.internal.feedbacklink.mu.FeedbackLinkConsumerMadInstance;
import uk.co.modularaudio.mads.internal.feedbacklink.mu.FeedbackLinkProducerMadDefinition;
import uk.co.modularaudio.mads.internal.feedbacklink.mu.FeedbackLinkProducerMadInstance;
import uk.co.modularaudio.mads.internal.feedbacklink.ui.FeedbackLinkConsumerMadUiDefinition;
import uk.co.modularaudio.mads.internal.feedbacklink.ui.FeedbackLinkConsumerMadUiInstance;
import uk.co.modularaudio.mads.internal.feedbacklink.ui.FeedbackLinkProducerMadUiDefinition;
import uk.co.modularaudio.mads.internal.feedbacklink.ui.FeedbackLinkProducerMadUiInstance;
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

	private final Map<String, Span> madIdToSpan = new HashMap<String, Span>();

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
		if( componentInstance instanceof FeedbackLinkConsumerMadInstance )
		{
			return new FeedbackLinkConsumerMadUiInstance(
					(FeedbackLinkConsumerMadInstance) componentInstance, flConsumerMud );
		}
		else if( componentInstance instanceof FeedbackLinkProducerMadInstance )
		{
			return new FeedbackLinkProducerMadUiInstance(
					(FeedbackLinkProducerMadInstance) componentInstance, flProducerMud );
		}
		else if( componentInstance instanceof AudioSystemTesterMadInstance )
		{
			return new AudioSystemTesterMadUiInstance(
					(AudioSystemTesterMadInstance)componentInstance, asTesterMud );
		}
		else if( componentInstance instanceof BlockingWriteRingMadInstance )
		{
			return new BlockingWriteRingMadUiInstance(
					(BlockingWriteRingMadInstance)componentInstance, bwrMud );
		}
		else if( componentInstance instanceof FadeInMadInstance )
		{
			return new FadeInMadUiInstance(
					(FadeInMadInstance)componentInstance, fadeInMud );
		}
		else if( componentInstance instanceof FadeOutMadInstance )
		{
			return new FadeOutMadUiInstance(
					(FadeOutMadInstance)componentInstance, fadeOutMud );
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
		return madIdToSpan.get( definition.getId() );
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
					flConsumerMd, componentImageFactory );
			muds.add( flConsumerMud );
			madIdToSpan.put( FeedbackLinkConsumerMadDefinition.DEFINITION_ID, flConsumerMud.getCellSpan() );

			final FeedbackLinkProducerMadDefinition flProducerMd =
					(FeedbackLinkProducerMadDefinition)componentService.findDefinitionById(
							FeedbackLinkProducerMadDefinition.DEFINITION_ID );
			flProducerMud = new FeedbackLinkProducerMadUiDefinition( bufferedImageAllocationService,
 					flProducerMd, componentImageFactory );
			muds.add( flProducerMud );
			madIdToSpan.put( FeedbackLinkProducerMadDefinition.DEFINITION_ID, flProducerMud.getCellSpan() );

			final AudioSystemTesterMadDefinition asMd =
					(AudioSystemTesterMadDefinition)componentService.findDefinitionById(
							AudioSystemTesterMadDefinition.DEFINITION_ID );
			asTesterMud = new AudioSystemTesterMadUiDefinition( bufferedImageAllocationService,
					asMd, componentImageFactory );
			muds.add( asTesterMud );
			madIdToSpan.put( AudioSystemTesterMadDefinition.DEFINITION_ID, asTesterMud.getCellSpan() );

			final BlockingWriteRingMadDefinition bwrMd =
					(BlockingWriteRingMadDefinition)componentService.findDefinitionById(
							BlockingWriteRingMadDefinition.DEFINITION_ID );
			bwrMud = new BlockingWriteRingMadUiDefinition( bufferedImageAllocationService,
					bwrMd, componentImageFactory );
			muds.add( bwrMud );
			madIdToSpan.put( BlockingWriteRingMadDefinition.DEFINITION_ID, bwrMud.getCellSpan() );

			final FadeInMadDefinition fiMd =
					(FadeInMadDefinition)componentService.findDefinitionById(
							FadeInMadDefinition.DEFINITION_ID );
			fadeInMud = new FadeInMadUiDefinition( bufferedImageAllocationService,
					fiMd, componentImageFactory );
			muds.add( fadeInMud );
			madIdToSpan.put( FadeInMadDefinition.DEFINITION_ID, fadeInMud.getCellSpan() );

			final FadeOutMadDefinition foMd =
					(FadeOutMadDefinition)componentService.findDefinitionById(
							FadeOutMadDefinition.DEFINITION_ID );
			fadeOutMud = new FadeOutMadUiDefinition( bufferedImageAllocationService,
					foMd, componentImageFactory );
			muds.add( fadeOutMud );
			madIdToSpan.put( FadeOutMadDefinition.DEFINITION_ID, fadeOutMud.getCellSpan() );

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
