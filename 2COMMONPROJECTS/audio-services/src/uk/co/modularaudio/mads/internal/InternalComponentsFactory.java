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

import java.util.HashMap;
import java.util.Map;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.mads.internal.audiosystemtester.mu.AudioSystemTesterMadDefinition;
import uk.co.modularaudio.mads.internal.audiosystemtester.mu.AudioSystemTesterMadInstance;
import uk.co.modularaudio.mads.internal.blockingwritering.mu.BlockingWriteRingMadDefinition;
import uk.co.modularaudio.mads.internal.blockingwritering.mu.BlockingWriteRingMadInstance;
import uk.co.modularaudio.mads.internal.fade.mu.FadeInMadDefinition;
import uk.co.modularaudio.mads.internal.fade.mu.FadeInMadInstance;
import uk.co.modularaudio.mads.internal.fade.mu.FadeOutMadDefinition;
import uk.co.modularaudio.mads.internal.fade.mu.FadeOutMadInstance;
import uk.co.modularaudio.mads.internal.feedbacklink.mu.FeedbackLinkConsumerMadDefinition;
import uk.co.modularaudio.mads.internal.feedbacklink.mu.FeedbackLinkConsumerMadInstance;
import uk.co.modularaudio.mads.internal.feedbacklink.mu.FeedbackLinkProducerMadDefinition;
import uk.co.modularaudio.mads.internal.feedbacklink.mu.FeedbackLinkProducerMadInstance;
import uk.co.modularaudio.mads.internal.paramfade.mu.PFadeInMadDefinition;
import uk.co.modularaudio.mads.internal.paramfade.mu.PFadeInMadInstance;
import uk.co.modularaudio.mads.internal.paramfade.mu.PFadeOutMadDefinition;
import uk.co.modularaudio.mads.internal.paramfade.mu.PFadeOutMadInstance;
import uk.co.modularaudio.service.madcomponent.AbstractMadComponentFactory;
import uk.co.modularaudio.util.audio.mad.MadCreationContext;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;

public class InternalComponentsFactory extends AbstractMadComponentFactory
{
	// Definitions to instances
	private final Map<Class<? extends MadDefinition<?,?>>, Class<? extends MadInstance<?,?>> > defClassToInsClassMap =
			new HashMap<Class<? extends MadDefinition<?,?>>, Class<? extends MadInstance<?,?>>>();

	private AdvancedComponentsFrontController advancedComponentsFrontController;

	private InternalComponentsCreationContext creationContext;

	public InternalComponentsFactory()
	{
		defClassToInsClassMap.put( FadeOutMadDefinition.class, FadeOutMadInstance.class );
		defClassToInsClassMap.put( FadeInMadDefinition.class, FadeInMadInstance.class );
		defClassToInsClassMap.put( FeedbackLinkConsumerMadDefinition.class, FeedbackLinkConsumerMadInstance.class );
		defClassToInsClassMap.put( FeedbackLinkProducerMadDefinition.class, FeedbackLinkProducerMadInstance.class );
		defClassToInsClassMap.put( AudioSystemTesterMadDefinition.class, AudioSystemTesterMadInstance.class );
		defClassToInsClassMap.put( PFadeInMadDefinition.class, PFadeInMadInstance.class );
		defClassToInsClassMap.put( PFadeOutMadDefinition.class, PFadeOutMadInstance.class );
		defClassToInsClassMap.put( BlockingWriteRingMadDefinition.class, BlockingWriteRingMadInstance.class );
	}

	@Override
	public Map<Class<? extends MadDefinition<?, ?>>, Class<? extends MadInstance<?, ?>>> provideDefClassToInsClassMap()
			throws ComponentConfigurationException
	{
		return defClassToInsClassMap;
	}

	@Override
	public MadCreationContext getCreationContext()
	{
		return creationContext;
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( advancedComponentsFrontController == null )
		{
			final String msg = "InternalComponentsFactory has missing service dependencies. Check configuration";
			throw new ComponentConfigurationException( msg );
		}

		creationContext = new InternalComponentsCreationContext( advancedComponentsFrontController );

		super.init();
	}

	public void setAdvancedComponentsFrontController( final AdvancedComponentsFrontController advancedComponentsFrontController )
	{
		this.advancedComponentsFrontController = advancedComponentsFrontController;
	}
}
