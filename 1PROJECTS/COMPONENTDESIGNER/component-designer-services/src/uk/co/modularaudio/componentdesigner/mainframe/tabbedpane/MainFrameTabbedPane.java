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

package uk.co.modularaudio.componentdesigner.mainframe.tabbedpane;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.gui.GuiTabbedPane;
import uk.co.modularaudio.service.gui.ContainerTab;
import uk.co.modularaudio.service.gui.ContainerTab.ContainerTabTitleListener;

public class MainFrameTabbedPane extends JTabbedPane implements GuiTabbedPane, ContainerTabTitleListener
{
	private static final long serialVersionUID = -8536408105149344856L;

	private static Log log = LogFactory.getLog( MainFrameTabbedPane.class.getName() );

	public MainFrameTabbedPane()
	{
	}

	@Override
	public void addNewContainerTab( final ContainerTab containerTab, final boolean isCloseable )
	{
		final String title = containerTab.getTitle();
		final JComponent src = containerTab.getJComponent();
		this.addTab( title, src );
		if( isCloseable )
		{
			final int index = this.indexOfComponent( src );
			final LabelAndCloseButton tabLabelAndCloseButton = new LabelAndCloseButton( containerTab );
			this.setTabComponentAt( index, tabLabelAndCloseButton );
		}
		containerTab.addTitleListener( this );

		this.setSelectedComponent( src );
	}

	@Override
	public void removeContainerTab( final ContainerTab containerTab )
	{
		containerTab.removeTitleListener( this );
		this.remove( containerTab.getJComponent() );
	}

	@Override
	public void receiveTitleUpdate( final ContainerTab containerTab, final String newTitle )
	{
		final JComponent jcomp = containerTab.getJComponent();
		final int index = this.indexOfComponent( jcomp );
		if( log.isDebugEnabled() )
		{
			log.debug("Yup, got a title change: " + newTitle );
		}
		final Component c = getTabComponentAt( index );
		if( c instanceof LabelAndCloseButton )
		{
			final LabelAndCloseButton lacb = (LabelAndCloseButton)c;
			lacb.resetTitle( newTitle );
		}
		else
		{
			this.setTitleAt( index, newTitle );
		}
	}
}
