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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.service.bufferedimageallocation.impl.cache.AllocationCacheForImageType;
import uk.co.modularaudio.service.bufferedimageallocation.impl.debugwindow.imagebrowser.RawImageBrowser;

public class BufferedImageTypeCachePanel extends JPanel implements ActionListener
{
//	private static Log log = LogFactory.getLog( BufferedImageTypeCachePanel.class.getName() );
	
	private static final long serialVersionUID = -5170152282173740254L;
	
	private AllocationCacheForImageType cache = null;
	
	private JButton refreshButton = null;
	private JLabel numRawDisplay = null;
	private JLabel numFreeDisplay = null;
	private JLabel numUsedDisplay = null;
	
	private RawImageBrowser rawImageBrowser = null;
	
	public BufferedImageTypeCachePanel( AllocationCacheForImageType cache )
	{
		this.cache = cache;
		
		MigLayout migLayout = new MigLayout("fillx", "[][grow]", "[][][][][fill,grow]");
		setLayout( migLayout );
		addComponents();
		connectRefreshButton();
	}
	
	private void connectRefreshButton()
	{
		refreshButton.addActionListener( this );
	}
	
	private void addComponents()
	{
		refreshButton = new JButton("Refresh");
		this.add( refreshButton, "wrap");
		JLabel numRawLabel = new JLabel("NumRaw:");
		this.add( numRawLabel, "" );
		numRawDisplay = new JLabel();
		this.add( numRawDisplay, "wrap");

		JLabel numFreeLabel = new JLabel("NumFree:");
		this.add( numFreeLabel, "" );
		numFreeDisplay = new JLabel();
		this.add( numFreeDisplay, "wrap");

		JLabel numUsedLabel = new JLabel("NumUsed:");
		this.add( numUsedLabel, "" );
		numUsedDisplay = new JLabel();
		this.add( numUsedDisplay, "wrap" );
		
		rawImageBrowser = new RawImageBrowser( cache );
		this.add( rawImageBrowser, "spanx 2, grow");
	}

	@Override
	public void actionPerformed( ActionEvent e )
	{
		// Refresh button click.
		int numRaw = cache.getNumRaw();
		numRawDisplay.setText( numRaw + "" );
		int numFree = cache.getNumFree();
		numFreeDisplay.setText( numFree + "" );
		int numUsed = cache.getNumUsed();
		numUsedDisplay.setText( numUsed + "" );
		rawImageBrowser.refreshFromCache();
	}
}
