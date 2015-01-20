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
import uk.co.modularaudio.util.audio.gui.mad.MadUiInstance;
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
import uk.co.modularaudio.util.component.ComponentWithPostInitPreShutdown;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.table.ContentsAlreadyAddedException;
import uk.co.modularaudio.util.table.NoSuchContentsException;
import uk.co.modularaudio.util.table.TableCellFullException;
import uk.co.modularaudio.util.table.TableIndexOutOfBoundsException;
import uk.co.modularaudio.util.table.impl.TablePrinter;

public class RackServiceImpl implements ComponentWithLifecycle, ComponentWithPostInitPreShutdown, RackService
{
	private static Log log = LogFactory.getLog( RackServiceImpl.class.getName() );
	
	private MadGraphService graphService = null;
	private MadComponentService componentService = null;
	private MadComponentUiService componentUiService = null;
	
	@Override
	public void destroy()
	{
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
	}

	@Override
	public void postInit()
	{
	}
	
	@Override
	public void preShutdown()
	{
	}
	
	@Override
	public RackDataModel createNewRackDataModel( String rackName, String rackPath, int numCols, int numRows, boolean withRackIO ) throws DatastoreException
	{
		try
		{
			MadGraphInstance<?,?> rackGraph = graphService.createNewParameterisedGraph( rackName + " Graph",
					GraphType.APP_GRAPH,
					16, 16,
					16, 16,
					16, 16 );
			
			RackDataModel newRackDataModel = new RackDataModel( rackGraph, rackName, rackPath, numCols, numRows);
			if( withRackIO )
			{
				// Add our IO component to the graph at the top
				String rackMasterIoInstanceName = "Master IO";
				MadDefinition<?,?> rackMasterIoDefinition = componentService.findDefinitionById( RackMasterIOMadDefinition.DEFINITION_ID );
				MadInstance<?,?> rackMasterIoInstance = componentService.createInstanceFromDefinition( rackMasterIoDefinition, null, rackMasterIoInstanceName );
				MadUiInstance<?,?> rackMasterUiInstance = componentUiService.createUiInstanceForInstance( rackMasterIoInstance );
				RackComponent rackMasterIORackComponent = new RackComponent( rackMasterIoInstanceName,
						rackMasterIoInstance,
						rackMasterUiInstance );
				newRackDataModel.addContentsAtPosition( rackMasterIORackComponent, 0, 0 );
			}
			newRackDataModel.setDirty( false );
			return newRackDataModel;
		}
		catch (Exception e)
		{
			String msg = "Exception caught creating rack data model: " + e.toString();
			log.error( msg, e );
			throw new DatastoreException( msg, e );
		}
	}
	
	@Override
	public RackDataModel createNewSubRackDataModel( String subRackName, String subRackPath, int numCols, int numRows, boolean withRackIO ) throws DatastoreException
	{
		log.debug("Creating new sub rack data model with name: " + subRackName );
		try
		{
			MadGraphInstance<?,?> subRackGraph = graphService.createNewParameterisedGraph( subRackName + " Graph",
					GraphType.SUB_GRAPH,
					16, 16,
					16, 16,
					16, 16 );
			
			RackDataModel subRackDataModel = new RackDataModel( subRackGraph, subRackName, subRackPath, numCols, numRows);
			
			if( withRackIO )
			{
				// Add our IO component to the graph at the top
				MadDefinition<?,?> rackMasterIoDefinition = componentService.findDefinitionById( RackMasterIOMadDefinition.DEFINITION_ID );
				String rackMasterIoInstanceName = "Master IO";
				MadInstance<?,?> rackMasterIoInstance = componentService.createInstanceFromDefinition( rackMasterIoDefinition, null, rackMasterIoInstanceName );
				MadUiInstance<?,?> rackMasterUiInstance = componentUiService.createUiInstanceForInstance( rackMasterIoInstance );
				RackComponent rackMasterIORackComponent = new RackComponent( rackMasterIoInstanceName,
						rackMasterIoInstance,
						rackMasterUiInstance );
				subRackDataModel.addContentsAtPosition( rackMasterIORackComponent, 0, 0 );
			}
			subRackDataModel.setDirty( false );

			return subRackDataModel;
		}
		catch (Exception e)
		{
			String msg = "Exception caught creating sub rack data model: " + e.toString();
			throw new DatastoreException( msg, e );
		}
	}
	
