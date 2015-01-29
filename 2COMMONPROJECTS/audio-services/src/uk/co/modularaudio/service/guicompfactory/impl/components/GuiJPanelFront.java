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

package uk.co.modularaudio.service.guicompfactory.impl.components;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JPanel;

import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;

public class GuiJPanelFront extends JPanel
{
	private static final long serialVersionUID = -3269360028279565801L;
//	private static Log log = LogFactory.getLog( GuiJPanelFront.class.getName() );
	
	private ComponentNameLabel componentNameLabel = null;

	public GuiJPanelFront( RackComponent inComponent )
	{
		this.setOpaque( false );
		this.setLayout( null );
		componentNameLabel = new ComponentNameLabel( inComponent );
		this.add( componentNameLabel );
		AbstractMadUiControlInstance<?,?,?>[] uiControls = inComponent.getUiControlInstances();
		for( AbstractMadUiControlInstance<?,?,?> uic : uiControls )
		{
			Component swingComponent = uic.getControl();
			this.add(swingComponent );
			swingComponent.setBounds( uic.getUiControlDefinition().getControlBounds() );
		}
	}

	@Override
	public void paint(Graphics g)
	{
		super.paintChildren( g );
	}

	public void destroy()
	{
//		log.debug("GuiJPanelFront destroy called");
		this.removeAll();		
	}
}
