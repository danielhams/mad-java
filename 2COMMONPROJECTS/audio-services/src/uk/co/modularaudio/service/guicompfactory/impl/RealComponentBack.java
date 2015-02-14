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

package uk.co.modularaudio.service.guicompfactory.impl;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JPanel;

import uk.co.modularaudio.service.gui.plugs.GuiChannelPlug;
import uk.co.modularaudio.service.guicompfactory.impl.components.PaintedComponentDefines;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;

class RealComponentBack extends JPanel
{
	private static final long serialVersionUID = 5211955307472576952L;

	public RealComponentBack( final ResizableBackContainer resizableBackContainer, final RackComponent rc )
	{
		this.setOpaque( false );
		this.setLayout( null );

		for (final GuiChannelPlug plug : resizableBackContainer.plugsToDestroy)
		{
			this.add( plug );
		}

		final Dimension size = new Dimension( PaintedComponentDefines.BACK_MIN_WIDTH,
				PaintedComponentDefines.BACK_MIN_HEIGHT );
		setSize( size );
		setMinimumSize( size );
		setPreferredSize( size );
	}

	public GuiChannelPlug getPlugFromPosition( final Point localPoint )
	{
		// log.debug("Looking for plug at real position " + localPoint );
		GuiChannelPlug retVal = null;
		final Component c = this.getComponentAt( localPoint );
		if (c != null)
		{
			if (c instanceof GuiChannelPlug)
			{
				retVal = (GuiChannelPlug) c;
			}
		}
		return retVal;
	}
}
