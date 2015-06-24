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

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.service.madcomponentui.AbstractMadComponentUiFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;

public class InternalComponentsUiFactory extends AbstractMadComponentUiFactory
{
	private InternalComponentsFactory internalComponentsFactory;

	@SuppressWarnings("rawtypes")
	private final Map<Class, Class> classToUiDefinition = new HashMap<Class, Class>();

	public InternalComponentsUiFactory()
	{
		// Definitions to UiDefinitions
		classToUiDefinition.put( FeedbackLinkConsumerMadDefinition.class, FeedbackLinkConsumerMadUiDefinition.class );
		classToUiDefinition.put( FeedbackLinkProducerMadDefinition.class, FeedbackLinkProducerMadUiDefinition.class );
		classToUiDefinition.put( AudioSystemTesterMadDefinition.class, AudioSystemTesterMadUiDefinition.class );
		classToUiDefinition.put( BlockingWriteRingMadDefinition.class, BlockingWriteRingMadUiDefinition.class );
		classToUiDefinition.put( FadeInMadDefinition.class, FadeInMadUiDefinition.class );
		classToUiDefinition.put( FadeOutMadDefinition.class, FadeOutMadUiDefinition.class );
	}

	public void setInternalComponentsFactory( final InternalComponentsFactory internalComponentsFactory )
	{
		this.internalComponentsFactory = internalComponentsFactory;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void setupTypeToDefinitionClasses() throws DatastoreException
	{
		try
		{
			final Collection<MadDefinition<?,?>> auds = internalComponentsFactory.listDefinitions();
			for( final MadDefinition<?,?> aud : auds )
			{
				final Class classToInstantiate = classToUiDefinition.get( aud.getClass() );
				if( classToInstantiate == null )
				{
					// Is a mad instance without a UI, carry on
					continue;
				}
				final Class[] constructorParamTypes = new Class[] {
						BufferedImageAllocator.class,
						aud.getClass(),
						ComponentImageFactory.class,
						String.class };
				final Object[] constructorParams = new Object[] {
						bufferedImageAllocationService,
						aud,
						componentImageFactory,
						imageRoot };
				final Constructor c = classToInstantiate.getConstructor( constructorParamTypes );
				final Object newInstance = c.newInstance( constructorParams );
				final MadUiDefinition instanceAsUiDefinition = (MadUiDefinition)newInstance;

				componentDefinitionToUiDefinitionMap.put( aud, instanceAsUiDefinition );
			}
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught setting up UI definitions: " + e.toString();
			throw new DatastoreException( msg, e );
		}
	}
}
