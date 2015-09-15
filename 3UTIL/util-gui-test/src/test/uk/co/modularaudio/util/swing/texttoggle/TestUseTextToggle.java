package test.uk.co.modularaudio.util.swing.texttoggle;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.texttoggle.TextToggle;
import uk.co.modularaudio.util.swing.toggle.ToggleReceiver;

public class TestUseTextToggle
{
	private static Log log = LogFactory.getLog( TestUseTextToggle.class.getName() );

	public TestUseTextToggle()
	{
	}

	public void go() throws Exception
	{
		final JFrame testFrame = new JFrame("TestFrame");
		testFrame.setSize( new Dimension(300, 300) );
		testFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );


		final Container contentPane = testFrame.getContentPane();

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "insets 0" );

		contentPane.setLayout( msh.createMigLayout() );

		final Color BACKGROUND_COLOR = Color.BLACK;
		final Color SCOPE_BODY = new Color( 75, 131, 155 );
		final Color SCOPE_AXIS_DETAIL = SCOPE_BODY.darker().darker();

		final Color selectedTextColor = SCOPE_BODY;
		final Color unselectedTextColor = SCOPE_AXIS_DETAIL;
		final Color borderColor = SCOPE_AXIS_DETAIL;
		final Color backgroundColor = BACKGROUND_COLOR;

		final ToggleReceiver testReceiver = new ToggleReceiver()
		{

			@Override
			public void receiveToggle( final int toggleId, final boolean active )
			{
				log.trace("Received a toggle of " + toggleId + " to " + active );
			}
		};

		final TextToggle tt = new TextToggle( "Bi Polar",
				"Uni Polar",
				selectedTextColor,
				unselectedTextColor,
				backgroundColor,
				borderColor,
				true,
				true,
				testReceiver,
				-1 );

		contentPane.add( tt, "grow" );

		testFrame.pack();

		testFrame.addWindowListener( new WindowListener()
		{

			@Override
			public void windowOpened( final WindowEvent e ){}
			@Override
			public void windowIconified( final WindowEvent e ){}
			@Override
			public void windowDeiconified( final WindowEvent e ){}
			@Override
			public void windowDeactivated( final WindowEvent e ){}
			@Override
			public void windowClosing( final WindowEvent e )
			{
				log.trace("Window closing. Value of control is \"" + tt.getControlValue() + "\"" );
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
				log.trace( "Showing test frame" );
				testFrame.setVisible( true );
			}
		} );
	}

	public static void main( final String[] args ) throws Exception
	{
		final TestUseTextToggle t = new TestUseTextToggle();
		t.go();
		log.debug("Going past...");
	}

}
