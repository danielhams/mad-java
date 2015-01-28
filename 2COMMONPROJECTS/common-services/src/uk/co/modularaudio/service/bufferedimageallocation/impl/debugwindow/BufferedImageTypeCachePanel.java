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

	private final AllocationCacheForImageType cache;

	private final JButton refreshButton;
	private final JLabel numRawDisplay;
	private final JLabel numFreeDisplay;
	private final JLabel numUsedDisplay;

	private final RawImageBrowser rawImageBrowser;

	public BufferedImageTypeCachePanel( final AllocationCacheForImageType cache )
	{
		this.cache = cache;

		final MigLayout migLayout = new MigLayout("fillx", "[][grow]", "[][][][][fill,grow]");
		setLayout( migLayout );

		refreshButton = new JButton("Refresh");
		this.add( refreshButton, "wrap");
		final JLabel numRawLabel = new JLabel("NumRaw:");
		this.add( numRawLabel, "" );
		numRawDisplay = new JLabel();
		this.add( numRawDisplay, "wrap");

		final JLabel numFreeLabel = new JLabel("NumFree:");
		this.add( numFreeLabel, "" );
		numFreeDisplay = new JLabel();
		this.add( numFreeDisplay, "wrap");

		final JLabel numUsedLabel = new JLabel("NumUsed:");
		this.add( numUsedLabel, "" );
		numUsedDisplay = new JLabel();
		this.add( numUsedDisplay, "wrap" );

		rawImageBrowser = new RawImageBrowser( cache );
		this.add( rawImageBrowser, "spanx 2, grow");

		refreshButton.addActionListener( this );
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		// Refresh button click.
		final int numRaw = cache.getNumRaw();
		numRawDisplay.setText( numRaw + "" );
		final int numFree = cache.getNumFree();
		numFreeDisplay.setText( numFree + "" );
		final int numUsed = cache.getNumUsed();
		numUsedDisplay.setText( numUsed + "" );
		rawImageBrowser.refreshFromCache();
	}
}
