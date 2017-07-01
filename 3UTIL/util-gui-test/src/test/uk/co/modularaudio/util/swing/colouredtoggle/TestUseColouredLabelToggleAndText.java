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

package test.uk.co.modularaudio.util.swing.colouredtoggle;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.swing.colouredtoggle.ColouredLabelToggleAndText;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.toggle.ToggleReceiver;

public class TestUseColouredLabelToggleAndText
{
	private static Log LOG = LogFactory.getLog( TestUseColouredLabelToggleAndText.class.getName() );

	private ColouredLabelToggleAndText cltat;

	public TestUseColouredLabelToggleAndText()
	{
	}

	public void go() throws Exception
	{
		final JFrame testFrame = new JFrame("TestFrame")
		{
			@Override
			public void setBounds( final int x, final int y, final int width, final int height )
			{
				LOG.info("SetBounds");
				super.setBounds( x, y, width, height );
			}
		};
		testFrame.setSize( new Dimension(300, 300) );
		testFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );


		final Container contentPane = testFrame.getContentPane();

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "insets 0" );

		contentPane.setLayout( msh.createMigLayout() );

		final Color surroundColor = Color.WHITE;
//		final Color surroundColor = Color.BLACK;
		final Color backgroundColor = Color.BLACK;
		final Color foregroundColor = Color.white;
//		final Color backgroundColor = Color.WHITE;
//		final Color foregroundColor = Color.BLACK;

		final ToggleReceiver testReceiver = new ToggleReceiver()
		{
			@Override
			public void receiveToggle( final int toggleId, final boolean active )
			{
				LOG.trace("Received a toggle of " + toggleId + " to " + active );
				doAToggle();
			}
		};

		cltat = new ColouredLabelToggleAndText( "Right Front",
				"Tooltip Text",
				backgroundColor,
				foregroundColor,
				surroundColor,
				false,
				testReceiver,
				0,
				"cm",
				50 );
		contentPane.add( cltat, "grow" );

		testFrame.pack();

		testFrame.addWindowListener( new WindowListener()
		{

			@Override
			public void windowOpened( final WindowEvent e ){
				cltat.setLabelText( "20.2" );
			}
			@Override
			public void windowIconified( final WindowEvent e ){}
			@Override
			public void windowDeiconified( final WindowEvent e ){}
			@Override
			public void windowDeactivated( final WindowEvent e ){}
			@Override
			public void windowClosing( final WindowEvent e )
			{
				LOG.trace("Window closing. Value of control is \"" + cltat.getControlValue() + "\"" );
			}
			@Override
			public void windowClosed( final WindowEvent e ){}
			@Override
			public void windowActivated( final WindowEvent e ){}
		} );

		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				LOG.trace( "Showing test frame" );
				testFrame.setVisible( true );
			}
		} );
	}

	private void doAToggle()
	{
		final int randInt = ThreadLocalRandom.current().nextInt( 0, 6 );
		final StringBuilder sb = new StringBuilder();
		for( int i = 0 ; i < randInt ; ++i )
		{
			sb.append( "1" );
		}
		sb.append(".02");
		cltat.setLabelText( sb.toString() );
	}

	public static void main( final String[] args ) throws Exception
	{
		final TestUseColouredLabelToggleAndText t = new TestUseColouredLabelToggleAndText();
		t.go();
		LOG.debug("Going past...");
	}

}
