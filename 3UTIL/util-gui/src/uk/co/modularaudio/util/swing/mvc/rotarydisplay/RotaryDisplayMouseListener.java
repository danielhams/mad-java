package uk.co.modularaudio.util.swing.mvc.rotarydisplay;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayController;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel;

public class RotaryDisplayMouseListener implements MouseMotionListener, MouseListener
{

	private final RotaryDisplayModel model;
	private final RotaryDisplayController controller;

	private float startDragValue;
	private Point startDragPoint = new Point();

	public RotaryDisplayMouseListener( final RotaryDisplayModel model, final RotaryDisplayController controller )
	{
		this.model = model;
		this.controller = controller;
	}

	@Override
	public void mouseClicked( final MouseEvent arg0 )
	{
	}

	@Override
	public void mouseEntered( final MouseEvent arg0 )
	{
	}

	@Override
	public void mouseExited( final MouseEvent arg0 )
	{
	}

	@Override
	public void mousePressed( final MouseEvent e )
	{
		final Point screenPoint = e.getLocationOnScreen();
		startDragPoint = screenPoint;
		startDragValue = model.getValue();
	}

	@Override
	public void mouseReleased( final MouseEvent arg0 )
	{
		startDragPoint = null;
	}

	@Override
	public void mouseDragged( final MouseEvent e )
	{
		final Point curPosition = e.getLocationOnScreen();

		final int yDelta = curPosition.y - startDragPoint.y;
		final int yAbsDelta = Math.abs( yDelta );
		final int ySigNum = (int)Math.signum( yDelta );

		// Scale it so 100 pixels difference = max diff
		float scaledDelta = (yAbsDelta / 100.0f);
		scaledDelta = (scaledDelta > 1.0f ? 1.0f : scaledDelta );

		final float scaledOffset = (model.getMaxValue() - model.getMinValue()) * scaledDelta * ySigNum;
		final float newValueToSet = startDragValue - scaledOffset;

		controller.setValue( this, newValueToSet );

	}

	@Override
	public void mouseMoved( final MouseEvent e )
	{
	}

}
