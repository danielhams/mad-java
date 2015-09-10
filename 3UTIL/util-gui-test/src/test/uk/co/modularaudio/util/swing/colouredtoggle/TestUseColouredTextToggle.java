package test.uk.co.modularaudio.util.swing.colouredtoggle;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.swing.colouredtoggle.ColouredTextToggle;
import uk.co.modularaudio.util.swing.colouredtoggle.ToggleReceiver;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class TestUseColouredTextToggle
{
	private static Log log = LogFactory.getLog( TestUseColouredTextToggle.class.getName() );

	public TestUseColouredTextToggle()
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

		final Color surroundColor = Color.decode( "#44BB44" );
		final Color backgroundColor = Color.BLACK;

		final ToggleReceiver testReceiver = new ToggleReceiver()
		{

			@Override
			public void receiveToggle( final int toggleId, final boolean active )
			{
				log.trace("Received a toggle of " + toggleId + " to " + active );
			}
		};

		final ColouredTextToggle ctt = new ColouredTextToggle( "NoTS",
				"Tooltip Text",
				backgroundColor,
				surroundColor,
				false,
				testReceiver,
				1 );
		contentPane.add( ctt, "grow" );

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
				log.trace("Window closing. Value of control is \"" + ctt.getControlValue() + "\"" );
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
		final TestUseColouredTextToggle t = new TestUseColouredTextToggle();
		t.go();
		log.debug("Going past...");
	}

}
