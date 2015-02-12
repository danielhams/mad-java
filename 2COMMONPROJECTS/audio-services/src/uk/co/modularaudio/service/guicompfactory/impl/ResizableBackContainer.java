package uk.co.modularaudio.service.guicompfactory.impl;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.service.gui.plugs.GuiAudioChannelPlug;
import uk.co.modularaudio.service.gui.plugs.GuiCVChannelPlug;
import uk.co.modularaudio.service.gui.plugs.GuiChannelPlug;
import uk.co.modularaudio.service.gui.plugs.GuiNoteChannelPlug;
import uk.co.modularaudio.service.guicompfactory.AbstractGuiAudioComponent;
import uk.co.modularaudio.service.guicompfactory.impl.components.PaintedComponentDefines;
import uk.co.modularaudio.util.audio.gui.mad.MadUiChannelInstance;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.mad.MadChannelDefinition;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class ResizableBackContainer extends AbstractGuiAudioComponent
{
	private static final long serialVersionUID = 5369121173959977190L;

//	private static Log log = LogFactory.getLog( ResizableFrontContainer.class.getName() );

	private final FixedSizeTransparentCorner ltCorner;
	private final FixedXTransparentBorder lBorder;
	private final FixedSizeTransparentCorner lbCorner;

	private final FixedSizeTransparentCorner rtCorner;
	private final FixedXTransparentBorder rBorder;
	private final FixedSizeTransparentCorner rbCorner;

	private final ResizableBackContainerMiddle containerMiddle;


	private final Rectangle renderedRectangle;

	private final GuiChannelPlug[] plugsToDestroy;

	public ResizableBackContainer( final ContainerImages backImages, final RackComponent rc )
	{
		super( rc );
		this.setOpaque( false );

		final MadUiChannelInstance[] chanDefs = rc.getUiChannelInstances();
		plugsToDestroy = new GuiChannelPlug[ chanDefs.length ];
		for( int i = 0 ; i < chanDefs.length ; i++ )
		{
			final MadUiChannelInstance cd = chanDefs[ i ];
			GuiChannelPlug plug = null;
			final MadChannelDefinition channelDefinition = cd.getChannelInstance().definition;
			switch( channelDefinition.type )
			{
				case AUDIO:
				{
					plug = new GuiAudioChannelPlug( cd );
					break;
				}
				case CV:
				{
					plug = new GuiCVChannelPlug( cd );
					break;
				}
				case NOTE:
				{
					plug = new GuiNoteChannelPlug( cd );
					break;
				}
			}
			plugsToDestroy[ i ] = plug;
		}

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "insets 0 20 0 20" );
		msh.addLayoutConstraint( "gap 0" );
//		msh.addLayoutConstraint( "debug" );

		msh.addColumnConstraint( "[][grow][]" );
		msh.addRowConstraint( "[][grow][]" );

		setLayout( msh.createMigLayout() );

		ltCorner = new FixedSizeTransparentCorner( backImages.ltbi );
		lBorder = new FixedXTransparentBorder( backImages.libi );
		lbCorner = new FixedSizeTransparentCorner( backImages.lbbi );

		rtCorner = new FixedSizeTransparentCorner( backImages.rtbi );
		rBorder = new FixedXTransparentBorder( backImages.ribi );
		rbCorner = new FixedSizeTransparentCorner( backImages.rbbi );

		containerMiddle = new ResizableBackContainerMiddle( backImages, rc, plugsToDestroy );

		this.add( ltCorner, "grow 0" );

		this.add( containerMiddle, "grow, spany 3" );

		this.add( rtCorner, "grow 0, wrap");
		this.add( lBorder, "growy" );

		this.add( rBorder, "growy, wrap" );
		this.add( lbCorner, "grow 0" );
		this.add( rbCorner, "grow 0" );

		this.renderedRectangle = new Rectangle( PaintedComponentDefines.DRAG_BAR_WIDTH +
					PaintedComponentDefines.BACK_INSET_WIDTH,
				PaintedComponentDefines.BACK_BOTTOM_TOP_INSET,
				getWidth(),
				getHeight() );
//		log.debug("Set rendered rectangle to " + renderedRectangle.toString() );
	}


	@Override
	public boolean isPointLocalDragRegion( final Point localPoint )
	{
		if( !rackComponent.isDraggable() )
		{
			return false;
		}
		// Make sure it doesn't fall in the HORIZON_INSET region
		final int x = localPoint.x;
		return ( x >= PaintedComponentDefines.DRAG_BAR_WIDTH && x < this.getWidth() - PaintedComponentDefines.DRAG_BAR_WIDTH );
	}


	@Override
	public void destroy()
	{
		this.removeAll();
		for( int i = 0 ; i < plugsToDestroy.length ; i++ )
		{
			plugsToDestroy[ i ].destroy();
			plugsToDestroy[ i ] = null;
		}
	}


	@Override
	public Rectangle getRenderedRectangle()
	{
		return renderedRectangle;
	}


	@Override
	public GuiChannelPlug getPlugFromPosition( final Point localPoint )
	{
//		log.debug("Asked for plug at position " + localPoint );
		localPoint.x -= renderedRectangle.x;
		localPoint.y -= renderedRectangle.y;
		return containerMiddle.getPlugFromPosition( localPoint );
	}


	@Override
	public GuiChannelPlug getPlugFromMadChannelInstance( final MadChannelInstance auChannelInstance )
	{
		for( final GuiChannelPlug plug : plugsToDestroy )
		{
			if( plug.getUiChannelInstance().getChannelInstance() == auChannelInstance )
			{
				return plug;
			}
		}
		return null;
	}
}
