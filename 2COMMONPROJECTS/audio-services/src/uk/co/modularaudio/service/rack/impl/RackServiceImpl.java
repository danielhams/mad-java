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

package uk.co.modularaudio.service.rack.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.rackmasterio.mu.RackMasterIOMadDefinition;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.madcomponentui.MadComponentUiService;
import uk.co.modularaudio.service.madgraph.GraphType;
import uk.co.modularaudio.service.madgraph.MadGraphService;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.rack.DirtyableRackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponentProperties;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackIOLink;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackLink;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadLink;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.MadState;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.table.ContentsAlreadyAddedException;
import uk.co.modularaudio.util.table.NoSuchContentsException;
import uk.co.modularaudio.util.table.TableCellFullException;
import uk.co.modularaudio.util.table.TableIndexOutOfBoundsException;
import uk.co.modularaudio.util.table.impl.TablePrinter;

public class RackServiceImpl implements ComponentWithLifecycle, RackService
{
	private static final String RM_INSTANCE_NAME = "Master IO";

	private static Log log = LogFactory.getLog( RackServiceImpl.class.getName() );

	private MadGraphService graphService;
	private MadComponentService componentService;
	private MadComponentUiService componentUiService;

	@Override
	public void destroy()
	{
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( graphService == null ||
				componentService == null ||
				componentUiService == null )
		{
			throw new ComponentConfigurationException("Service missing dependencies. Check configuration");
		}
	}

	@Override
	public RackDataModel createNewRackDataModel( final String rackName,
			final String rackPath,
			final int numCols,
			final int numRows,
			final boolean withRackIO ) throws DatastoreException
	{
		try
		{
			final MadGraphInstance<?,?> rackGraph = graphService.createNewParameterisedGraph( rackName + " Graph",
					GraphType.APP_GRAPH,
					16, 16,
					16, 16,
					16, 16 );

			final RackDataModel newRackDataModel = new RackDataModel( rackGraph, rackName, rackPath, numCols, numRows);
			if( withRackIO )
			{
				// Add our IO component to the graph at the top
				final MadDefinition<?,?> rackMasterIoDefinition = componentService.findDefinitionById( RackMasterIOMadDefinition.DEFINITION_ID );
				final MadInstance<?,?> rackMasterIoInstance = componentService.createInstanceFromDefinition( rackMasterIoDefinition, null, RM_INSTANCE_NAME );
				final IMadUiInstance<?,?> rackMasterUiInstance = componentUiService.createUiInstanceForInstance( rackMasterIoInstance );
				final RackComponent rackMasterIORackComponent = new RackComponent( RM_INSTANCE_NAME,
						rackMasterIoInstance,
						rackMasterUiInstance );
				newRackDataModel.addContentsAtPosition( rackMasterIORackComponent, 0, 0 );
			}
			newRackDataModel.setDirty( false );
			return newRackDataModel;
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught creating rack data model: " + e.toString();
			log.error( msg, e );
			throw new DatastoreException( msg, e );
		}
	}

	@Override
	public RackDataModel createNewSubRackDataModel( final String subRackName,
			final String subRackPath,
			final int numCols,
			final int numRows,
			final boolean withRackIO ) throws DatastoreException
	{
		if( log.isDebugEnabled() )
		{
			log.debug("Creating new sub rack data model with name: " + subRackName );
		}
		try
		{
			final MadGraphInstance<?,?> subRackGraph = graphService.createNewParameterisedGraph( subRackName + " Graph",
					GraphType.SUB_GRAPH,
					16, 16,
					16, 16,
					16, 16 );

			final RackDataModel subRackDataModel = new RackDataModel( subRackGraph, subRackName, subRackPath, numCols, numRows);

			if( withRackIO )
			{
				// Add our IO component to the graph at the top
				final MadDefinition<?,?> rackMasterIoDefinition = componentService.findDefinitionById( RackMasterIOMadDefinition.DEFINITION_ID );
				final MadInstance<?,?> rackMasterIoInstance = componentService.createInstanceFromDefinition( rackMasterIoDefinition, null, RM_INSTANCE_NAME );
				final IMadUiInstance<?,?> rackMasterUiInstance = componentUiService.createUiInstanceForInstance( rackMasterIoInstance );
				final RackComponent rackMasterIORackComponent = new RackComponent( RM_INSTANCE_NAME,
						rackMasterIoInstance,
						rackMasterUiInstance );
				subRackDataModel.addContentsAtPosition( rackMasterIORackComponent, 0, 0 );
			}
			subRackDataModel.setDirty( false );

			return subRackDataModel;
		}
		catch (final Exception e)
		{
			final String msg = "Exception caught creating sub rack data model: " + e.toString();
			throw new DatastoreException( msg, e );
		}
	}

