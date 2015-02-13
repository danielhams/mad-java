package uk.co.modularaudio.service.guicompfactory.impl;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.gui.plugs.GuiChannelPlug;
import uk.co.modularaudio.service.guicompfactory.impl.components.ComponentNameLabel;
import uk.co.modularaudio.service.guicompfactory.impl.components.PaintedComponentDefines;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class ResizableBackContainerMiddle extends JPanel
{
	private static final long serialVersionUID = 5700599707006370407L;

	private static Log log = LogFactory.getLog( ResizableBackContainerMiddle.class.getName() );

	private final FixedYTransparentBorder tBorder;
	private final FixedYTransparentBorder bBorder;

	private final ComponentNameLabel componentNameLabel;
	private final BufferedImage backgroundImage;

	private final RealComponentBack realComponentBack;
	private final RackComponent rc;

	public ResizableBackContainerMiddle( final ContainerImages ci, final RealComponentBack realComponentBack,
			final RackComponent rc )
	{
		this.realComponentBack = realComponentBack;
		this.backgroundImage = rc.getUiDefinition().getBackBufferedImage();
		this.rc = rc;

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
			final int imageWidth = backgroundImage.getWidth();
			final int imageHeight = backgroundImage.getHeight();
			final int width = getWidth();
			final int height = getHeight();

			if( imageWidth != width || imageHeight != height )
			{
				final StringBuilder sb = new StringBuilder("Component ");
				sb.append( rc.getInstance().getDefinition().getId() );
				sb.append( " has badly sized back image: (" );
				sb.append( imageWidth );
				sb.append( ", " );
				sb.append( imageHeight );
				sb.append( ") - component size(" );
				sb.append( width );
				sb.append( ", " );
				sb.append( height );
				sb.append( ")" );
				final String msg = sb.toString();
				log.warn( msg );
			}
//			log.debug("Drawing background image of dims " + backgroundImage.getWidth() + " " + backgroundImage.getHeight() );
			g.drawImage( backgroundImage, 0, 0, width, height, null );
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
