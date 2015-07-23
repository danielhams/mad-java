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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import uk.co.modularaudio.util.tuple.TwoTuple;

public class FadeInOutLinkHelper
{
	private static Log log = LogFactory.getLog( FadeInOutLinkHelper.class.getName() );

	private final MadComponentService componentService;
	private final FadeInMadDefinition fadeInDefinition;
	private final FadeOutMadDefinition fadeOutDefinition;

	private final PFadeInMadDefinition pfadeInDefinition;
	private final PFadeOutMadDefinition pfadeOutDefinition;

	private final static Map<MadParameterDefinition, String> EMPTY_PARAM_VALUES = new HashMap<MadParameterDefinition, String>();

	// TODO: Find a way to fix this.
	private final long MAX_WAIT_MILLIS = 200;

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
		// Any channel instances currently with producer audio links need to be replaced with a fade out
		final MadChannelInstance[] channelsToCheck = instanceToRemove.getChannelInstances();

		final Set<MadLink> regularLinksToFadeOut = new HashSet<MadLink>();
		final ArrayList<MadChannelInstance> graphChannelPairsToFadeOut = new ArrayList<MadChannelInstance>();

		for( final MadChannelInstance auci : channelsToCheck )
		{
			// Skip non-audio channels
			if( auci.definition.type == MadChannelType.AUDIO && auci.definition.direction == MadChannelDirection.PRODUCER )
			{
				// See if it's exposed as a graph channel
				final boolean channelIsGraphMapped = graph.isProducerChannelExposedOnGraph( auci );
				if( channelIsGraphMapped )
				{
					final ArrayList<MadChannelInstance> mappedGraphChannels = graph.getGraphChannelsExposedForProducerChannel( auci );
//					log.debug("Channel instance " + auci.toString() + " is mapped as one or more graph channels - will insert fade out for it");
					for( final MadChannelInstance mgci : mappedGraphChannels )
					{
						graphChannelPairsToFadeOut.add( mgci );
						graphChannelPairsToFadeOut.add( auci );
					}
				}
				else
				{
//					log.debug("Checking if channel instance " + auci.toString() + " is linked as a producer");
					// Find if it's exposed as a regular link
					final Set<MadLink> linksWithAuciAsProducer = graph.findProducerInstanceLinksReturnNull(auci);
					if( linksWithAuciAsProducer != null && linksWithAuciAsProducer.size() > 0 )
					{
//						log.debug("Have " + linksWithAuciAsProducer.size() + " links to fade out");
						regularLinksToFadeOut.addAll( linksWithAuciAsProducer );
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

	public TwoTuple<PFadeInMadInstance, PFadeInMadInstance> fadeInGraphChannelMap( final MadGraphInstance<?,?> graph,
			final MadInstance<?,?> instanceToMap,
			final boolean warnAboutMissingChannels )
		throws MadProcessingException, DatastoreException, RecordNotFoundException, MAConstraintViolationException
	{
		// For each channel in the instance look for a correspondingly named graph channel to
		// map it to. If the channel is an audio channel, add it to the list
		// of channels we'll fade in on.
		final MadChannelInstance[] instanceToMapChannels = instanceToMap.getChannelInstances();
		final ArrayList<MadChannelInstance> producerPairsToBulkFade = new ArrayList<MadChannelInstance>();
		final ArrayList<MadChannelInstance> consumerPairsToBulkFade = new ArrayList<MadChannelInstance>();
		for( final MadChannelInstance instanceChannel : instanceToMapChannels )
		{
			final String name = instanceChannel.definition.name;

			final MadChannelInstance matchingGraphChannel = graph.getChannelInstanceByNameReturnNull( name );

			if( matchingGraphChannel != null )
			{
				final MadChannelType channelType = instanceChannel.definition.type;
				final MadChannelDirection channelDirection = instanceChannel.definition.direction;

				if( channelType == MadChannelType.AUDIO )
				{
					switch( channelDirection )
					{
						case PRODUCER:
						{
							producerPairsToBulkFade.add( matchingGraphChannel );
							producerPairsToBulkFade.add( instanceChannel );
							break;
						}
						case CONSUMER:
						default:
						{
							consumerPairsToBulkFade.add( matchingGraphChannel );
							consumerPairsToBulkFade.add( instanceChannel );
							break;
						}
					}
				}
				else
				{
					graph.exposeAudioInstanceChannelAsGraphChannel( matchingGraphChannel, instanceChannel );
				}
			}
			else
			{
//				if( log.isWarnEnabled() )
//				{
//					log.warn( "Unable to map channel name " + name + " to appropriate graph channel" );
//				}
			}
		}

		PFadeInMadInstance producersFadeIn = null;
		PFadeInMadInstance consumersFadeIn = null;

		if( producerPairsToBulkFade.size() > 0 )
		{
			producersFadeIn = insertPFadeInInstanceForGraphChannels( graph,
					producerPairsToBulkFade,
					"Producers Fade In");
		}
		if( consumerPairsToBulkFade.size() > 0 )
		{
			consumersFadeIn = insertPFadeInInstanceForGraphChannels( graph,
				consumerPairsToBulkFade,
				"Consumers Fade In");
		}

		return new TwoTuple<PFadeInMadInstance, PFadeInMadInstance>( producersFadeIn, consumersFadeIn );
	}

	public void removePFadeInGraphChannelMap( final TwoTuple<PFadeInMadInstance,PFadeInMadInstance> fiInstance,
			final MadGraphInstance<?, ?> graph,
			final MadInstance<?, ?> instanceToMap )
		throws RecordNotFoundException, MAConstraintViolationException
	{
		final PFadeInMadInstance prodInstance = fiInstance.getHead();
		final PFadeInMadInstance consInstance = fiInstance.getTail();
		final PFadeInMadInstance waitInstance = (prodInstance == null ? consInstance : prodInstance );
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

		// Basically remove the fade in instance
		// and then re-expose the instance channels that map to graph ones
		if( prodInstance != null )
		{
			graph.removeInstance( prodInstance );
		}
		if( consInstance != null )
		{
			graph.removeInstance( consInstance );
		}

		// Map channels
		final MadChannelInstance[] graphChannels = graph.getChannelInstances();
		for( final MadChannelInstance graphChannel : graphChannels )
		{
			// Only need to remap audio ones - others were already mapped
			if( graphChannel.definition.type == MadChannelType.AUDIO )
			{
				final MadChannelInstance instanceChannel = instanceToMap.getChannelInstanceByNameReturnNull(
						graphChannel.definition.name );

				graph.exposeAudioInstanceChannelAsGraphChannel( graphChannel, instanceChannel );
			}
		}
	}

	public void finaliseFadeIn( final FadeInMadInstance fadeInInstance ) throws RecordNotFoundException, DatastoreException
	{
		componentService.destroyInstance( fadeInInstance );
	}

	public void finalisePFadeIn( final TwoTuple<PFadeInMadInstance, PFadeInMadInstance> fiInstances ) throws DatastoreException, RecordNotFoundException
	{
		final PFadeInMadInstance prodIns = fiInstances.getHead();
		final PFadeInMadInstance consIns = fiInstances.getTail();
		if( prodIns != null )
		{
			componentService.destroyInstance( prodIns );
		}
		if( consIns != null )
		{
			componentService.destroyInstance( consIns );
		}
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
				EMPTY_PARAM_VALUES,
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
				EMPTY_PARAM_VALUES,
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
				EMPTY_PARAM_VALUES,
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
				EMPTY_PARAM_VALUES,
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
			final ArrayList<MadChannelInstance> channelPairsToBulkFade,
			final String nameInGraph )
		throws DatastoreException, RecordNotFoundException, MadProcessingException, MAConstraintViolationException
	{
		final int totalChannels = channelPairsToBulkFade.size();
		final int numFadedChannels = totalChannels / 2;
		final Map<MadParameterDefinition, String> params = new HashMap<MadParameterDefinition, String>();
		params.put( PFadeDefinitions.NUM_CHANNELS_PARAMETER, numFadedChannels + "");
		final PFadeInMadInstance retVal = (PFadeInMadInstance)componentService.createInstanceFromDefinition(
				pfadeInDefinition, params, nameInGraph );
		graph.addInstanceWithName(retVal, nameInGraph );
		final PFadeConfiguration pfc = retVal.getInstanceConfiguration();
		final MadChannelInstance[] pfadeChannelInstances = retVal.getChannelInstances();

		int cpIndex = 0;
		for( int c = 0 ; c < numFadedChannels ; ++c )
		{
			final MadChannelInstance graphChannelInstance = channelPairsToBulkFade.get( cpIndex++ );
			final MadChannelDirection graphChannelDirection = graphChannelInstance.definition.direction;

			switch( graphChannelDirection )
			{
				case PRODUCER:
				{
					final MadChannelInstance channelInstanceToExpose = channelPairsToBulkFade.get( cpIndex++ );
					final int producerIndex = pfc.getProducerChannelIndex( c );
					final int consumerIndex = pfc.getConsumerChannelIndex( c );

					final MadChannelInstance fadeInProducerChannel = pfadeChannelInstances[ producerIndex ];
					final MadChannelInstance fadeInConsumerChannel = pfadeChannelInstances[ consumerIndex ];

					final MadChannelInstance producerChannel = channelInstanceToExpose;

					final MadLink producerLink = new MadLink( producerChannel, fadeInConsumerChannel );
					graph.addLink( producerLink );

					graph.exposeAudioInstanceChannelAsGraphChannel( graphChannelInstance, fadeInProducerChannel );
					break;
				}
				case CONSUMER:
				default:
				{
					final MadChannelInstance channelInstanceToExpose = channelPairsToBulkFade.get( cpIndex++ );
					final int producerIndex = pfc.getProducerChannelIndex( c );
					final int consumerIndex = pfc.getConsumerChannelIndex( c );

					final MadChannelInstance fadeInProducerChannel = pfadeChannelInstances[ producerIndex ];
					final MadChannelInstance fadeInConsumerChannel = pfadeChannelInstances[ consumerIndex ];

					final MadChannelInstance consumerChannel = channelInstanceToExpose;

					final MadLink consumerLink = new MadLink( fadeInProducerChannel, consumerChannel );
					graph.addLink( consumerLink );

					graph.exposeAudioInstanceChannelAsGraphChannel( graphChannelInstance, fadeInConsumerChannel );
					break;
				}
			}
		}

		return retVal;
	}

	private PFadeOutMadInstance insertPFadeOutInstanceForChannels( final MadGraphInstance<?, ?> graph,
			final ArrayList<MadChannelInstance> graphChannelPairsToFadeOut,
			final Set<MadLink> regularLinksToFadeOut )
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
		for( final MadLink linkToFadeOut : regularLinksToFadeOut )
		{
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
