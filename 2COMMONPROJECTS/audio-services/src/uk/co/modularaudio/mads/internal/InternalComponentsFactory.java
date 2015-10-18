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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.mads.internal.audiosystemtester.mu.AudioSystemTesterMadDefinition;
import uk.co.modularaudio.mads.internal.blockingwritering.mu.BlockingWriteRingMadDefinition;
import uk.co.modularaudio.mads.internal.fade.mu.FadeInMadDefinition;
import uk.co.modularaudio.mads.internal.fade.mu.FadeOutMadDefinition;
import uk.co.modularaudio.mads.internal.feedbacklink.mu.FeedbackLinkConsumerMadDefinition;
import uk.co.modularaudio.mads.internal.feedbacklink.mu.FeedbackLinkProducerMadDefinition;
import uk.co.modularaudio.mads.internal.paramfade.mu.PFadeInMadDefinition;
import uk.co.modularaudio.mads.internal.paramfade.mu.PFadeOutMadDefinition;
import uk.co.modularaudio.service.madclassification.MadClassificationService;
import uk.co.modularaudio.service.madcomponent.MadComponentFactory;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class InternalComponentsFactory
	implements ComponentWithLifecycle, MadComponentFactory
{
	private static Log log = LogFactory.getLog( InternalComponentsFactory.class.getName() );

	private MadClassificationService classificationService;
	private MadComponentService componentService;

	private AdvancedComponentsFrontController advancedComponentsFrontController;

	private InternalComponentsCreationContext creationContext;

	private FadeOutMadDefinition fadeOutMD;
	private FadeInMadDefinition fadeInMD;
	private FeedbackLinkConsumerMadDefinition fblinkConsumerMD;
	private FeedbackLinkProducerMadDefinition fblinkProducerMD;
	private AudioSystemTesterMadDefinition asTesterMD;
	private PFadeOutMadDefinition pfadeOutMD;
	private PFadeInMadDefinition pfadeInMD;
	private BlockingWriteRingMadDefinition bwrMD;

	private final ArrayList<MadDefinition<?,?>> mds = new ArrayList<MadDefinition<?,?>>();

	private final Map<String, InternalMadDefinition> defIdToImd = new HashMap<String, InternalMadDefinition>();

	public InternalComponentsFactory()
	{
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( classificationService == null ||
				componentService == null ||
				advancedComponentsFrontController == null )
		{
			final String msg = "InternalComponentsFactory has missing service dependencies. Check configuration";
			throw new ComponentConfigurationException( msg );
		}

		creationContext = new InternalComponentsCreationContext( advancedComponentsFrontController );

		try
		{
			fadeOutMD = new FadeOutMadDefinition( creationContext, classificationService );
			mds.add( fadeOutMD );
			defIdToImd.put( FadeOutMadDefinition.DEFINITION_ID, fadeOutMD );
			fadeInMD = new FadeInMadDefinition( creationContext, classificationService );
			mds.add( fadeInMD );
			defIdToImd.put( FadeInMadDefinition.DEFINITION_ID, fadeInMD );
			fblinkConsumerMD = new FeedbackLinkConsumerMadDefinition( creationContext, classificationService );
			mds.add( fblinkConsumerMD );
			defIdToImd.put( FeedbackLinkConsumerMadDefinition.DEFINITION_ID, fblinkConsumerMD );
			fblinkProducerMD = new FeedbackLinkProducerMadDefinition( creationContext, classificationService );
			mds.add( fblinkProducerMD );
			defIdToImd.put( FeedbackLinkProducerMadDefinition.DEFINITION_ID, fblinkProducerMD );
			asTesterMD = new AudioSystemTesterMadDefinition( creationContext, classificationService );
			mds.add( asTesterMD );
			defIdToImd.put( AudioSystemTesterMadDefinition.DEFINITION_ID, asTesterMD );
			pfadeOutMD = new PFadeOutMadDefinition( creationContext, classificationService );
			mds.add( pfadeOutMD );
			defIdToImd.put( PFadeOutMadDefinition.DEFINITION_ID, pfadeOutMD );
			pfadeInMD = new PFadeInMadDefinition( creationContext, classificationService );
			mds.add( pfadeInMD );
			defIdToImd.put( PFadeInMadDefinition.DEFINITION_ID, pfadeInMD );
			bwrMD = new BlockingWriteRingMadDefinition( creationContext, classificationService );
			mds.add( bwrMD );
			defIdToImd.put( BlockingWriteRingMadDefinition.DEFINITION_ID, bwrMD );

			componentService.registerComponentFactory( this );
		}
		catch( final DatastoreException | RecordNotFoundException | MAConstraintViolationException e )
		{
			throw new ComponentConfigurationException( "Failed instantiating MADS: " + e.toString(), e );
		}
	}

	public void setAdvancedComponentsFrontController( final AdvancedComponentsFrontController advancedComponentsFrontController )
	{
		this.advancedComponentsFrontController = advancedComponentsFrontController;
	}

	@Override
	public Collection<MadDefinition<?, ?>> listDefinitions()
	{
		return mds;
	}

	@Override
	public MadInstance<?, ?> createInstanceForDefinition( final MadDefinition<?, ?> definition,
			final Map<MadParameterDefinition, String> parameterValues, final String instanceName )
		throws DatastoreException
	{
		final InternalMadDefinition md = defIdToImd.get( definition.getId() );
		if( md == null )
		{
			throw new DatastoreException( "Unknown MAD: " + definition.getName() );
		}
		else
		{
			return md.createInstance( parameterValues, instanceName );
		}
	}

	@Override
	public void destroyInstance( final MadInstance<?, ?> instanceToDestroy ) throws DatastoreException
	{
		instanceToDestroy.destroy();
	}

	@Override
	public void destroy()
	{
		try
		{
			componentService.unregisterComponentFactory( this );
		}
		catch( final DatastoreException e )
		{
			log.error( e );
		}
	}

	public void setClassificationService( final MadClassificationService classificationService )
	{
		this.classificationService = classificationService;
	}

	public void setComponentService( final MadComponentService componentService )
	{
		this.componentService = componentService;
	}
}
