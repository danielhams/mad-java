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

package uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane.oscope;

import java.awt.Graphics;

import uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane.AAColours;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;

public class VerticalScroller extends PacPanel
{
	private static final long serialVersionUID = 7596642941999492647L;

	public VerticalScroller()
	{
		setOpaque(true);
		setBackground(AAColours.BACKGROUND);
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		int width = getWidth();
		int height = getHeight();

		g.setColor( AAColours.BACKGROUND );
		g.fillRect( 0, 0, width - 1, height - 1 );
		
		g.setColor( AAColours.FOREGROUND_WHITISH );
		g.drawRect( 0, 0, width - 1, height - 1);
		
		g.fillRect( 3, 3, width - 6, height - 6 );
	}
}
