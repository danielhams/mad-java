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

package uk.co.modularaudio.service.bufferedimageallocation.impl.debugwindow.imagebrowser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.service.bufferedimageallocation.impl.cache.AllocationCacheForImageType;

public class RawImageBrowser extends JPanel implements ActionListener
{
//	private static Log log = LogFactory.getLog( RawImageBrowser.class.getName() );

	private static final long serialVersionUID = 3311354314509726320L;

	private final RawImageCombo rawImageCombo;
	private final RawImageDisplay rawImageDisplay;

	public RawImageBrowser( final AllocationCacheForImageType cache )
	{
		final MigLayout layout = new MigLayout("fillx", "[][grow]", "[][fill,grow]");
		this.setLayout( layout );

		final JLabel comboLabel = new JLabel("Raw Image:");
		this.add( comboLabel, "" );
		rawImageCombo = new RawImageCombo( this, cache );
		this.add( rawImageCombo, "wrap");
		rawImageDisplay = new RawImageDisplay( cache );
		this.add( rawImageDisplay, "spanx 2, grow" );
	}

	public void refreshFromCache()
	{
		final Object selectedItem = rawImageCombo.getSelectedItem();
		rawImageDisplay.clearDisplayedImage();
		rawImageCombo.refreshFromCache();
		rawImageCombo.setSelectedItem( selectedItem );
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
//		log.debug( "Got an action: " + e.toString() );
		if( e.getActionCommand().equals( "comboBoxChanged" ) )
		{
			final String rawImageIdStr = (String)rawImageCombo.getSelectedItem();
			long rawImageId;
			try
			{
				rawImageId = Long.parseLong( rawImageIdStr );
				rawImageDisplay.displayRawImage( rawImageId );
			}
			catch (final NumberFormatException e1)
			{
			}
		}
	}

}
