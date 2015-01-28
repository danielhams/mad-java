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

import javax.swing.JComboBox;

import uk.co.modularaudio.service.bufferedimageallocation.impl.cache.AllocationCacheForImageType;

public class RawImageCombo extends JComboBox<String>
{
	private static final long serialVersionUID = 8860825475817063033L;
//	private static Log log = LogFactory.getLog( RawImageCombo.class.getName() );

	private final RawImageBrowser rawImageBrowser;
	private final AllocationCacheForImageType cache;

	public RawImageCombo( final RawImageBrowser rawImageBrowser, final AllocationCacheForImageType cache )
	{
		this.rawImageBrowser = rawImageBrowser;
		this.cache = cache;
	}

	public void refreshFromCache()
	{
		this.removeActionListener( rawImageBrowser );
		this.setModel( new RawImageComboModel( cache ) );
		this.addActionListener( rawImageBrowser );
		cache.debugUsedEntries();
	}
}
