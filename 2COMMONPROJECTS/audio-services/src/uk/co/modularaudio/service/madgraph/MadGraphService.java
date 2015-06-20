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

package uk.co.modularaudio.service.madgraph;

import java.util.Collection;
import java.util.Set;

import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadLink;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphListener;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;

public interface MadGraphService
{
	// CRUD on the graph
	// Can only create and delete graphs.
	public MadGraphInstance<?,?> createNewRootGraph( String name )
		throws DatastoreException;

	public MadGraphInstance<?,?> createNewParameterisedGraph( String name,
			GraphType graphType,
			int numInputAudioChannels,
			int numOutputAudioChannels,
			int numInputCvChannels,
			int numOutputCvChannels,
			int numInputNoteChannels,
			int numOutputNoteChannels )
		throws DatastoreException;

	public void destroyGraph( MadGraphInstance<?,?> graph, boolean deleteLinks, boolean deleteMadInstances ) throws DatastoreException;

	// CRUD on listeners on the graph
	public void addGraphListener( MadGraphInstance<?,?> graph, MadGraphListener listener );
	public void removeGraphListener( MadGraphInstance<?,?> graph, MadGraphListener listener );
	public boolean graphHasListeners( MadGraphInstance<?, ?> graph );

	// CRUD on audio components in the graph - we are manipulating "instances"
	// as components have extra properties when they are in a graph ( internal state of processing e.g. gain, pan etc)
	public boolean checkCanAddInstanceToGraphWithName( MadGraphInstance<?,?> graph, String name );
	public void addInstanceToGraphWithName( MadGraphInstance<?,?> graph, MadInstance<?,?> instance, String name )
			throws DatastoreException, MAConstraintViolationException;
	public void addInstanceToGraphWithNameAndMapChannelsToGraphChannels( MadGraphInstance<?,?> graph,
			MadInstance<?,?> instanceToMap, String name, boolean warnAboutMissingChannels )
		throws RecordNotFoundException, MAConstraintViolationException, DatastoreException;

	public String getNameForNewComponentOfType( MadGraphInstance<?,?> graph, MadDefinition<?,?> definitionToAdd)
		throws DatastoreException;
	public MadInstance<?,?> findInstanceByName( MadGraphInstance<?,?> graph, String name )
		throws DatastoreException, RecordNotFoundException;
	public Collection<MadInstance<?,?>> findAllInstances( MadGraphInstance<?,?> graph )
		throws DatastoreException;
	public boolean renameInstance( MadGraphInstance<?,?> graph, String oldName, String newName, String newNameInGraph )
		throws DatastoreException, RecordNotFoundException, MAConstraintViolationException;
	public void removeInstanceFromGraph( MadGraphInstance<?,?> graph, MadInstance<?,?> instanceToRemove )
		throws DatastoreException, RecordNotFoundException, MAConstraintViolationException;

	// CRUD on links between components within the graph
	public void addLink( MadGraphInstance<?,?> graph, MadLink link )
		throws DatastoreException, RecordNotFoundException, MAConstraintViolationException;

	public void deleteLink( MadGraphInstance<?,?> graph, MadLink link )
		throws DatastoreException, RecordNotFoundException, MAConstraintViolationException;

	public Collection<MadLink> findAllLinks( MadGraphInstance<?,?> graph )
		throws DatastoreException;

	public Set<MadLink> getProducerInstanceLinks( MadGraphInstance<?,?> graph,
			MadInstance<?,?> instance )
		throws DatastoreException;

	public Set<MadLink> getConsumerInstanceLinks( MadGraphInstance<?,?> graph,
			MadInstance<?,?> instance )
		throws DatastoreException;

	// Debugging
	public void dumpGraph( MadGraphInstance<?,?> g );

	// CRUD on exposing links into and out of the graph
	public void exposeAudioInstanceChannelAsGraphChannel( MadGraphInstance<?,?> graph,
			MadChannelInstance graphChannelInstance,
			MadChannelInstance channelInstanceToExpose )
		throws RecordNotFoundException, MAConstraintViolationException, DatastoreException;

	public void removeAudioInstanceChannelAsGraphChannel( MadGraphInstance<?,?> graph,
			MadChannelInstance graphChannelInstance,
			MadChannelInstance channelInstanceExposed )
		throws RecordNotFoundException, DatastoreException, MAConstraintViolationException;

	// Create a copy of the supplied graph where subgraph components are moved up to
	// the root
	public MadGraphInstance<?,?> flattenGraph( MadGraphInstance<?,?> graph )
		throws DatastoreException, MAConstraintViolationException, RecordNotFoundException;

}
