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

package uk.co.modularaudio.util.audio.gui.mad.rack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.swing.table.GuiTableDataModel;
import uk.co.modularaudio.util.table.NoSuchContentsException;
import uk.co.modularaudio.util.table.TableModelEvent;

public class RackDataModel extends GuiTableDataModel<RackComponent, RackComponentProperties>
	implements RackDirtyListener
{
	private static Log log = LogFactory.getLog( RackDataModel.class.getName() );
	
	private MadGraphInstance<?,?> rackGraph = null;
	
	private String name = null;
	private String path = null;
	
	private boolean isDirty = false;
	
	private List<RackLink> rackLinks = new ArrayList<RackLink>();
	private List<RackIOLink> rackIOLinks = new ArrayList<RackIOLink>();
	
	private List<RackLinkListener> linkListeners = new ArrayList<RackLinkListener>();
	private List<RackIOLinkListener> rackIOLinkListeners = new ArrayList<RackIOLinkListener>();
	
	private List<RackDirtyListener> rackDirtyListeners = new ArrayList<RackDirtyListener>();
	
	private RackLinkEvent rackLinkEvent = new RackLinkEvent( this, 0, 0, 0 );

	public RackDataModel( MadGraphInstance<?,?> rackGraph, String name,  String path, int numCols, int numRows)
	{
		super(numCols, numRows);
		this.rackGraph = rackGraph;
		this.name = name;
		this.path = path;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getPath()
	{
		return path;
	}
	
	public void setPath( String path )
	{
		this.path = path;
	}
	
	public MadGraphInstance<?,?> getRackGraph()
	{
		return rackGraph;
	}

	public void addRackLinksListener(RackLinkListener listener)
	{
		linkListeners.add( listener );		
	}

	public void removeRackLinksListener(RackLinkListener listener)
	{
		linkListeners.remove( listener );
	}
	
	public void addRackLink( RackLink link )
	{
		rackLinks.add( link );
		int newIndex = rackLinks.indexOf( link );
		// Notify listeners that a row has been added
		rackLinkEvent.setValues( this, newIndex, newIndex, RackLinkEvent.INSERT );
		fireRackLinkEvent( rackLinkEvent );
	}
	
	public void removeRackLink( RackLink link )
	{
		int index = rackLinks.indexOf( link );
		rackLinks.remove( link );
		rackLinkEvent.setValues( this, index, index, RackLinkEvent.DELETE );
		fireRackLinkEvent( rackLinkEvent );
	}
	
	public void removeAllRackLinks()
	{
		int numRackLinks = rackLinks.size();
		if(  numRackLinks > 0 )
		{
			int lastLinkIndex = numRackLinks - 1;
			rackLinks.clear();
			rackLinkEvent.setValues( this,  0, lastLinkIndex, RackLinkEvent.DELETE );
			fireRackLinkEvent( rackLinkEvent );
		}
	}

	public void addRackIOLinksListener(RackIOLinkListener listener)
	{
		rackIOLinkListeners.add( listener );
	}

	public void removeRackIOLinksListener(RackIOLinkListener listener)
	{
		rackIOLinkListeners.remove( listener );
	}
	
	public void addRackIOLink( RackIOLink rackIOLink )
	{
		rackIOLinks.add( rackIOLink );
		int newIndex = rackIOLinks.indexOf( rackIOLink );
		// Notify listeners that a row has been added
		RackIOLinkEvent outEvent = new RackIOLinkEvent( this, newIndex, newIndex, RackIOLinkEvent.INSERT );
		fireRackIOLinkEvent( outEvent );
	}
	
	public void removeRackIOLink( RackIOLink rackIOLink )
	{
		int index = rackIOLinks.indexOf( rackIOLink );
		rackIOLinks.remove( rackIOLink );
		RackIOLinkEvent outEvent = new RackIOLinkEvent( this, index, index, RackLinkEvent.DELETE );
		fireRackIOLinkEvent( outEvent );
	}

	public void removeAllRackIOLinks()
	{
		int numRackIOLinks = rackIOLinks.size();
		if(  numRackIOLinks > 0 )
		{
			int lastIOLinkIndex = numRackIOLinks - 1;
			rackIOLinks.clear();
			RackIOLinkEvent outEvent = new RackIOLinkEvent( this, 0, lastIOLinkIndex, RackIOLinkEvent.DELETE );
			fireRackIOLinkEvent( outEvent );
		}
	}

	public void removeAllComponents()
	{
		try
		{
			ArrayList<RackComponent> componentsToRemove = new ArrayList<RackComponent>( getEntriesAsList() );
			int lastComponentIndex = componentsToRemove.size() - 1;
			
			for( RackComponent rc : componentsToRemove )
			{
				noEventFireRemoveContents( rc );
			}
			outEvent.setValues( this, 0, lastComponentIndex, TableModelEvent.DELETE );
			fireTableChangedEvent( outEvent );
		}
		catch( NoSuchContentsException nsce )
		{
			String msg = "Exception caught clearing all components: " + nsce.toString();
			log.error( msg, nsce );
		}
	}

	public RackLink getLinkAt(int elementNum)
	{
		return rackLinks.get( elementNum );
	}

	public int getNumLinks()
	{
		return rackLinks.size();
	}

	public Set<RackLink> getLinks()
	{
		return new HashSet<RackLink>(rackLinks);
	}

	public MadChannelInstance getRackIOChannelInstanceByName( String channelName ) throws RecordNotFoundException
	{
		return rackGraph.getChannelInstanceByName( channelName );
	}

	public Set<RackIOLink> getRackIOLinks()
	{
		return new HashSet<RackIOLink>( rackIOLinks );
	}

	public RackIOLink getIOLinkAt(int elementNum)
	{
		return rackIOLinks.get( elementNum );
	}

	public int getNumIOLinks()
	{
		return rackIOLinks.size();
	}

	private void fireRackLinkEvent(RackLinkEvent event)
	{
		for( RackLinkListener listener : linkListeners )
		{
			listener.linksChanged( event );
		}		
	}

	private void fireRackIOLinkEvent( RackIOLinkEvent outEvent )
	{
		for( RackIOLinkListener listener : rackIOLinkListeners )
		{
			listener.ioLinksChanged( outEvent );
		}
	}

	public void setDirty( boolean isDirty )
	{
		this.isDirty = isDirty;
		if( isDirty )
		{
			for( RackDirtyListener l : rackDirtyListeners )
			{
				l.receiveRackDirty();
			}
		}
	}
	
	public boolean isDirty()
	{
		return isDirty;
	}
	
	public void addRackDirtyListener( RackDirtyListener listener )
	{
		rackDirtyListeners.add( listener );
	}
	
	public void removeRackDirtyListener( RackDirtyListener listener )
	{
		rackDirtyListeners.remove( listener );
	}

	public void removeAllRackDirtyListeners()
	{
		rackDirtyListeners.clear();
	}

	public void setName( String rackName )
	{
		this.name = rackName;		
	}

	@Override
	public void receiveRackDirty()
	{
		setDirty( true );
	}

	@Override
	public void dirtyFixToCleanupReferences()
	{
		rackLinkEvent.setValues( null,  -1, -1, -1 );
		super.dirtyFixToCleanupReferences();
		rackGraph = null;
	}
}
