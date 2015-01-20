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

package uk.co.modularaudio.componentdesigner.mainframe;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.gui.GuiTabbedPane;
import uk.co.modularaudio.service.gui.SubrackTab;
import uk.co.modularaudio.service.gui.SubrackTab.SubrackTitleListener;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacButton;

public class MainFrameTabbedPane extends JTabbedPane implements GuiTabbedPane, SubrackTitleListener
{
	private static final long serialVersionUID = -8536408105149344856L;
	
	private static Log log = LogFactory.getLog( MainFrameTabbedPane.class.getName() );
	
	public MainFrameTabbedPane()
	{
	}

	@Override
	public void addNewSubrackTab( SubrackTab subrackTab, boolean isCloseable )
	{
		String title = subrackTab.getTitle();
		JComponent src = subrackTab.getJComponent();
		this.addTab( title, src );
		if( isCloseable )
		{
			int index = this.indexOfComponent( src );
			LabelAndCloseButton tabLabelAndCloseButton = new LabelAndCloseButton( subrackTab );
			this.setTabComponentAt( index, tabLabelAndCloseButton );
		}
		subrackTab.addTitleListener( this );
		
		this.setSelectedComponent( src );
	}

	@Override
	public void removeSubrackTab( SubrackTab subrackTab )
	{
		subrackTab.removeTitleListener( this );
		this.remove( subrackTab.getJComponent() );
	}

	@Override
	public void receiveTitleUpdate( SubrackTab subrackTab, String newTitle )
	{
		JComponent jcomp = subrackTab.getJComponent();
		int index = this.indexOfComponent( jcomp );
		log.debug("Yup, got a title change: " + newTitle );
		Component c = getTabComponentAt( index );
		if( c instanceof LabelAndCloseButton )
		{
			LabelAndCloseButton lacb = (LabelAndCloseButton)c;
			lacb.resetTitle( newTitle );
		}
		else
		{
			this.setTitleAt( index, newTitle );
		}
	}

	private class LabelAndCloseButton extends JPanel
	{
		private static final long serialVersionUID = 4749639147954010294L;
		
		private JLabel titleLabel = null;

		public LabelAndCloseButton( final SubrackTab subrackTab )
		{
			setOpaque( false );
			MigLayout layout = new MigLayout("insets 0, gap 0");
			this.setLayout( layout );
			titleLabel = new JLabel( subrackTab.getTitle() );
			this.add( titleLabel, "");
			JButton closeButton = new PacButton()
			{
				private static final long serialVersionUID = 4253160873361081364L;

				@Override
				public void receiveEvent( ActionEvent e )
				{
					subrackTab.doTabClose();
				}
			};
			Font f = closeButton.getFont();
			closeButton.setMargin( new Insets( 0, 0, 0, 0 ) );
			closeButton.setFont( f.deriveFont( 9f ) );
			closeButton.setText( "x" );
			Rectangle bounds = new Rectangle( 0, 0, 40, 12  );
			Dimension sizeDim = new Dimension( bounds.width, bounds.height );
			closeButton.setMinimumSize( sizeDim );
			closeButton.setMaximumSize( sizeDim );
			this.add( closeButton );
		}

		public void resetTitle( String newTitle )
		{
			titleLabel.setText( newTitle );
		}
	}
}
