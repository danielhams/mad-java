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

package test.uk.co.modularaudio.util.swing.lwtc;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCToggleButton;

public class TestShowLWTCToggleButton
{
	private static Log log = LogFactory.getLog( TestShowLWTCToggleButton.class.getName() );

	private final LWTCToggleButton tdb;
	private final JToggleButton otherButton;

	public TestShowLWTCToggleButton()
	{
		tdb = new LWTCToggleButton( LWTCControlConstants.STD_TOGGLE_BUTTON_COLOURS, "Kill A", false, false )
		{
			private static final long serialVersionUID = -359196738631950261L;

			@Override
			public void receiveUpdateEvent( final boolean previousValue, final boolean newValue )
			{
				log.debug("Received update event from " + previousValue + " to " + newValue );
			}
		};
		tdb.setMinimumSize( new Dimension( 75, 30 ) );
		otherButton = new JToggleButton( "Kill B", false );
		otherButton.setMinimumSize( new Dimension( 75,30 ) );
		final Font f = otherButton.getFont();
		log.debug("Regular button font size = " + f.toString() );

		otherButton.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( final ActionEvent e )
			{
				log.debug("Received action event: " + e.toString() );
			}

		});

		tdb.setSelected( true );
		otherButton.setSelected( true );
	}

	public void go() throws Exception
	{

		final JFrame f = new JFrame();
		final MigLayoutStringHelper msg = new MigLayoutStringHelper();
		msg.addLayoutConstraint( "fill" );
		msg.addLayoutConstraint( "insets 0" );
		msg.addLayoutConstraint( "gap 0" );
		msg.addColumnConstraint( "[][grow][]" );
		msg.addRowConstraint( "[][grow][][grow][]" );
		f.setLayout( msg.createMigLayout() );

		f.add( new JLabel("TL"), "center");
		f.add( new JLabel("TM"), "center");
		f.add( new JLabel("TR"), "center,wrap");
		f.add( new JLabel("ML"), "center");
		f.add( tdb, "grow" );
		f.add( new JLabel("MR"), "center,wrap");

		f.add( new JLabel("BL"), "center");
		f.add( new JLabel("BM"), "center");
		f.add( new JLabel("BR"), "center,wrap");

		f.add( new JLabel("SML"), "center");
		f.add( otherButton, "grow" );
		f.add( new JLabel("SMR"), "center,wrap");

		f.add( new JLabel("BL"), "center");
		f.add( new JLabel("BM"), "center");
		f.add( new JLabel("BR"), "center,wrap");

		f.pack();

		f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		SwingUtilities.invokeLater( new Runnable()
		{

			@Override
			public void run()
			{
				f.setVisible( true );
			}
		} );
	}

	public static void main( final String[] args ) throws Exception
	{
		if( LWTCCtrlTestingConstants.USE_LAF )
		{
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		}
		final TestShowLWTCToggleButton t = new TestShowLWTCToggleButton();
		t.go();
	}

}