	@Override
	public String getRackName( final RackDataModel rack )
	{
		return rack.getName();
	}

	@Override
	public void setRackName( final RackDataModel rack, final String newRackName )
	{
		rack.setName( newRackName );
	}

	@Override
	public void setRackDirty( final RackDataModel rack, final boolean dirtyFlag )
	{
		rack.setDirty( dirtyFlag );
	}

	@Override
	public boolean isRackDirty( final RackDataModel rack )
	{
		return rack.isDirty();
	}

	protected void internalAddRackComponentAtPosition( final RackDataModel rack,
			final RackComponent rackComponent,
			final int col,
			final int row )
					throws DatastoreException, MAConstraintViolationException, TableCellFullException, TableIndexOutOfBoundsException
	{
		try
		{
			final MadInstance<?,?> ci = rackComponent.getInstance();

			// Must add to rack before adding to the graph - we want and GUI initialisation
			// (like GUI element bounds) to be set before the component itself gets initialised.
			// This avoids the startup() call on the component without a GUI thing knowing how
			// big it needs to be for buffers
			// However, it is the rack that checks for name collisions, so we need to check for name collisions first
			if( !graphService.checkCanAddInstanceToGraphWithName( rack.getRackGraph(), ci.getInstanceName() ) )
			{
				throw new MAConstraintViolationException( "A component with the name " + ci.getInstanceName() + " already exists in this rack" );
			}

			rack.addContentsAtPosition( rackComponent,  col, row );

			graphService.addInstanceToGraphWithName( rack.getRackGraph(), ci, ci.getInstanceName() );

			if( ci instanceof DirtyableRackComponent )
			{
				final DirtyableRackComponent dirtyableComponent = (DirtyableRackComponent)ci;
				dirtyableComponent.addRackDirtyListener( rack );
			}
			// Mark the rack as dirty
			rack.setDirty( true );
		}
		catch (final ContentsAlreadyAddedException e)
		{
			final String msg ="ContentsAlreadyAddedException caught during internal add: " + e.toString();
			log.error( msg, e );
			throw new DatastoreException( msg, e );
		}
	}

	@Override
	public RackComponent createComponentAtPosition( final RackDataModel rack,
			final MadDefinition<?,?> madDefinition,
			final Map<MadParameterDefinition,String> parameterValues,
			final String name,
			final int col,
			final int row )
					throws TableCellFullException, TableIndexOutOfBoundsException, DatastoreException,
					MAConstraintViolationException, RecordNotFoundException
	{
		// Make a new RackComponent for this component instance and add it to the rack
		try
		{
			final MadInstance<?,?> newAuInstance = componentService.createInstanceFromDefinition( madDefinition, parameterValues, name );
			final IMadUiInstance<?,?> uiInstance = componentUiService.createUiInstanceForInstance( newAuInstance );
			final RackComponent rci = new RackComponent( name,
					newAuInstance,
					uiInstance );
			internalAddRackComponentAtPosition( rack, rci, col, row );

			return rci;
		}
		catch( final MadProcessingException aupe )
		{
			final String msg = "MADProcessingException caught when attempting to create mad instance for rack: " + aupe.toString();
			log.error( msg, aupe );
			throw new DatastoreException( msg, aupe );
		}
	}

