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

import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.bufferedimageallocation.impl.cache.AllocationCacheForImageType;
import uk.co.modularaudio.service.bufferedimageallocation.impl.cache.FreeEntry;
import uk.co.modularaudio.service.bufferedimageallocation.impl.cache.RawImage;
import uk.co.modularaudio.service.bufferedimageallocation.impl.cache.UsedEntry;

public class RawImageDisplay extends JPanel
{
	private static Log log = LogFactory.getLog( RawImageDisplay.class.getName() );

	private static final long serialVersionUID = -6045916326255358082L;

	private final JScrollPane scrollPane;
	private final RawImageCanvas canvas;

	private final AllocationCacheForImageType cache;

	public RawImageDisplay( final AllocationCacheForImageType cache )
	{
		this.cache = cache;

		final MigLayout layout = new MigLayout("fill");
		this.setLayout( layout );
		canvas = new RawImageCanvas();
//		scrollPane.getViewport().add( canvas );
		scrollPane = new JScrollPane( canvas );
		scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
		scrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
		scrollPane.getVerticalScrollBar().setUnitIncrement( 20 );
		this.add( scrollPane, "grow" );
	}

	public void clearDisplayedImage()
	{
		scrollPane.getViewport().remove( canvas );
		canvas.clearDisplayedImage();
		scrollPane.getViewport().add( canvas );
	}

	public void displayRawImage( final long rawImageId )
	{
		scrollPane.getViewport().remove( canvas );
		final RawImage ri = cache.getRawImageById( rawImageId );
		final Set<FreeEntry> freeEntrySet = cache.getRawImageFreeEntrySet( ri );
		final Set<UsedEntry> usedEntrySet = cache.getRawImageUsedEntrySet( ri );
		if( freeEntrySet == null || usedEntrySet == null )
		{
			log.error("Oops. Not sure how we got here.");
		}
		canvas.setDisplayedImage( ri, freeEntrySet, usedEntrySet );
		scrollPane.getViewport().add( canvas );
	}

}
