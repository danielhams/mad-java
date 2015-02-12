package uk.co.modularaudio.service.guicompfactory.impl;

import java.awt.Dimension;

import javax.swing.JPanel;

import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class ResizableBackContainerLeft extends JPanel
{
	private static final long serialVersionUID = 5700599707006370407L;

//	private static Log log = LogFactory.getLog( ResizableBackContainerMiddle.class.getName() );

	private final FixedSizeTransparentCorner topCorner;
	private final FixedXTransparentBorder lBorder;
	private final FixedSizeTransparentCorner bottomCorner;

	public ResizableBackContainerLeft( final ContainerImages ci )
	{
		this.setOpaque( false );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "inset 0" );
		msh.addLayoutConstraint( "gap 0" );
//		msh.addLayoutConstraint( "debug" );

		msh.addRowConstraint( "[][grow][]" );

		setLayout( msh.createMigLayout() );

		topCorner = new FixedSizeTransparentCorner( ci.ltbi );
		lBorder = new FixedXTransparentBorder( ci.libi );
		bottomCorner = new FixedSizeTransparentCorner( ci.lbbi );

		this.add( topCorner, "wrap" );
		this.add( lBorder, "growy, wrap" );
		this.add( bottomCorner, "" );

		final Dimension size = new Dimension( ci.ltbi.getWidth(), ci.ltbi.getHeight() + ci.lbbi.getHeight() + 1 );
		this.setPreferredSize( size );
	}
}