	@Override
	public RackComponent createComponent( final RackDataModel rack,
			final MadDefinition<?,?> madDefinition,
			final Map<MadParameterDefinition,String> parameterValues,
			final String name )
					throws TableCellFullException, TableIndexOutOfBoundsException,
					DatastoreException, MAConstraintViolationException, RecordNotFoundException
	{
		final int newCol = 0;
		boolean canAdd = false;
		try
		{
			final MadInstance<?,?> madInstance = componentService.createInstanceFromDefinition( madDefinition, parameterValues, name );
			final IMadUiInstance<?,?> madUiInstance = componentUiService.createUiInstanceForInstance( madInstance );
			final RackComponent rci = new RackComponent( name, madInstance, madUiInstance );
			int testRow = 0;
			for( ; !canAdd && testRow < rack.getNumRows() ; testRow++ )
			{
				try
				{
					if( rack.canStoreContentsAtPosition( rci, newCol, testRow) )
					{
						canAdd = true;
						break;
					}
				}
				catch (final TableIndexOutOfBoundsException | ContentsAlreadyAddedException e)
				{
					final String msg = "Exception caught during addNamedContents: " + e.toString();
					log.error( msg, e );
				}
			}
			if( canAdd )
			{
				// This also sets the isDirty flag of the rack
				internalAddRackComponentAtPosition( rack, rci, newCol, testRow );
			}
			else
			{
				rci.destroy();
				componentUiService.destroyUiInstance( madUiInstance );
				componentService.destroyInstance( madInstance );
				final String msg = "RackService unable to find a row for component " + madInstance.getInstanceName() + " at column (" + newCol + ")";
				throw new TableCellFullException( msg );
			}
			rack.setDirty( true );
			return rci;
		}
		catch(final MadProcessingException aupe)
		{
			final String msg = "MADProcessingException caught when attempting to create mad instance for rack: " + aupe.toString();
			log.error( msg, aupe );
			throw new DatastoreException( msg, aupe );
		}
	}


	@Override
	public void renameContents( final RackDataModel rack,
			final RackComponent component,
			final String newName )
					throws DatastoreException, MAConstraintViolationException, RecordNotFoundException
	{
		final String oldName = component.getComponentName();
		graphService.renameInstance( rack.getRackGraph(), oldName, newName, newName );
		component.setComponentName( newName );
		component.getUiInstance().receiveComponentNameChange( newName );
		rack.setDirty( true );
	}

	public void setGraphService( final MadGraphService graphService)
	{
		this.graphService = graphService;
	}

	@Override
	public String getNameForNewComponentOfType( final RackDataModel rackDataModel,
			final MadDefinition<?,?> typeToAdd)
					throws DatastoreException
	{
		return graphService.getNameForNewComponentOfType( rackDataModel.getRackGraph(), typeToAdd);
	}

	@Override
	public void dumpRack( final RackDataModel rdm )
	{
		if( log.isDebugEnabled() )
		{
			final MadGraphInstance<?,?> g = rdm.getRackGraph();
			log.debug("Rack named " + rdm.getName() );
			log.debug("=====================");
			final Set<RackIOLink> rackIOLinks = rdm.getRackIOLinks();
			for( final RackIOLink ril : rackIOLinks )
			{
				log.debug("RackIOLink: " + ril.toString() );
			}
			final TablePrinter<RackComponent, RackComponentProperties> printer = new TablePrinter<RackComponent, RackComponentProperties>();
			printer.printTableContents( rdm );
			// Now print the links
			final Set<RackLink> links = rdm.getLinks();
			for( final RackLink link : links )
			{
				log.debug("Link: " + link.toString() );
			}
			log.debug("=====================");
			log.debug("Graph named " + g.getInstanceName());
			log.debug("=====================");
			graphService.dumpGraph( g );
			log.debug("=====================");
		}
	}

	@Override
	public RackLink addRackLink( final RackDataModel rack,
			final RackComponent producerRackComponent,
			final MadChannelInstance producerChannelInstance,
			final RackComponent consumerRackComponent,
			final MadChannelInstance consumerChannelInstance  )
					throws RecordNotFoundException, MAConstraintViolationException, DatastoreException
	{
		final MadLink madLink = new MadLink( producerChannelInstance, consumerChannelInstance );
		final MadGraphInstance<?,?> g = rack.getRackGraph();
		graphService.addLink( g,  madLink );
		final RackLink rackLink = new RackLink( producerRackComponent, producerChannelInstance,
				consumerRackComponent, consumerChannelInstance,
				madLink );
		// Let the graph model do the validation for us
		rack.addRackLink( rackLink );
		rack.setDirty( true );
		return rackLink;
	}

