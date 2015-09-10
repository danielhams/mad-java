package test.uk.co.modularaudio.util.swing.colouredlabeltoggle;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.swing.colouredlabeltoggle.ColouredLabelToggle;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class TestUseColouredLabelToggle
{
	private static Log log = LogFactory.getLog( TestUseColouredLabelToggle.class.getName() );

	public TestUseColouredLabelToggle()
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

		final Color surroundColor = Color.decode( "#FFFFFF" );
		final Color backgroundColor = Color.BLACK;
		final Color foregroundColor = Color.white;

		final ColouredLabelToggle clt = new ColouredLabelToggle( "Trigger",
				"Tooltip Text",
				backgroundColor,
				foregroundColor,
				surroundColor,
				false );
		contentPane.add( clt, "grow" );

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
				log.trace("Window closing. Value of control is \"" + clt.getControlValue() + "\"" );
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
		final TestUseColouredLabelToggle t = new TestUseColouredLabelToggle();
		t.go();
		log.debug("Going past...");
	}

}
