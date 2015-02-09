package uk.co.modularaudio.service.guicompfactory.impl.memreduce;

import java.awt.Point;
import java.awt.Rectangle;

import uk.co.modularaudio.service.gui.plugs.GuiChannelPlug;
import uk.co.modularaudio.service.guicompfactory.AbstractGuiAudioComponent;
import uk.co.modularaudio.service.guicompfactory.impl.components.PaintedComponentDefines;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.mad.MadChannelInstance;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class ResizableFrontContainer extends AbstractGuiAudioComponent
{
	private static final long serialVersionUID = 5369121173959977190L;

//	private static Log log = LogFactory.getLog( ResizableFrontContainer.class.getName() );

	private final FixedSizeTransparentCorner ltCorner;
	private final FixedXTransparentBorder lBorder;
	private final FixedSizeTransparentCorner lbCorner;

	private final FixedSizeTransparentCorner rtCorner;
	private final FixedXTransparentBorder rBorder;
	private final FixedSizeTransparentCorner rbCorner;

	private final ResizableFrontContainerMiddle containerMiddle;


	private final Rectangle renderedRectangle;


	public ResizableFrontContainer( final ContainerImages frontImages, final RackComponent rc )
	{
		super( rc );
		this.setOpaque( false );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "insets 0 2 0 2" );
		msh.addLayoutConstraint( "gap 0" );
//		msh.addLayoutConstraint( "debug" );

		msh.addColumnConstraint( "[][grow][]" );
		msh.addRowConstraint( "[][grow][]" );

		setLayout( msh.createMigLayout() );

		ltCorner = new FixedSizeTransparentCorner( frontImages.ltbi );
		lBorder = new FixedXTransparentBorder( frontImages.libi );
		lbCorner = new FixedSizeTransparentCorner( frontImages.lbbi );

		rtCorner = new FixedSizeTransparentCorner( frontImages.rtbi );
		rBorder = new FixedXTransparentBorder( frontImages.ribi );
		rbCorner = new FixedSizeTransparentCorner( frontImages.rbbi );

		containerMiddle = new ResizableFrontContainerMiddle( frontImages, rc );

		this.add( ltCorner, "" );

		this.add( containerMiddle, "grow, spany 3" );

		this.add( rtCorner, "wrap");
		this.add( lBorder, "growy" );

		this.add( rBorder, "growy, wrap" );
		this.add( lbCorner, "" );
		this.add( rbCorner );

		this.renderedRectangle = new Rectangle( 20, 20 );
	}


	@Override
	public boolean isPointLocalDragRegion( final Point localPoint )
	{
		if( !rackComponent.isDraggable() )
		{
			return false;
		}
		// Only allow dragging in the first/last 10 pixels of the component width
		final int curWidth = this.getWidth();
		final int mouseXPos = localPoint.x;
//		int mouseYPos = localPoint.y;
		if( (mouseXPos > PaintedComponentDefines.INSET && mouseXPos < PaintedComponentDefines.INSET + PaintedComponentDefines.DRAG_BAR_WIDTH) ||
					(mouseXPos < curWidth - PaintedComponentDefines.INSET && mouseXPos > curWidth - PaintedComponentDefines.DRAG_BAR_WIDTH ) )
		{
			return true;
		}
		else
		{
			return false;
		}
	}


	@Override
	public void destroy()
	{
	}


	@Override
	public Rectangle getRenderedRectangle()
	{
		return renderedRectangle;
	}


	@Override
	public GuiChannelPlug getPlugFromPosition( final Point localPoint )
	{
		return null;
	}


	@Override
	public GuiChannelPlug getPlugFromMadChannelInstance( final MadChannelInstance auChannelInstance )
	{
		return null;
	}
}