	@Override
	public RackIOLink addRackIOLink( final RackDataModel rack,
			final MadChannelInstance rackChannelInstance,
			final RackComponent rackComponent,
			final MadChannelInstance rackComponentChannelInstance )
					throws DatastoreException, RecordNotFoundException, MAConstraintViolationException
	{
		final RackIOLink rackIOLink = new RackIOLink( rackChannelInstance, rackComponent, rackComponentChannelInstance );
		final MadGraphInstance<?,?> g = rack.getRackGraph();

		final MadChannelInstance graphChannelInstance = mapRackIOChannelInstanceToGraphChannelInstance( rack,
				rackIOLink, g );

		//		log.debug("Before exposing as graph channel - graph dump: " );
		//		graphService.dumpGraph(g);

		// Let the graph model do the validation for us
		graphService.exposeAudioInstanceChannelAsGraphChannel( g,  graphChannelInstance,
				rackIOLink.getRackComponentChannelInstance() );
		rack.addRackIOLink( rackIOLink );
		rack.setDirty( true );

		//		log.debug("After exposing as graph channel - graph dump: " );
		//		graphService.dumpGraph(g);

		return rackIOLink;
	}

	private MadChannelInstance mapRackIOChannelInstanceToGraphChannelInstance( final RackDataModel rack,
			final RackIOLink rackIOLink,
			final MadGraphInstance<?,?> graph )
					throws RecordNotFoundException
	{
		final MadChannelInstance mici = rackIOLink.getRackChannelInstance();
		final String rackChannelName = mici.definition.name;
		final MadChannelInstance graphChannelInstance = graph.getChannelInstanceByName( rackChannelName );
		rack.setDirty( true );

		return graphChannelInstance;
	}

	@Override
	public void moveContentsToPosition( final RackDataModel rack,
			final RackComponent component,
			final int x,
			final int y )
					throws DatastoreException, NoSuchContentsException, TableIndexOutOfBoundsException, TableCellFullException
	{
		// Graph doesn't know about positions, just do internal move
		rack.moveContentsToPosition( component, x, y );
		rack.setDirty( true );
	}

	@Override
	public void deleteRackLink( final RackDataModel rack, final RackLink rackLink)
			throws DatastoreException, RecordNotFoundException, MAConstraintViolationException
	{
		final MadGraphInstance<?,?> g = rack.getRackGraph();
		final MadLink madLink = rackLink.getLink();
		graphService.deleteLink( g, madLink );
		rack.removeRackLink( rackLink );
		rack.setDirty( true );
	}

	@Override
	public void deleteRackIOLink( final RackDataModel rack,
			final RackIOLink rackIOLink)
					throws DatastoreException, RecordNotFoundException, MAConstraintViolationException
	{
		final MadGraphInstance<?,?> g = rack.getRackGraph();
		final MadChannelInstance graphChannelInstance = mapRackIOChannelInstanceToGraphChannelInstance( rack, rackIOLink, g );

		graphService.removeAudioInstanceChannelAsGraphChannel( g,
				graphChannelInstance,
				rackIOLink.getRackComponentChannelInstance() );

		rack.removeRackIOLink( rackIOLink );
		rack.setDirty( true );
	}

	@Override
	public MadGraphInstance<?,?> getRackGraphInstance( final RackDataModel rack )
	{
		return rack.getRackGraph();
	}

