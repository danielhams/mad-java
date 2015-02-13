package uk.co.modularaudio.service.guicompfactory.impl;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.service.guicompfactory.impl.components.ComponentNameLabel;
import uk.co.modularaudio.service.guicompfactory.impl.components.PaintedComponentDefines;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class ResizableFrontContainerMiddle extends JPanel
{
	private static final long serialVersionUID = 5700599707006370407L;

	private static Log log = LogFactory.getLog( ResizableFrontContainerMiddle.class.getName() );

	private final FixedYTransparentBorder tBorder;
	private final FixedYTransparentBorder bBorder;

	private final ComponentNameLabel componentNameLabel;
	private final BufferedImage backgroundImage;
	private final RackComponent rc;

	public ResizableFrontContainerMiddle( final ContainerImages ci, final RackComponent rc )
	{
		this.setOpaque( false );
		this.backgroundImage = rc.getUiDefinition().getFrontBufferedImage();
		this.rc = rc;

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
		this.add( new RealComponentFront( rc ), "grow, wrap" );
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
				sb.append( " has badly sized front image: (" );
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
}
