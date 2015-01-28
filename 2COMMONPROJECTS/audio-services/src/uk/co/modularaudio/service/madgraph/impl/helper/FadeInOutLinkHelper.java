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

package uk.co.modularaudio.service.madgraph.impl.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.internal.fade.mu.FadeDefinitions;
import uk.co.modularaudio.mads.internal.fade.mu.FadeInMadDefinition;
import uk.co.modularaudio.mads.internal.fade.mu.FadeInMadInstance;
import uk.co.modularaudio.mads.internal.fade.mu.FadeOutMadDefinition;
import uk.co.modularaudio.mads.internal.fade.mu.FadeOutMadInstance;
import uk.co.modularaudio.mads.internal.paramfade.mu.PFadeConfiguration;
import uk.co.modularaudio.mads.internal.paramfade.mu.PFadeDefinitions;
import uk.co.modularaudio.mads.internal.paramfade.mu.PFadeInMadDefinition;
import uk.co.modularaudio.mads.internal.paramfade.mu.PFadeInMadInstance;
import uk.co.modularaudio.mads.internal.paramfade.mu.PFadeOutMadDefinition;
import uk.co.modularaudio.mads.internal.paramfade.mu.PFadeOutMadInstance;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.util.audio.mad.MadChannelDirection;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadChannelType;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadLink;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public class FadeInOutLinkHelper
{
	private static Log log = LogFactory.getLog( FadeInOutLinkHelper.class.getName() );

	private final MadComponentService componentService;
	private final FadeInMadDefinition fadeInDefinition;
	private final FadeOutMadDefinition fadeOutDefinition;

	private final PFadeInMadDefinition pfadeInDefinition;
	private final PFadeOutMadDefinition pfadeOutDefinition;

	private final Map<MadParameterDefinition, String> emptyParameterValues = new HashMap<MadParameterDefinition, String>();

	private final long MAX_WAIT_MILLIS = 500;

	public FadeInOutLinkHelper( final MadComponentService componentService ) throws DatastoreException, RecordNotFoundException
	{
		this.componentService = componentService;

		fadeInDefinition = (FadeInMadDefinition) componentService.findDefinitionById( FadeInMadDefinition.DEFINITION_ID );
		fadeOutDefinition = (FadeOutMadDefinition) componentService.findDefinitionById( FadeOutMadDefinition.DEFINITION_ID );

		pfadeInDefinition = (PFadeInMadDefinition)componentService.findDefinitionById( PFadeInMadDefinition.DEFINITION_ID );
		pfadeOutDefinition = (PFadeOutMadDefinition)componentService.findDefinitionById( PFadeOutMadDefinition.DEFINITION_ID );
	}

	public FadeInMadInstance fadeInAddLink( final MadGraphInstance<?,?> graph,
			final MadLink link )
		throws DatastoreException, RecordNotFoundException, MAConstraintViolationException, MadProcessingException
	{
		return insertFadeInInstanceForLink( graph, link );
	}

	public void removeFadeInAddLink( final FadeInMadInstance fadeInInstance,
			final MadGraphInstance<?,?> graph,
			final MadLink link )
		throws RecordNotFoundException, MAConstraintViolationException, DatastoreException
	{
		// Need to wait until the fade in has happened
		final long startTime = System.currentTimeMillis();
		long curTime = 0;
		while( curTime < startTime + MAX_WAIT_MILLIS && !fadeInInstance.completed() )
		{
			try
			{
				Thread.sleep( FadeDefinitions.FADE_MILLIS );
			}
			catch (final InterruptedException e)
			{
				log.debug("Interrupted caught during fade out / in - can probably ignore.");
			}
			curTime = System.currentTimeMillis();
		}

		graph.removeInstance( fadeInInstance );
		
		graph.addLink( link );
	}

	public FadeOutMadInstance fadeOutDeleteLink( final MadGraphInstance<?,?> graph,
			final MadLink link )
		throws MAConstraintViolationException, DatastoreException, RecordNotFoundException, MadProcessingException
	{
		return insertFadeOutInstanceForLink( graph, link );
	}

	public void removeFadeOutDeleteLink( final FadeOutMadInstance fadeOutInstance,
			final MadGraphInstance<?,?> graph,
			final MadLink link )
		throws RecordNotFoundException, DatastoreException
	{
		// Need to wait until the fade in has happened
		final long startTime = System.currentTimeMillis();
		long curTime = 0;
		while( curTime < startTime + MAX_WAIT_MILLIS && !fadeOutInstance.completed() )
		{
			try
			{
				Thread.sleep( FadeDefinitions.FADE_MILLIS );
			}
			catch (final InterruptedException e)
			{
				log.debug("Interrupted caught during fade out / in - can probably ignore.");
			}
			curTime = System.currentTimeMillis();
		}

		graph.removeInstance( fadeOutInstance );
	}

	public FadeInMadInstance fadeInExposeAsGraphChannel( final MadGraphInstance<?,?> graph,
			final MadChannelInstance graphChannelInstance,
			final MadChannelInstance channelInstanceToExpose )
		throws DatastoreException, RecordNotFoundException, MAConstraintViolationException, MadProcessingException
	{
		return insertFadeInInstanceForExposedGraphChannel( graph,
				graphChannelInstance,
				channelInstanceToExpose );
	}

	public void removeFadeInExposeAsGraphChannel( final FadeInMadInstance fadeInInstance,
			final MadGraphInstance<?,?> graph,
			final MadChannelInstance graphChannelInstance,
			final MadChannelInstance channelInstanceToExpose )
		throws RecordNotFoundException, MAConstraintViolationException, DatastoreException
	{
		// Need to wait until the fade in has happened
		final long startTime = System.currentTimeMillis();
		long curTime = 0;
		while( curTime < startTime + MAX_WAIT_MILLIS && !fadeInInstance.completed() )
		{
			try
			{
				Thread.sleep( FadeDefinitions.FADE_MILLIS );
			}
			catch (final InterruptedException e)
			{
				log.debug("Interrupted caught during fade out / in - can probably ignore.");
			}
			curTime = System.currentTimeMillis();
		}

		// Will clean up the links
		graph.removeInstance( fadeInInstance );

		graph.exposeAudioInstanceChannelAsGraphChannel( graphChannelInstance, channelInstanceToExpose );
	}

	public FadeOutMadInstance fadeOutRemoveExposeAsGraphChannel(
			final MadGraphInstance<?,?> graph,
			final MadChannelInstance graphChannelInstance,
			final MadChannelInstance channelInstanceWasExposed )
		 throws DatastoreException, RecordNotFoundException, MadProcessingException, MAConstraintViolationException
	{
		return insertFadeOutInstanceForExposedGraphChannel( graph,
				graphChannelInstance,
				channelInstanceWasExposed );
	}

	public void removeFadeOutRemoveExposeAsGraphChannel( final FadeOutMadInstance fadeOutInstance,
			final MadGraphInstance<?,?> graph,
			final MadChannelInstance graphChannelInstance,
			final MadChannelInstance channelInstanceExposed )
		 throws DatastoreException, RecordNotFoundException, MadProcessingException
	{
		// Need to wait until the fade out has happened
		final long startTime = System.currentTimeMillis();
		long curTime = 0;
		while( curTime < startTime + MAX_WAIT_MILLIS && !fadeOutInstance.completed() )
		{
			try
			{
				Thread.sleep( FadeDefinitions.FADE_MILLIS );
			}
			catch (final InterruptedException e)
			{
				log.debug("Interrupted caught during fade out / in - can probably ignore.");
			}
			curTime = System.currentTimeMillis();
		}
		graph.removeInstance( fadeOutInstance );
	}

	public PFadeOutMadInstance fadeOutDeleteMadInstance( final MadGraphInstance<?,?> graph,
			final MadInstance<?,?> instanceToRemove )
		throws MadProcessingException, DatastoreException, RecordNotFoundException, MAConstraintViolationException
	{
		final Map<MadChannelInstance, MadChannelInstance> auChannelInstanceToGraphChannelInstanceMap =
				graph.getAuChannelInstanceToGraphChannelInstanceMap();

		// Any channel instances currently with producer audio links need to be replaced with a fade out
		final MadChannelInstance[] channelsToCheck = instanceToRemove.getChannelInstances();

		final ArrayList<MadLink> regularLinksToFadeOut = new ArrayList<MadLink>();
		final ArrayList<MadChannelInstance> graphChannelPairsToFadeOut = new ArrayList<MadChannelInstance>();

		for( final MadChannelInstance auci : channelsToCheck )
		{
			// Skip non-audio channels
			if( auci.definition.type == MadChannelType.AUDIO && auci.definition.direction == MadChannelDirection.PRODUCER )
			{
				// See if it's exposed as a graph channel
				final MadChannelInstance graphChannelInstance = auChannelInstanceToGraphChannelInstanceMap.get( auci );
				if( graphChannelInstance != null )
				{
//					log.debug("Channel instance " + auci.toString() + " is mapped as graph channel - will insert fade out for it");
					graphChannelPairsToFadeOut.add( graphChannelInstance );
					graphChannelPairsToFadeOut.add( auci );
				}
				else
				{
//					log.debug("Checking if channel instance " + auci.toString() + " is linked as a producer");
					// Find if it's exposed as a regular link
					final ArrayList<MadLink> linksForProducerChannel = graph.findLinksForProducerChannelInstanceReturnNull(auci);
					if( linksForProducerChannel != null )
					{
						regularLinksToFadeOut.addAll( linksForProducerChannel );
					}
				}
			}
		}

		return insertPFadeOutInstanceForChannels( graph,
				graphChannelPairsToFadeOut,
				regularLinksToFadeOut );
	}

	public void removePFadeOutDeleteMadInstance( final PFadeOutMadInstance waitInstance,
			final MadGraphInstance<?, ?> graph,
			final MadInstance<?, ?> instanceToRemove )
		throws RecordNotFoundException
	{
		// Need to wait until the fade out has happened
		final long startTime = System.currentTimeMillis();
		long curTime = startTime;
		while( curTime < startTime + MAX_WAIT_MILLIS && !waitInstance.completed() )
		{
			try
			{
				Thread.sleep( FadeDefinitions.FADE_MILLIS );
			}
			catch (final InterruptedException e)
			{
				log.debug("Interrupted caught during fade out / in - can probably ignore.");
			}
			curTime = System.currentTimeMillis();
		}

		graph.removeInstance( waitInstance );
		graph.removeInstance(instanceToRemove);
	}

	public PFadeInMadInstance fadeInGraphChannelMap( final MadGraphInstance<?,?> graph,
			final MadInstance<?,?> instanceToMap,
			final boolean warnAboutMissingChannels )
		throws MadProcessingException, DatastoreException, RecordNotFoundException, MAConstraintViolationException
	{
		// For each channel we find in the graph, look for a corresponding named channel in
		// the instance, and create a fade in instance between them
		final MadChannelInstance[] graphChannels = graph.getChannelInstances();
		final ArrayList<MadChannelInstance> channelPairsToBulkFade = new ArrayList<MadChannelInstance>();
		for( final MadChannelInstance graphChannel : graphChannels )
		{
			final String name = graphChannel.definition.name;
			final MadChannelInstance madInstanceChannel = instanceToMap.getChannelInstanceByNameReturnNull( name );

			if( madInstanceChannel != null )
			{
				final MadChannelType channelType = graphChannel.definition.type;
				final MadChannelDirection channelDirection = graphChannel.definition.direction;
				// Only do fade in on producer channels
				if( channelType == MadChannelType.AUDIO && channelDirection == MadChannelDirection.PRODUCER )
				{
					channelPairsToBulkFade.add( graphChannel );
					channelPairsToBulkFade.add( madInstanceChannel );
				}
				else
				{
					// We need to map the other channels too, as they may have an influence on the sound being produced (control etc)
					graph.exposeAudioInstanceChannelAsGraphChannel( graphChannel, madInstanceChannel );
				}
			}
			else if( warnAboutMissingChannels )
			{
				if( log.isWarnEnabled() )
				{
					log.warn( "Unable to map channel name " + name + " to appropriate instance channel" );
				}
			}
		}

		return insertPFadeInInstanceForGraphChannels( graph, channelPairsToBulkFade );
	}

	public void removeFadeInGraphChannelMap( final PFadeInMadInstance waitInstance,
			final MadGraphInstance<?,?> graph,
			final MadInstance<?,?> instanceToMap )
		throws RecordNotFoundException, MAConstraintViolationException, DatastoreException
	{
		// Need to wait until the fade in has happened
		final long startTime = System.currentTimeMillis();
		long curTime = 0;
		while( curTime < startTime + MAX_WAIT_MILLIS && !waitInstance.completed() )
		{
			try
			{
				Thread.sleep( FadeDefinitions.FADE_MILLIS );
			}
			catch (final InterruptedException e)
			{
				log.debug("Interrupted caught during fade out / in - can probably ignore.");
			}
			curTime = System.currentTimeMillis();
		}
		// Basically remove all the fade in instances
		// and then re-expose the instance channels that map to graph ones
		graph.removeInstance(waitInstance);

		// Map channels
		final MadChannelInstance[] graphChannels = graph.getChannelInstances();
		for( final MadChannelInstance graphChannel : graphChannels )
		{
			// Only need to remap the audio ones that are output
			final String name = graphChannel.definition.name;
			final MadChannelType channelType = graphChannel.definition.type;
			final MadChannelDirection channelDirection = graphChannel.definition.direction;
			if( channelType == MadChannelType.AUDIO && channelDirection == MadChannelDirection.PRODUCER )
			{
				final MadChannelInstance instanceChannel = instanceToMap.getChannelInstanceByNameReturnNull( name );
				if( instanceChannel != null )
				{
					graph.exposeAudioInstanceChannelAsGraphChannel( graphChannel, instanceChannel );
				}
				else
				{
					log.warn("Unable to re-map channel");
				}
			}
		}
	}

	public void removePFadeInGraphChannelMap( final PFadeInMadInstance waitInstance,
			final MadGraphInstance<?, ?> graph,
			final MadInstance<?, ?> instanceToMap )
		throws RecordNotFoundException, MAConstraintViolationException
	{
		// Need to wait until the fade in has happened
		final long startTime = System.currentTimeMillis();
		long curTime = 0;
		while( curTime < startTime + MAX_WAIT_MILLIS && !waitInstance.completed() )
		{
			try
			{
				Thread.sleep( FadeDefinitions.FADE_MILLIS );
			}
			catch (final InterruptedException e)
			{
				log.debug("Interrupted caught during fade out / in - can probably ignore.");
			}
			curTime = System.currentTimeMillis();
		}
		// Basically remove all the fade in instances
		// and then re-expose the instance channels that map to graph ones
		graph.removeInstance(waitInstance);

		// Map channels
		final MadChannelInstance[] graphChannels = graph.getChannelInstances();
		for( final MadChannelInstance graphChannel : graphChannels )
		{
			// Only need to remap the audio ones that are output
			final String name = graphChannel.definition.name;
			final MadChannelType channelType = graphChannel.definition.type;
			final MadChannelDirection channelDirection = graphChannel.definition.direction;
			if( channelType == MadChannelType.AUDIO && channelDirection == MadChannelDirection.PRODUCER )
			{
				final MadChannelInstance instanceChannel = instanceToMap.getChannelInstanceByNameReturnNull( name );

				graph.exposeAudioInstanceChannelAsGraphChannel( graphChannel, instanceChannel );
			}
		}
	}

	public void finaliseFadeIn( final FadeInMadInstance fadeInInstance ) throws RecordNotFoundException, DatastoreException
	{
		componentService.destroyInstance( fadeInInstance );
	}

	public void finalisePFadeIn( final PFadeInMadInstance pFadeInInstance ) throws DatastoreException, RecordNotFoundException
	{
		componentService.destroyInstance( pFadeInInstance );
	}

	public void finaliseFadeOut( final FadeOutMadInstance fadeOutInstance ) throws DatastoreException, RecordNotFoundException
	{
		componentService.destroyInstance( fadeOutInstance );
	}

	public void finalisePFadeOut( final PFadeOutMadInstance fadeOutInstance ) throws DatastoreException, RecordNotFoundException
	{
		componentService.destroyInstance( fadeOutInstance );
	}

	private FadeOutMadInstance insertFadeOutInstanceForLink(
			final MadGraphInstance<?,?> graph,
			final MadLink link )
		throws DatastoreException, RecordNotFoundException, MadProcessingException, MAConstraintViolationException
	{
		final String uniqueInstanceName = link.toString();
		// Remove the actual link
		graph.deleteLink( link );

		// Add a fade out component to the graph
		final FadeOutMadInstance fadeOutInstance = (FadeOutMadInstance)componentService.createInstanceFromDefinition( fadeOutDefinition,
				emptyParameterValues,
				"Internal Link Fade Out Component for link " + uniqueInstanceName );

		final MadChannelInstance fadeOutProducerChannel = fadeOutInstance.getChannelInstances()[ FadeOutMadDefinition.PRODUCER ];
		final MadChannelInstance fadeOutConsumerChannel = fadeOutInstance.getChannelInstances()[ FadeOutMadDefinition.CONSUMER ];

		graph.addInstanceWithName( fadeOutInstance, "Temporary Link Fade Out" + uniqueInstanceName );

		final MadChannelInstance consumerChannel = link.getConsumerChannelInstance();

		final MadChannelInstance producerChannel = link.getProducerChannelInstance();

		// Link the consumer to the fade out producer channel
		final MadLink consumerLink = new MadLink( fadeOutProducerChannel, consumerChannel );
		graph.addLink( consumerLink );

		final MadLink producerLink = new MadLink( producerChannel, fadeOutConsumerChannel );
		graph.addLink( producerLink );
		return fadeOutInstance;
	}

	private FadeOutMadInstance insertFadeOutInstanceForExposedGraphChannel(
			final MadGraphInstance<?,?> graph,
			final MadChannelInstance graphChannelInstance,
			final MadChannelInstance channelInstanceWasExposed )
		throws RecordNotFoundException, DatastoreException, MadProcessingException, MAConstraintViolationException
	{
		final String uniqueInstanceName = graphChannelInstance.toString();

		graph.removeAudioInstanceChannelAsGraphChannel( graphChannelInstance, channelInstanceWasExposed );

		// Now insert a fade out component and map it in
		final FadeOutMadInstance fadeOutInstance = (FadeOutMadInstance)componentService.createInstanceFromDefinition( fadeOutDefinition,
				emptyParameterValues,
				"Internal Link Fade Out Component for " + uniqueInstanceName );

		final MadChannelInstance fadeOutProducerChannel = fadeOutInstance.getChannelInstances()[ FadeOutMadDefinition.PRODUCER ];
		final MadChannelInstance fadeOutConsumerChannel = fadeOutInstance.getChannelInstances()[ FadeOutMadDefinition.CONSUMER ];

		graph.addInstanceWithName( fadeOutInstance, "Temporary Link Fade Out For " + uniqueInstanceName );

		final MadChannelDirection channelToExposeDirection = channelInstanceWasExposed.definition.direction;

		if( channelToExposeDirection == MadChannelDirection.CONSUMER )
		{
			final MadChannelInstance consumerChannel = channelInstanceWasExposed;

			// Link the consumer to the fade in producer channel
			final MadLink consumerLink = new MadLink( fadeOutProducerChannel, consumerChannel );
			graph.addLink( consumerLink );

			// And expose the fade in consumer channel as the graph channel
			graph.exposeAudioInstanceChannelAsGraphChannel( graphChannelInstance, fadeOutConsumerChannel );
		}
		else
		{
			final MadChannelInstance producerChannel = channelInstanceWasExposed;

			final MadLink producerLink = new MadLink( producerChannel, fadeOutConsumerChannel );
			graph.addLink( producerLink );

			graph.exposeAudioInstanceChannelAsGraphChannel( graphChannelInstance, fadeOutProducerChannel );

		}
		return fadeOutInstance;
	}

	private FadeInMadInstance insertFadeInInstanceForLink( final MadGraphInstance<?,?> graph,
			final MadLink link )
		throws DatastoreException, RecordNotFoundException, MadProcessingException, MAConstraintViolationException
	{
		final String uniqueInstanceName = link.toString();
		// Add a fade in component to the graph
		final FadeInMadInstance fadeInInstance = (FadeInMadInstance)componentService.createInstanceFromDefinition( fadeInDefinition,
				emptyParameterValues,
				"Internal Link Fade In Component for " + uniqueInstanceName );

		final MadChannelInstance fadeInProducerChannel = fadeInInstance.getChannelInstances()[ FadeInMadDefinition.PRODUCER ];
		final MadChannelInstance fadeInConsumerChannel = fadeInInstance.getChannelInstances()[ FadeInMadDefinition.CONSUMER ];

		graph.addInstanceWithName( fadeInInstance, "Temporary Link Fade In For " + uniqueInstanceName );

		final MadChannelInstance consumerChannel = link.getConsumerChannelInstance();

		final MadChannelInstance producerChannel = link.getProducerChannelInstance();

		// Link the consumer to the fade in producer channel
		final MadLink consumerLink = new MadLink( fadeInProducerChannel, consumerChannel );
		graph.addLink( consumerLink );

		final MadLink producerLink = new MadLink( producerChannel, fadeInConsumerChannel );
		graph.addLink( producerLink );
		return fadeInInstance;
	}

	private FadeInMadInstance insertFadeInInstanceForExposedGraphChannel( final MadGraphInstance<?,?> graph,
			final MadChannelInstance graphChannelInstance,
			final MadChannelInstance channelInstanceToExpose )
		throws DatastoreException, RecordNotFoundException, MadProcessingException, MAConstraintViolationException
	{
		final String uniqueInstanceName = graphChannelInstance.toString();
		// Add a fade out component to the graph
		final FadeInMadInstance fadeInInstance = (FadeInMadInstance)componentService.createInstanceFromDefinition( fadeInDefinition,
				emptyParameterValues,
				"Internal Link Fade In Component for graph channel " + uniqueInstanceName );

		final MadChannelInstance fadeInProducerChannel = fadeInInstance.getChannelInstances()[ FadeInMadDefinition.PRODUCER ];
		final MadChannelInstance fadeInConsumerChannel = fadeInInstance.getChannelInstances()[ FadeInMadDefinition.CONSUMER ];

		graph.addInstanceWithName( fadeInInstance, "Temporary Link Fade In for " + uniqueInstanceName );

		final MadChannelDirection channelToExposeDirection = channelInstanceToExpose.definition.direction;

		if( channelToExposeDirection == MadChannelDirection.CONSUMER )
		{
			final MadChannelInstance consumerChannel = channelInstanceToExpose;

			// Link the consumer to the fade in producer channel
			final MadLink consumerLink = new MadLink( fadeInProducerChannel, consumerChannel );
			graph.addLink( consumerLink );

			// And expose the fade in consumer channel as the graph channel
			graph.exposeAudioInstanceChannelAsGraphChannel( graphChannelInstance, fadeInConsumerChannel );
		}
		else
		{
			final MadChannelInstance producerChannel = channelInstanceToExpose;

			final MadLink producerLink = new MadLink( producerChannel, fadeInConsumerChannel );
			graph.addLink( producerLink );

			graph.exposeAudioInstanceChannelAsGraphChannel( graphChannelInstance, fadeInProducerChannel );

		}
		return fadeInInstance;
	}

	private PFadeInMadInstance insertPFadeInInstanceForGraphChannels(
			final MadGraphInstance<?, ?> graph,
			final ArrayList<MadChannelInstance> channelPairsToBulkFade )
		throws DatastoreException, RecordNotFoundException, MadProcessingException, MAConstraintViolationException
	{
		final int totalChannels = channelPairsToBulkFade.size();
		final int numFadedChannels = totalChannels / 2;
		final Map<MadParameterDefinition, String> params = new HashMap<MadParameterDefinition, String>();
		params.put( PFadeDefinitions.NUM_CHANNELS_PARAMETER, numFadedChannels + "");
		final PFadeInMadInstance retVal = (PFadeInMadInstance)componentService.createInstanceFromDefinition(
				pfadeInDefinition, params, "Temporary component fade in");
		graph.addInstanceWithName(retVal, retVal.getInstanceName() );
		final PFadeConfiguration pfc = retVal.getInstanceConfiguration();
		final MadChannelInstance[] pfadeChannelInstances = retVal.getChannelInstances();

		int cpIndex = 0;
		for( int c = 0 ; c < numFadedChannels ; ++c )
		{
			final MadChannelInstance graphChannelInstance = channelPairsToBulkFade.get( cpIndex++ );
			final MadChannelInstance channelInstanceToExpose = channelPairsToBulkFade.get( cpIndex++ );
			final int producerIndex = pfc.getProducerChannelIndex( c );
			final int consumerIndex = pfc.getConsumerChannelIndex( c );

			final MadChannelInstance fadeInProducerChannel = pfadeChannelInstances[ producerIndex ];
			final MadChannelInstance fadeInConsumerChannel = pfadeChannelInstances[ consumerIndex ];

			final MadChannelInstance producerChannel = channelInstanceToExpose;

			final MadLink producerLink = new MadLink( producerChannel, fadeInConsumerChannel );
			graph.addLink( producerLink );

			graph.exposeAudioInstanceChannelAsGraphChannel( graphChannelInstance, fadeInProducerChannel );
		}

		return retVal;
	}

	private PFadeOutMadInstance insertPFadeOutInstanceForChannels( final MadGraphInstance<?, ?> graph,
			final ArrayList<MadChannelInstance> graphChannelPairsToFadeOut,
			final ArrayList<MadLink> regularLinksToFadeOut )
		throws DatastoreException, RecordNotFoundException, MadProcessingException, MAConstraintViolationException
	{
		final int totalGraphChannels = graphChannelPairsToFadeOut.size();
		final int numFadedGraphChannels = totalGraphChannels / 2;

		final int totalRegularChannels = regularLinksToFadeOut.size();
		final int numFadedRegularChannels = totalRegularChannels;

		final int numTotalFadedChannels = numFadedGraphChannels + numFadedRegularChannels;

		final Map<MadParameterDefinition, String> params = new HashMap<MadParameterDefinition, String>();
		params.put( PFadeDefinitions.NUM_CHANNELS_PARAMETER, numTotalFadedChannels + "");
		final PFadeOutMadInstance retVal = (PFadeOutMadInstance)componentService.createInstanceFromDefinition(
				pfadeOutDefinition, params, "Temporary component fade out");
		graph.addInstanceWithName(retVal, retVal.getInstanceName());
		final PFadeConfiguration pfc = retVal.getInstanceConfiguration();

		int fadeIndex = 0;
		int cpIndex = 0;
		final MadChannelInstance[] pfadeChannelInstances = retVal.getChannelInstances();
		for( int c = 0 ; c < numFadedGraphChannels ; ++c )
		{
			final MadChannelInstance graphChannelInstance = graphChannelPairsToFadeOut.get( cpIndex++ );
			final MadChannelInstance channelInstanceToRemove = graphChannelPairsToFadeOut.get( cpIndex++ );
			final int producerIndex = pfc.getProducerChannelIndex( fadeIndex );
			final int consumerIndex = pfc.getConsumerChannelIndex( fadeIndex );

			final MadChannelInstance fadeOutProducerChannel = pfadeChannelInstances[ producerIndex ];
			final MadChannelInstance fadeOutConsumerChannel = pfadeChannelInstances[ consumerIndex ];

			// Remove existing IO link
			graph.removeAudioInstanceChannelAsGraphChannel(graphChannelInstance, channelInstanceToRemove);

			// Wire through fade channel
			graph.exposeAudioInstanceChannelAsGraphChannel(graphChannelInstance, fadeOutProducerChannel);
			final MadLink graphToFadeLink = new MadLink( channelInstanceToRemove, fadeOutConsumerChannel );
			graph.addLink( graphToFadeLink );
			fadeIndex++;
		}
		int linkIndex = 0;
		for( int c = 0 ; c < numFadedRegularChannels ; ++c )
		{
			final MadLink linkToFadeOut = regularLinksToFadeOut.get( linkIndex++ );
			final MadChannelInstance consumerChannelInstance = linkToFadeOut.getConsumerChannelInstance();
			final MadChannelInstance producerChannelInstanceToRemove = linkToFadeOut.getProducerChannelInstance();
			final int producerIndex = pfc.getProducerChannelIndex( fadeIndex );
			final int consumerIndex = pfc.getConsumerChannelIndex( fadeIndex );

			final MadChannelInstance fadeOutProducerChannel = pfadeChannelInstances[ producerIndex ];
			final MadChannelInstance fadeOutConsumerChannel = pfadeChannelInstances[ consumerIndex ];

			// Remove link
			graph.deleteLink( linkToFadeOut );

			// Wire through fade channel
			final MadLink consumerLink = new MadLink( producerChannelInstanceToRemove, fadeOutConsumerChannel );
			graph.addLink( consumerLink );
			final MadLink producerLink = new MadLink( fadeOutProducerChannel, consumerChannelInstance );
			graph.addLink( producerLink );
			fadeIndex++;
		}

		return retVal;
	}
}