	protected void internalAddRackComponentAtPosition( RackDataModel rack,
			RackComponent rackComponent,
			int col,
			int row ) throws DatastoreException, MAConstraintViolationException, ContentsAlreadyAddedException, TableCellFullException, TableIndexOutOfBoundsException
	{
		MadInstance<?,?> ci = rackComponent.getInstance();
		
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
			DirtyableRackComponent dirtyableComponent = (DirtyableRackComponent)ci;
			dirtyableComponent.addRackDirtyListener( rack );
		}
		// Mark the rack as dirty
		rack.setDirty( true );
	}
	
	@Override
	public RackComponent createComponentAtPosition( RackDataModel rack, MadDefinition<?,?> madDefinition,
			Map<MadParameterDefinition,String> parameterValues, String name, int col, int row )
			throws ContentsAlreadyAddedException, TableCellFullException, TableIndexOutOfBoundsException, DatastoreException, MAConstraintViolationException, RecordNotFoundException
	{
		// Make a new RackComponent for this component instance and add it to the rack
		try
		{
			MadInstance<?,?> newAuInstance = componentService.createInstanceFromDefinition( madDefinition, parameterValues, name );
			MadUiInstance<?,?> uiInstance = componentUiService.createUiInstanceForInstance( newAuInstance );
			RackComponent rci = new RackComponent( name,
					newAuInstance,
					uiInstance );
			internalAddRackComponentAtPosition( rack, rci, col, row );

			return rci;
		}
		catch( MadProcessingException aupe )
		{
			String msg = "MADProcessingException caught when attempting to create mad instance for rack: " + aupe.toString();
			log.error( msg, aupe );
			throw new DatastoreException( msg, aupe );
		}
	}
	
	@Override
	public RackComponent createComponent( RackDataModel rack, MadDefinition<?,?> madDefinition, 
			Map<MadParameterDefinition,String> parameterValues, String name )
			throws ContentsAlreadyAddedException, TableCellFullException, TableIndexOutOfBoundsException, DatastoreException, MAConstraintViolationException, RecordNotFoundException
	{
		RackComponent rci = null;
		int newCol = 0;
		int newRow = 0;
		boolean canAdd = false;
		try
		{
			MadInstance<?,?> madInstance = componentService.createInstanceFromDefinition( madDefinition, parameterValues, name );
			MadUiInstance<?,?> madUiInstance = componentUiService.createUiInstanceForInstance( madInstance );
			rci = new RackComponent( name, madInstance, madUiInstance );
			for( int j = 0 ; !canAdd && j < rack.getNumRows() ; j++ )
			{
				try
				{
					if( rack.canStoreContentsAtPosition( rci, newCol, j) )
					{
						newRow = j;
						canAdd =true;
					}
				}
				catch (TableIndexOutOfBoundsException e)
				{
					String msg = "TableIndexOutOfBoundsException caught during addNamedContents: " + e.toString();
					log.error( msg, e );
				}
			}
			if( canAdd )
			{
				// This also sets the isDirty flag of the rack
				internalAddRackComponentAtPosition( rack, rci, newCol, newRow );
			}
			else
			{
				componentUiService.destroyUiInstance( madUiInstance );
				componentService.destroyInstance( madInstance );
				String msg = "RackService unable to add component " + madInstance.getInstanceName() + " at location (" + newCol + ", " + newRow + ")";
				throw new TableCellFullException( msg );
			}
			rack.setDirty( true );
			return rci;
		}
		catch(MadProcessingException aupe)
		{
			String msg = "MADProcessingException caught when attempting to create mad instance for rack: " + aupe.toString();
			log.error( msg, aupe );
			throw new DatastoreException( msg, aupe );
		}
	}


	@Override
	public void renameContents( RackDataModel rack, RackComponent component,
			String newName )
			throws DatastoreException, MAConstraintViolationException, RecordNotFoundException
	{
		String oldName = component.getComponentName();
		graphService.renameInstance( rack.getRackGraph(), oldName, newName, newName );
		component.setComponentName( newName );
		component.getUiInstance().receiveComponentNameChange( newName );
		rack.setDirty( true );
	}

	public MadGraphService getGraphService()
	{
		return graphService;
	}

	public void setGraphService(MadGraphService graphService)
	{
		this.graphService = graphService;
	}

	@Override
	public String getNameForNewComponentOfType(RackDataModel rackDataModel, MadDefinition<?,?> typeToAdd) throws DatastoreException
	{
		return graphService.getNameForNewComponentOfType( rackDataModel.getRackGraph(), typeToAdd);
	}

	@Override
	public void dumpRack(RackDataModel rdm)
	{
		MadGraphInstance<?,?> g = rdm.getRackGraph();
		log.debug("Rack named " + rdm.getName() );
		log.debug("=====================");
		Set<RackIOLink> rackIOLinks = rdm.getRackIOLinks();
		for( RackIOLink ril : rackIOLinks )
		{
			log.debug("RackIOLink: " + ril.toString() );
		}
		TablePrinter<RackComponent, RackComponentProperties> printer = new TablePrinter<RackComponent, RackComponentProperties>();
		printer.printTableContents( rdm );
		// Now print the links
		Set<RackLink> links = rdm.getLinks();
		for( RackLink link : links )
		{
			log.debug("Link: " + link.toString() );
		}
		log.debug("=====================");
		log.debug("Graph named " + g.getInstanceName());
		log.debug("=====================");
		graphService.dumpGraph( g );
		log.debug("=====================");
	}

	@Override
	public RackLink addRackLink( RackDataModel rack, RackComponent producerRackComponent, MadChannelInstance producerChannelInstance,
			RackComponent consumerRackComponent, MadChannelInstance consumerChannelInstance  )
			throws RecordNotFoundException, MAConstraintViolationException, DatastoreException
	{
		MadLink madLink = new MadLink( producerChannelInstance, consumerChannelInstance );
		MadGraphInstance<?,?> g = rack.getRackGraph();
		graphService.addLink( g,  madLink );
		RackLink rackLink = new RackLink( producerRackComponent, producerChannelInstance, consumerRackComponent, consumerChannelInstance,
				madLink );
		// Let the graph model do the validation for us
		rack.addRackLink( rackLink );
		rack.setDirty( true );
		return rackLink;
	}
	
	@Override
	public RackIOLink addRackIOLink( RackDataModel rack, MadChannelInstance rackChannelInstance, RackComponent rackComponent,
			MadChannelInstance rackComponentChannelInstance )
			throws DatastoreException, RecordNotFoundException, MAConstraintViolationException
	{
		RackIOLink rackIOLink = new RackIOLink( rackChannelInstance, rackComponent, rackComponentChannelInstance );
		MadGraphInstance<?,?> g = rack.getRackGraph();
		
		MadChannelInstance graphChannelInstance = mapRackIOChannelInstanceToGraphChannelInstance( rack,
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
	
	private MadChannelInstance mapRackIOChannelInstanceToGraphChannelInstance( RackDataModel rack,
			RackIOLink rackIOLink,
			MadGraphInstance<?,?> graph )
		throws RecordNotFoundException
	{
		MadChannelInstance mici = rackIOLink.getRackChannelInstance();
		String rackChannelName = mici.definition.name;
		MadChannelInstance graphChannelInstance = graph.getChannelInstanceByName( rackChannelName );
		rack.setDirty( true );

		return graphChannelInstance;
	}

	@Override
	public void moveContentsToPosition(RackDataModel rack, RackComponent component, int x, int y) throws DatastoreException, NoSuchContentsException, TableIndexOutOfBoundsException, TableCellFullException
	{
		// Graph doesn't know about positions, just do internal move
		rack.moveContentsToPosition( component, x, y );
		rack.setDirty( true );

	}

	@Override
	public void deleteRackLink(RackDataModel rack, RackLink rackLink) throws DatastoreException, RecordNotFoundException, MAConstraintViolationException
	{
		MadGraphInstance<?,?> g = rack.getRackGraph();
		MadLink madLink = rackLink.getLink();
		graphService.deleteLink( g, madLink );
		rack.removeRackLink( rackLink );
		rack.setDirty( true );
	}
	
	@Override
	public void deleteRackIOLink( RackDataModel rack, RackIOLink rackIOLink) throws DatastoreException, RecordNotFoundException, MAConstraintViolationException
	{
		MadGraphInstance<?,?> g = rack.getRackGraph();
		MadChannelInstance graphChannelInstance = mapRackIOChannelInstanceToGraphChannelInstance( rack, rackIOLink, g );
		
		graphService.removeAudioInstanceChannelAsGraphChannel( g, 
				graphChannelInstance, 
				rackIOLink.getRackComponentChannelInstance() );
		
		rack.removeRackIOLink( rackIOLink );
		rack.setDirty( true );

	}

	@Override
	public MadGraphInstance<?,?> getRackGraphInstance( RackDataModel rack )
	{
		return rack.getRackGraph();
	}

	@Override
	public void destroyRackDataModel(RackDataModel rack) throws DatastoreException, MAConstraintViolationException
	{
//		log.debug("Destroying rack data model named: " + rack.getName() );

		// Not necessary - use the remove contents methods on individual components.
//		rack.removeAllRackIOLinks();
//		rack.removeAllRackLinks();
//		rack.removeAllComponents();
		
		try
		{
			rack.removeAllRackDirtyListeners();

			ArrayList<RackComponent> components = new ArrayList<RackComponent>( rack.getEntriesAsList() );
			
			MadGraphInstance<?,?> rackGraph = rack.getRackGraph();
			if( rackGraph == null )
			{
				log.error("Somehow got a null rack graph to destroy.");
			}
			
			RackComponent rackMasterIOToDelete = null;
			
			for( RackComponent rc : components )
			{
				MadInstance<?,?> aui = rc.getInstance();
				if( aui.getDefinition().getId().equals( "rack_master_io" ) )
				{
					rackMasterIOToDelete = rc;
				}
				else
				{
					removeContentsFromRack( rack, rc );
					rc.destroy();			
				}
			}
			
			if( rackMasterIOToDelete != null )
			{
				MadInstance<?,?> aui = rackMasterIOToDelete.getInstance();
				rack.removeContents( rackMasterIOToDelete );
				MadUiInstance<?,?> auui = rackMasterIOToDelete.getUiInstance();
				componentUiService.destroyUiInstance( auui );
				componentService.destroyInstance( aui );
				rackMasterIOToDelete.destroy();
			}
			rack.dirtyFixToCleanupReferences();
			graphService.destroyGraph( rackGraph, true, true );
		}
		catch( Exception e )
		{
			String msg = "Exception caught destroying rack data model: " + e.toString();
			log.error( msg, e );
			throw new DatastoreException( msg,e );
		}
	}

	public MadComponentUiService getComponentUiService()
	{
		return componentUiService;
	}

	public void setComponentUiService(MadComponentUiService componentUiService)
	{
		this.componentUiService = componentUiService;
	}

	public MadComponentService getComponentService()
	{
		return componentService;
	}

	public void setComponentService( MadComponentService componentService )
	{
		this.componentService = componentService;
	}

	@Override
	public void removeContentsFromRack( RackDataModel rackDataModel,
			RackComponent componentForAction )
			throws DatastoreException, RecordNotFoundException, MAConstraintViolationException, NoSuchContentsException
	{
		MadInstance<?,?> madInstance = componentForAction.getInstance();

		// Now remove it from the rack
		Set<RackLink> allLinks = rackDataModel.getLinks();
		Set<RackLink> forIter = new HashSet<RackLink>( allLinks );
		for( RackLink rl : forIter )
		{
			RackComponent crc = rl.getConsumerRackComponent();
			RackComponent prc = rl.getProducerRackComponent();
			if( crc == componentForAction || prc == componentForAction )
			{
				rackDataModel.removeRackLink( rl );
			}
		}
		Set<RackIOLink> allIOLinks = rackDataModel.getRackIOLinks();
		Set<RackIOLink> forIOIter = new HashSet<RackIOLink>( allIOLinks );
		for( RackIOLink ril : forIOIter )
		{
			RackComponent rc = ril.getRackComponent();
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
			catch (MadProcessingException e)
			{
				log.error("Exception caught stopping component: " + e.toString(), e );
			}
		}
		
		// Call destroy on the ui instance
		MadUiInstance<?, ?> componentUiInstance = componentForAction.getUiInstance();
		componentUiInstance.destroy();
		
		// Now destroy the component UI instance itself
		componentUiService.destroyUiInstance( componentUiInstance );
		
		// Have destroyed the UI instance, can destroy the instance itself
		componentService.destroyInstance( madInstance );
	}
}
