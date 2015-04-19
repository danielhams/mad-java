package test.uk.co.modularaudio.util.audio.gui.madstdctrls;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.gui.madstdctrls.MadButton;
import uk.co.modularaudio.util.audio.gui.madstdctrls.MadControlConstants;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class TestShowMadButton
{
	private static Log log = LogFactory.getLog( TestShowMadButton.class.getName() );

	private final MadButton tdb;
	private final JButton otherButton;

	public TestShowMadButton()
	{
		tdb = new MadButton( MadControlConstants.STD_BUTTON_COLOURS );
		tdb.setMinimumSize( new Dimension( 75, 30 ) );
		otherButton = new JButton("Kill B");
		otherButton.setMinimumSize( new Dimension( 75,30 ) );
		final Font f = otherButton.getFont();
		log.debug("Regular button font size = " + f.toString() );
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
		if( MadCtrlTestingConstants.USE_LAF )
		{
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		}
		final TestShowMadButton t = new TestShowMadButton();
		t.go();
	}

}
