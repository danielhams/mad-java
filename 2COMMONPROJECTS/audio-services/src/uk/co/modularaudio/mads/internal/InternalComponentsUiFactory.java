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
import uk.co.modularaudio.mads.internal.feedbacklink.mu.FeedbackLinkConsumerMadDefinition;
import uk.co.modularaudio.mads.internal.feedbacklink.mu.FeedbackLinkProducerMadDefinition;
import uk.co.modularaudio.mads.internal.feedbacklink.ui.FeedbackLinkConsumerMadUiDefinition;
import uk.co.modularaudio.mads.internal.feedbacklink.ui.FeedbackLinkProducerMadUiDefinition;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.service.madcomponentui.factoryshell.AbstractMadComponentUiFactory;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;

public class InternalComponentsUiFactory extends AbstractMadComponentUiFactory
{
	private InternalComponentsFactory internalComponentsFactory = null;

	public void setInternalComponentsFactory( InternalComponentsFactory internalComponentsFactory )
	{
		this.internalComponentsFactory = internalComponentsFactory;
	}

	@SuppressWarnings("rawtypes")
	private Map<Class, Class> classToUiDefinition = new HashMap<Class, Class>();
	
	public InternalComponentsUiFactory()
	{
		// Definitions to UiDefinitions
		classToUiDefinition.put( FeedbackLinkConsumerMadDefinition.class, FeedbackLinkConsumerMadUiDefinition.class );
		classToUiDefinition.put( FeedbackLinkProducerMadDefinition.class, FeedbackLinkProducerMadUiDefinition.class );
		classToUiDefinition.put( AudioSystemTesterMadDefinition.class, AudioSystemTesterMadUiDefinition.class );
		classToUiDefinition.put( BlockingWriteRingMadDefinition.class, BlockingWriteRingMadUiDefinition.class );
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void setupTypeToDefinitionClasses() throws DatastoreException
	{
		try
		{
			Collection<MadDefinition<?,?>> auds = internalComponentsFactory.listDefinitions();
			for( MadDefinition<?,?> aud : auds )
			{
				Class classToInstantiate = classToUiDefinition.get( aud.getClass() );
				if( classToInstantiate == null )
				{
					// Is a mad instance without a UI, carry on
					continue;
				}
				Class[] constructorParamTypes = new Class[] {
						BufferedImageAllocator.class,
						aud.getClass(),
						ComponentImageFactory.class,
						String.class };
				Object[] constructorParams = new Object[] {
						bufferedImageAllocationService,
						aud,
						componentImageFactory,
						imageRoot };
				Constructor c = classToInstantiate.getConstructor( constructorParamTypes );
				Object newInstance = c.newInstance( constructorParams );
				MadUiDefinition instanceAsUiDefinition = (MadUiDefinition)newInstance;
				
				componentDefinitionToUiDefinitionMap.put( aud, instanceAsUiDefinition );
			}
		}
		catch (Exception e)
		{
			String msg = "Exception caught setting up UI definitions: " + e.toString();
			throw new DatastoreException( msg, e );
		}
	}
}
