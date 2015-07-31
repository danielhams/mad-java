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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import uk.co.modularaudio.service.gui.ContainerTab;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacButton;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

class LabelAndCloseButton extends JPanel
{
	private static final long serialVersionUID = 4749639147954010294L;

	private JLabel titleLabel;

	public LabelAndCloseButton( final ContainerTab subrackTab )
	{
		setOpaque( false );
		MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "insets 2" );
		msh.addLayoutConstraint( "gap 5" );
		msh.addLayoutConstraint( "aligny center" );
		this.setLayout( msh.createMigLayout() );
		titleLabel = new JLabel( subrackTab.getTitle() );
		this.add( titleLabel, "");
		final JButton closeButton = new PacButton()
		{
			private static final long serialVersionUID = 4253160873361081364L;

			@Override
			public void receiveEvent( final ActionEvent e )
			{
				subrackTab.doTabClose();
			}
		};
		final Font f = closeButton.getFont();
		closeButton.setMargin( new Insets( 0, 0, 0, 0 ) );
		closeButton.setFont( f.deriveFont( 9f ) );
		closeButton.setText( "x" );
		final Rectangle bounds = new Rectangle( 0, 0, 40, 12  );
		final Dimension sizeDim = new Dimension( bounds.width, bounds.height );
		closeButton.setMinimumSize( sizeDim );
		closeButton.setMaximumSize( sizeDim );
		this.add( closeButton );
	}

	public void resetTitle( final String newTitle )
	{
		titleLabel.setText( newTitle );
	}
}
