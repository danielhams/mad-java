package uk.co.modularaudio.service.guicompfactory.impl;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import uk.co.modularaudio.service.gui.plugs.GuiChannelPlug;
import uk.co.modularaudio.service.guicompfactory.impl.components.ComponentNameLabel;
import uk.co.modularaudio.service.guicompfactory.impl.components.PaintedComponentDefines;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class ResizableBackContainerMiddle extends JPanel
{
	private static final long serialVersionUID = 5700599707006370407L;

//	private static Log log = LogFactory.getLog( ResizableBackContainerMiddle.class.getName() );

	private final FixedYTransparentBorder tBorder;
	private final FixedYTransparentBorder bBorder;

	private final ComponentNameLabel componentNameLabel;
	private final BufferedImage backgroundImage;

	private final RealComponentBack realComponentBack;

	public ResizableBackContainerMiddle( final ContainerImages ci, final RealComponentBack realComponentBack,
			final RackComponent rc )
	{
		this.realComponentBack = realComponentBack;
		this.backgroundImage = rc.getUiDefinition().getBackBufferedImage();

		this.setOpaque( false );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "inset 0" );
		msh.addLayoutConstraint( "gap 0" );
//		msh.addLayoutConstraint( "debug" );

		msh.addRowConstraint( "[][grow][]" );

		setLayout( msh.createMigLayout() );

		tBorder = new FixedYTransparentBorder( ci.tibi );
		bBorder = new FixedYTransparentBorder( ci.bibi );

		this.add( tBorder, "growx, wrap" );
		this.add( realComponentBack, "grow, wrap" );
		this.add( bBorder, "growx" );

		componentNameLabel = new ComponentNameLabel( rc, this );
	}

	@Override
	public void paint( final Graphics g )
	{
//		final Rectangle paintRect = g.getClipBounds();
//		log.debug( "Painting rect " + paintRect.toString() );
		if( backgroundImage != null )
		{
//			log.debug("Drawing background image of dims " + backgroundImage.getWidth() + " " + backgroundImage.getHeight() );
			g.drawImage( backgroundImage, 0, 0, getWidth(), getHeight(), null );
		}
		super.paint( g );
		g.translate( 0, PaintedComponentDefines.FRONT_BOTTOM_TOP_INSET );
		componentNameLabel.paint( g );
	}

	public GuiChannelPlug getPlugFromPosition( final Point localPoint )
	{
//		log.debug("Asked for plug at position " + localPoint );
		return realComponentBack.getPlugFromPosition( localPoint );
	}

}
