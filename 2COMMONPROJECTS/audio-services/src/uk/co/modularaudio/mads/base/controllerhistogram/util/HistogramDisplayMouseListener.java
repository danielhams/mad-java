package uk.co.modularaudio.mads.base.controllerhistogram.util;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.ToolTipManager;

public class HistogramDisplayMouseListener implements MouseListener, MouseMotionListener
{
//	private static Log log = LogFactory.getLog( HistogramDisplayMouseListener.class.getName() );

	private final static int NO_BUTTONS_MASK = MouseEvent.BUTTON1_DOWN_MASK |
			MouseEvent.BUTTON2_DOWN_MASK |
			MouseEvent.BUTTON3_DOWN_MASK;

	private final HistogramDisplay display;
	private final HistogramGraph graph;

	private final int defaultTooltipDelay = ToolTipManager.sharedInstance().getInitialDelay();

	public HistogramDisplayMouseListener( final HistogramDisplay display,
			final HistogramGraph graph)
	{
		this.display = display;
		this.graph = graph;
	}

	@Override
	public void mouseDragged( final MouseEvent me )
	{
	}

	@Override
	public void mouseMoved( final MouseEvent me )
	{
		if( (me.getModifiersEx() & NO_BUTTONS_MASK) == 0 )
		{
//			log.debug( "MouseMoved " + me.toString() );
			final int curX = me.getX();
			final int adjustedX = curX - (HistogramDisplay.EVENTS_LABELS_WIDTH + HistogramDisplay.AXIS_MARKER_LENGTH);
			final int curY = me.getY();
			final int adjustedY = curY - (HistogramDisplay.BINS_LABELS_HEIGHT + HistogramDisplay.AXIS_MARKER_LENGTH);
			if( adjustedX >= 0 && adjustedX <= graph.getGraphWidth() &&
					adjustedY >= 0 && adjustedY <= graph.getGraphHeight() )
			{
				display.setMousePosition( adjustedX );
				graph.setMousePosition( adjustedX );
			}
			else
			{
				display.unsetMousePosition();
				graph.unsetMousePosition();
			}
		}
	}

	@Override
	public void mouseClicked( final MouseEvent me )
	{
	}

	@Override
	public void mousePressed( final MouseEvent me )
	{
	}

	@Override
	public void mouseReleased( final MouseEvent me )
	{
	}

	@Override
	public void mouseEntered( final MouseEvent me )
	{
		ToolTipManager.sharedInstance().setInitialDelay( 0 );
//		if( (me.getModifiersEx() & NO_BUTTONS_MASK) == 0 )
//		{
//			log.debug( "MouseEntered " + me.toString() );
//		}
	}

	@Override
	public void mouseExited( final MouseEvent me )
	{
		ToolTipManager.sharedInstance().setInitialDelay( defaultTooltipDelay );
//		if( (me.getModifiersEx() & NO_BUTTONS_MASK) == 0 )
//		{
//			log.debug( "MouseExited " + me.toString() );
//		}
	}
}
