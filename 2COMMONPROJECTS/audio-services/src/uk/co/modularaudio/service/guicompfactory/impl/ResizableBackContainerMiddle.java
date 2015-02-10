package uk.co.modularaudio.service.guicompfactory.impl;

import java.awt.Component;
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

	private final GuiChannelPlug[] plugs;

	private final RealComponent realComponent;

	private class RealComponent extends JPanel
	{
		private static final long serialVersionUID = 5211955307472576952L;

		public RealComponent( final RackComponent rc )
		{
			this.setOpaque( false );
			this.setLayout( null );

			for( final GuiChannelPlug plug : plugs )
			{
				this.add( plug );
			}
		}

		public GuiChannelPlug getPlugFromPosition( final Point localPoint )
		{
//			log.debug("Looking for plug at real position " + localPoint );
			GuiChannelPlug retVal = null;
			final Component c = this.getComponentAt( localPoint );
			if( c != null )
			{
				if( c instanceof GuiChannelPlug )
				{
					retVal = (GuiChannelPlug)c;
				}
			}
			return retVal;
		}
	}

	public ResizableBackContainerMiddle( final ContainerImages ci, final RackComponent rc, final GuiChannelPlug[] plugs )
	{
		this.setOpaque( false );
		this.backgroundImage = rc.getUiDefinition().getBackBufferedImage();
		this.plugs = plugs;

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
		realComponent = new RealComponent( rc );
		this.add( realComponent, "grow, wrap" );
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
		return realComponent.getPlugFromPosition( localPoint );
	}

}
