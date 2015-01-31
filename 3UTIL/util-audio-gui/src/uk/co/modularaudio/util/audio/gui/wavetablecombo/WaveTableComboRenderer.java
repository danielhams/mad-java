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

package uk.co.modularaudio.util.audio.gui.wavetablecombo;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import uk.co.modularaudio.util.audio.lookuptable.LookupTable;
import uk.co.modularaudio.util.swing.mvc.combo.ComboViewListCellRenderer;

public class WaveTableComboRenderer extends JLabel
	implements ComboViewListCellRenderer<WaveTableComboItem>
{
	private static final long serialVersionUID = -6633248241915765586L;

	public WaveTableComboRenderer()
	{
	}

//	private final static Log log = LogFactory.getLog( WaveTableComboRenderer.class.getName() );

	@SuppressWarnings("rawtypes")
	private final ListCellRenderer defaultRendererFactory = new DefaultListCellRenderer();

	@SuppressWarnings("unchecked")
	@Override
	public Component getListCellRendererComponent( final JList<? extends WaveTableComboItem> list,
			final WaveTableComboItem value,
			final int index,
			final boolean isSelected,
			final boolean cellHasFocus )
	{
		Icon icon = null;

		if( isSelected )
		{
			setBackground( list.getSelectionBackground() );
			setForeground( list.getSelectionForeground() );
		}
		else
		{
			setBackground( list.getBackground() );
			setForeground( list.getForeground() );
		}

		if( value != null )
		{
			this.setText( value.getDisplayString() );
			final LookupTable wt = value.getValue();
			final boolean isBipolar = value.isBipolar();
			icon = WaveTableIconCache.getIconForWaveTable( wt, isBipolar );
			setIcon( icon );
		}
		else
		{
			icon = null;
			setIcon( null );
		}
//		log.debug("get renderer called on " + this );

		return defaultRendererFactory.getListCellRendererComponent( list, icon, index, isSelected, cellHasFocus );
//		return this;
	}
}
