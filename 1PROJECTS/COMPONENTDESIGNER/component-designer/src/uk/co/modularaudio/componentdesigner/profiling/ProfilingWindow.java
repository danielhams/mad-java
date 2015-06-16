package uk.co.modularaudio.componentdesigner.profiling;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class ProfilingWindow extends JFrame
{
	private static final long serialVersionUID = 5841688948040826459L;

	private static final Dimension MIN_SIZE = new Dimension( 320, 256 );

	private final ProfilingPanel pp;

	public ProfilingWindow( final ComponentDesignerFrontController fc )
	{
		super.setTitle( "Audio Job Profiling" );

		setMinimumSize( MIN_SIZE );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "insets 15");
		msh.addLayoutConstraint( "gap 5");
		msh.addLayoutConstraint( "fill" );

		msh.addRowConstraint( "[][grow]" );

		setLayout( msh.createMigLayout() );

		final JButton refreshButton = new JButton( "Refresh" );

		pp = new ProfilingPanel( fc );

		refreshButton.addActionListener( new ActionListener()
		{

			@Override
			public void actionPerformed( final ActionEvent e )
			{
				pp.refresh();
			}
		} );

		add( refreshButton, "wrap" );

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		add( pp, "grow" );
	}

}
