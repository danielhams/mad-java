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

package uk.co.modularaudio.service.bufferedimageallocation.impl.debugwindow;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.apache.mahout.math.map.OpenLongObjectHashMap;

import uk.co.modularaudio.service.bufferedimageallocation.impl.cache.AllocationCacheForImageType;
import uk.co.modularaudio.util.exception.DatastoreException;

public class BufferedImageDebugWindow extends JFrame implements ActionListener
{
	private static final long serialVersionUID = -4394218797027660403L;

//	private static final Log log = LogFactory.getLog( BufferedImageDebugWindow.class.getName() );

	private OpenLongObjectHashMap<AllocationCacheForImageType> allocationCachePerTypeMap = null;

	private final JButton consCheckButton;
	private final JPanel comboAndCache;
	private final ChooseBufferTypeCombo chooseBufferTypeCombo;
	private BufferedImageTypeCachePanel bitCachePanel;

	public BufferedImageDebugWindow( final int windowX,
			final int windowY,
			final OpenLongObjectHashMap<AllocationCacheForImageType> bufferedImageTypeToAllocationCacheMap )
		throws DatastoreException
	{
		this.allocationCachePerTypeMap = bufferedImageTypeToAllocationCacheMap;
		this.setTitle( this.getClass().getSimpleName() );
		this.setSize( new Dimension(800,800) );
		this.setLocation( windowX, windowY );

		comboAndCache = new JPanel();
		final MigLayout layout = new MigLayout("fillx, top, left", "", "[][][grow]");
		comboAndCache.setLayout( layout );

		consCheckButton = new JButton("Do consistency check");
		comboAndCache.add( consCheckButton, "wrap" );
		consCheckButton.addActionListener( this );

		chooseBufferTypeCombo = new ChooseBufferTypeCombo( bufferedImageTypeToAllocationCacheMap );
		comboAndCache.add( chooseBufferTypeCombo, "wrap");

		chooseBufferTypeCombo.addActionListener( this );

		this.add( comboAndCache );

//		this.add( new BufferedImageTypeCachePanel( allocationCachePerTypeMap ) );
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		final String actionCommand = e.getActionCommand();
		if( actionCommand.equals("comboBoxChanged") )
		{
			if( bitCachePanel != null )
			{
				comboAndCache.remove( bitCachePanel );
				bitCachePanel = null;
			}

			final long selectedCompoundKey = chooseBufferTypeCombo.getSelectedCompoundKey();

			final AllocationCacheForImageType cacheForImageType = allocationCachePerTypeMap.get( selectedCompoundKey );

			if( cacheForImageType != null )
			{
				bitCachePanel = new BufferedImageTypeCachePanel( cacheForImageType );
				bitCachePanel.actionPerformed( null );
				comboAndCache.add( bitCachePanel, "grow");
			}
		}
		else if( actionCommand.equals("Do consistency check") )
		{
			for( final AllocationCacheForImageType ac : allocationCachePerTypeMap.values() )
			{
				ac.doConsistencyChecks();
			}
		}
		this.revalidate();
		this.repaint();
	}
}