	@Override
	public void destroyRackDataModel( final RackDataModel rack )
			throws DatastoreException, MAConstraintViolationException
	{
		//		log.debug("Destroying rack data model named: " + rack.getName() );

		// Not necessary - use the remove contents methods on individual components.
		//		rack.removeAllRackIOLinks();
		//		rack.removeAllRackLinks();
		//		rack.removeAllComponents();

		try
		{
			rack.removeAllRackDirtyListeners();

			final ArrayList<RackComponent> components = new ArrayList<RackComponent>( rack.getEntriesAsList() );

			final MadGraphInstance<?,?> rackGraph = rack.getRackGraph();
			if( rackGraph == null )
			{
				log.error("Somehow got a null rack graph to destroy.");
			}

			RackComponent rmToDelete = null;

			for( final RackComponent rc : components )
			{
				final MadInstance<?,?> aui = rc.getInstance();
				if( aui.getDefinition().getId().equals( RackMasterIOMadDefinition.DEFINITION_ID ) )
				{
					rmToDelete = rc;
				}
				else
				{
					if( log.isTraceEnabled() )
					{
						log.trace("Cleaning up component in rack: \"" + aui.getInstanceName() +"\"");
					}
					removeContentsFromRack( rack, rc );
					rc.destroy();
				}
			}

			if( rmToDelete != null )
			{
				final MadInstance<?,?> aui = rmToDelete.getInstance();
				if( log.isTraceEnabled() )
				{
					log.trace("Cleaning up component in rack: \"" + aui.getInstanceName() +"\"");
				}
				rack.removeContents( rmToDelete );
				final IMadUiInstance<?,?> auui = rmToDelete.getUiInstance();
				componentUiService.destroyUiInstance( auui );
				componentService.destroyInstance( aui );
				rmToDelete.destroy();
			}
			rack.dirtyFixToCleanupReferences();
			graphService.destroyGraph( rackGraph, true, true );
		}
		catch( final Exception e )
		{
			final String msg = "Exception caught destroying rack data model: " + e.toString();
			log.error( msg, e );
			throw new DatastoreException( msg,e );
		}
	}

	public void setComponentUiService( final MadComponentUiService componentUiService )
	{
		this.componentUiService = componentUiService;
	}

	public void setComponentService( final MadComponentService componentService )
	{
		this.componentService = componentService;
	}

	@Override
	public void removeContentsFromRack( final RackDataModel rackDataModel,
			final RackComponent componentForAction )
					throws DatastoreException, RecordNotFoundException, MAConstraintViolationException, NoSuchContentsException
	{
		final MadInstance<?,?> madInstance = componentForAction.getInstance();

		// Now remove it from the rack
		final Set<RackLink> allLinks = rackDataModel.getLinks();
		final Set<RackLink> forIter = new HashSet<RackLink>( allLinks );
		for( final RackLink rl : forIter )
		{
			final RackComponent crc = rl.getConsumerRackComponent();
			final RackComponent prc = rl.getProducerRackComponent();
			if( crc == componentForAction || prc == componentForAction )
			{
				rackDataModel.removeRackLink( rl );
			}
		}
		final Set<RackIOLink> allIOLinks = rackDataModel.getRackIOLinks();
		final Set<RackIOLink> forIOIter = new HashSet<RackIOLink>( allIOLinks );
		for( final RackIOLink ril : forIOIter )
		{
			final RackComponent rc = ril.getRackComponent();
			if( rc == componentForAction )
			{
				rackDataModel.removeRackIOLink( ril );
			}
		}
		rackDataModel.removeContents( componentForAction );
		// Mark the rack as dirty
		rackDataModel.setDirty( true );

		graphService.removeInstanceFromGraph( rackDataModel.getRackGraph(), madInstance );

		// Now halt the component
		if( madInstance.getState() == MadState.RUNNING )
		{
			try
			{
				madInstance.internalEngineStop();
			}
			catch (final MadProcessingException e)
			{
				if( log.isErrorEnabled() )
				{
					log.error("Exception caught stopping component: " + e.toString(), e );
				}
			}
		}

		// Call destroy on the ui instance
		final IMadUiInstance<?, ?> componentUiInstance = componentForAction.getUiInstance();
		componentUiInstance.destroy();

		// Now destroy the component UI instance itself
		componentUiService.destroyUiInstance( componentUiInstance );

		// Have destroyed the UI instance, can destroy the instance itself
		componentService.destroyInstance( madInstance );
	}
}
