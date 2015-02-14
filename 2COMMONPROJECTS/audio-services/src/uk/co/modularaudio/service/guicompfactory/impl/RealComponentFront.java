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

import javax.swing.JPanel;

import uk.co.modularaudio.service.guicompfactory.impl.components.PaintedComponentDefines;
import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;

class RealComponentFront extends JPanel
{
	private static final long serialVersionUID = 5211955307472576952L;

	public RealComponentFront( final RackComponent rc )
	{
		this.setOpaque( false );
		this.setLayout( null );
		final AbstractMadUiControlInstance<?,?,?>[] uiControls = rc.getUiControlInstances();
		for( final AbstractMadUiControlInstance<?,?,?> uic : uiControls )
		{
			final Component swingComponent = uic.getControl();
			this.add(swingComponent );
			swingComponent.setBounds( uic.getUiControlDefinition().getControlBounds() );
		}

		final Dimension size = new Dimension( PaintedComponentDefines.FRONT_MIN_WIDTH,
				PaintedComponentDefines.FRONT_MIN_HEIGHT );
		setSize( size );
		setMinimumSize( size );
		setPreferredSize( size );

	}
}
