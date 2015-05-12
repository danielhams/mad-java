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

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCToggleButton;
import uk.co.modularaudio.util.swing.lwtc.LWTCToggleGroup;

public class TestShowLWTCToggleButtonGroup
{
	private static Log log = LogFactory.getLog( TestShowLWTCToggleButtonGroup.class.getName() );

	private final LWTCToggleGroup tg;

	public TestShowLWTCToggleButtonGroup()
	{
		final String[] buttonLabels = new String[] {
				"Item 1",
				"Item 2",
				"Item 3"
		};
		tg = new LWTCToggleGroup( LWTCControlConstants.STD_TOGGLE_BUTTON_COLOURS,
				buttonLabels, 1, false )
		{

			@Override
			public void receiveUpdateEvent( final int previousSelection, final int newSelection )
			{
				log.debug("Received update event:  " + newSelection );
			}
		};
	}

	public void go() throws Exception
	{

		final JFrame f = new JFrame();
		final MigLayoutStringHelper msg = new MigLayoutStringHelper();
		msg.addLayoutConstraint( "fill" );
		msg.addLayoutConstraint( "insets 0" );
		msg.addLayoutConstraint( "gap 0" );
		msg.addColumnConstraint( "[][grow][]" );
//		msg.addRowConstraint( "[][grow][][grow][]" );
		f.setLayout( msg.createMigLayout() );

		f.add( new JLabel("TL"), "center, grow 0");
		f.add( new JLabel("TM"), "center, growy 0");
		f.add( new JLabel("TR"), "center, grow 0, wrap");

		final LWTCToggleButton[] tbs = tg.getToggleButtons();
		for( int i = 0 ; i < tbs.length ; ++i )
		{
			final LWTCToggleButton tb = tbs[i];

			f.add( new JLabel("ML"), "center");
			f.add( tb, "grow" );
			f.add( new JLabel("MR"), "center,wrap");
		}

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
		final TestShowLWTCToggleButtonGroup t = new TestShowLWTCToggleButtonGroup();
		t.go();
	}

}
