package test.uk.co.modularaudio.util.swing.imagebutton;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.imagebutton.ImageButton;
import uk.co.modularaudio.util.swing.imagebutton.LWTCOptionsButton;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class ImageButtonTester
{
	private final static Log LOG = LogFactory.getLog( ImageButtonTester.class );

	public static void main( final String[] args )
	{
		final JFrame testFrame = new JFrame( "Image Button Test" );
		testFrame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		testFrame.setMinimumSize( new Dimension( 50, 50 ) );
		final Container container = testFrame.getContentPane();

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addLayoutConstraint( "insets 0, gap 0, fill" );
		container.setLayout( msh.createMigLayout() );

		@SuppressWarnings("serial")
		final ImageButton testButton = new LWTCOptionsButton( LWTCControlConstants.STD_BUTTON_COLOURS, true )
		{
			@Override
			public void receiveClick()
			{
				LOG.info("Clicky");
			}
		};

		container.add( testButton, "grow" );

		SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run()
			{
				testFrame.setVisible( true );
			}
		} );
	}

}
