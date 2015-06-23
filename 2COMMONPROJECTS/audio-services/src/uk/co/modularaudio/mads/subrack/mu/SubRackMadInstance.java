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

package uk.co.modularaudio.mads.subrack.mu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.subrack.SubRackCreationContext;
import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.service.jobexecutor.JobExecutorService;
import uk.co.modularaudio.service.madgraph.MadGraphService;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.service.rackmarshalling.RackMarshallingService;
import uk.co.modularaudio.service.userpreferences.UserPreferencesService;
import uk.co.modularaudio.util.audio.gui.mad.rack.DirtyableRackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDirtyListener;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class SubRackMadInstance extends MadGraphInstance<SubRackMadDefinition, SubRackMadInstance>
	implements DirtyableRackComponent
{
	private static Log log = LogFactory.getLog( SubRackMadInstance.class.getName() );

	// Messy - should really have a way for the UI components to get references into the component graph
	public final RackService rackService;
	public final MadGraphService graphService;
	public final RackMarshallingService rackMarshallingService;
	public final GuiService guiService;
	public final JobExecutorService jobExecutorService;
	public final UserPreferencesService userPreferencesService;

	private RackDataModel subRackDataModel;

	private final List<RackDirtyListener> dirtyListeners = new ArrayList<RackDirtyListener>();

	public SubRackMadInstance( final SubRackCreationContext creationContext,
			final String instanceName,
			final SubRackMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
		throws DatastoreException, MAConstraintViolationException, RecordNotFoundException, IOException
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
		this.rackService = creationContext.getRackService();
		this.graphService = creationContext.getGraphService();
		this.rackMarshallingService = creationContext.getRackMarshallingService();
		this.guiService = creationContext.getGuiService();
		this.jobExecutorService = creationContext.getJobExecutorService();
		this.userPreferencesService = creationContext.getUserPreferencesService();

		subRackDataModel = rackService.createNewSubRackDataModel( instanceName,
				"",
				RackService.DEFAULT_RACK_COLS,
				RackService.DEFAULT_RACK_ROWS,
				true );
//		log.debug("Created named " + instanceName );
		mapSubRackIntoGraph();
	}

	private void mapSubRackIntoGraph() throws DatastoreException, MAConstraintViolationException, RecordNotFoundException
	{
		// First add the sub rack graph as an instance inside ourselves
		final MadGraphInstance<?,?> subRackGraph = rackService.getRackGraphInstance( subRackDataModel );

		// Need to push this down into graph service so all links are faded
		// Now expose all the sub rack channels as our channels
		graphService.addInstanceToGraphWithNameAndMapChannelsToGraphChannels( this, subRackGraph,
				rackService.getRackName( subRackDataModel ), true );
	}

	private void unmapSubRackFromGraph() throws DatastoreException, RecordNotFoundException, MAConstraintViolationException
	{
		final MadGraphInstance<?,?> subRackGraph = rackService.getRackGraphInstance( subRackDataModel );
		// Will remove anything connected to graph channels
		graphService.removeInstanceFromGraph( this, subRackGraph );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings,
			final MadTimingParameters timingParameters,
			final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		throw new MadProcessingException( "SubRacks should never be scheduled!" );
	}

	@Override
	public void stop() throws MadProcessingException
	{
		throw new MadProcessingException( "SubRacks should never be scheduled!" );
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage ,
			final MadTimingParameters timingParameters ,
			final long periodStartFrameTime ,
			final MadChannelConnectedFlags channelConnectedFlags ,
			final MadChannelBuffer[] channelBuffers , final int frameOffset , final int numFrames  )
	{
		log.error( "Subracks should never be scheduled!" );
		return RealtimeMethodReturnCodeEnum.FAIL_FATAL;
	}

	public String getCurrentPatchName()
	{
		return rackService.getRackName( subRackDataModel );
	}

	public void setCurrentPatchName( final String currentPatchName )
	{
		rackService.setRackName( subRackDataModel, currentPatchName );
		if( log.isDebugEnabled() )
		{
			log.debug("SubRackMI setCurrentPatchName(" + currentPatchName + ")");
		}
	}

	public RackDataModel getSubRackDataModel()
	{
		return subRackDataModel;
	}

	public void setSubRackDataModel( final RackDataModel newSubRackDataModel, final boolean destroyPrevious )
		throws DatastoreException, RecordNotFoundException, MAConstraintViolationException
	{
//		log.debug("SetSubRackDataModel called on " + subRackDataModel.getName() );
		final RackDataModel previousModel = subRackDataModel;
		// Remove listeners
		for( final RackDirtyListener rdl : dirtyListeners )
		{
			subRackDataModel.removeRackDirtyListener( rdl );
		}
		unmapSubRackFromGraph();

		this.subRackDataModel = newSubRackDataModel;
		mapSubRackIntoGraph();
		for( final RackDirtyListener rdl : dirtyListeners )
		{
			subRackDataModel.addRackDirtyListener( rdl );
		}
		// Mark us as dirty since the sub rack has changed
		for( final RackDirtyListener rdl : dirtyListeners )
		{
			rdl.receiveRackDirty();
		}
		if( destroyPrevious )
		{
			rackService.destroyRackDataModel( previousModel );
		}
	}


	public boolean isDirty()
	{
		return rackService.isRackDirty( subRackDataModel );
	}

	@Override
	public void addRackDirtyListener( final RackDirtyListener rackDirtyListener )
	{
		dirtyListeners.add( rackDirtyListener );
		subRackDataModel.addRackDirtyListener( rackDirtyListener );
	}

	@Override
	public void removeRackDirtyListener( final RackDirtyListener rackDirtyListener )
	{
		dirtyListeners.remove( rackDirtyListener );
		subRackDataModel.removeRackDirtyListener( rackDirtyListener );
	}

	@Override
	public void destroy()
	{
//		log.debug("About to be destroyed " + instanceName );
		destroySubRackDataModel();
		try
		{
			this.dirtyListeners.clear();
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught cleaning up sub rack instance: " + e.toString();
			log.error( msg, e );
		}
		super.destroy();
	}

	public void destroySubRackDataModel()
	{
		try
		{
			if( subRackDataModel != null )
			{
				for( final RackDirtyListener rdl : dirtyListeners )
				{
					subRackDataModel.removeRackDirtyListener( rdl );
				}
				unmapSubRackFromGraph();
				rackService.destroyRackDataModel( subRackDataModel );
				subRackDataModel = null;
			}
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught cleaning up sub rack instance: " + e.toString();
			log.error( msg, e );
		}
	}
}
