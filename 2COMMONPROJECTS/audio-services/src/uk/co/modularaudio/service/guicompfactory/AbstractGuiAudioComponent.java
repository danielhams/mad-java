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

package uk.co.modularaudio.service.guicompfactory;

import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JPanel;

import uk.co.modularaudio.service.gui.plugs.GuiChannelPlug;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.swing.dndtable.GuiDndTableComponent;
import uk.co.modularaudio.util.swing.table.layeredpane.LayeredPaneTableComponent;

public abstract class AbstractGuiAudioComponent extends JPanel implements GuiDndTableComponent, LayeredPaneTableComponent
{
	private static final long serialVersionUID = 1400654882139462865L;

	protected final RackComponent rackComponent;

	public AbstractGuiAudioComponent( final RackComponent rackComponent )
	{
		this.rackComponent = rackComponent;
	}

	public abstract Rectangle getRenderedRectangle();

	public abstract GuiChannelPlug getPlugFromPosition( Point localPoint );

	public abstract GuiChannelPlug getPlugFromMadChannelInstance( MadChannelInstance auChannelInstance );

}
