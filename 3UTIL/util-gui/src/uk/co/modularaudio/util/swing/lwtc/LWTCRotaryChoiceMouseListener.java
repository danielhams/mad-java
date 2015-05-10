package uk.co.modularaudio.util.swing.lwtc;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class LWTCRotaryChoiceMouseListener implements MouseListener, MouseMotionListener
{
//	private static Log log = LogFactory.getLog( LWTCRotaryChoiceMouseListener.class.getName() );

	private final LWTCRotaryChoice rotaryChoice;

	private int prevFlecheIndex = -1;

	public LWTCRotaryChoiceMouseListener( final LWTCRotaryChoice rotaryChoice )
	{
		this.rotaryChoice = rotaryChoice;
	}

	@Override
	public void mouseDragged( final MouseEvent e )
	{
	}

	@Override
	public void mouseMoved( final MouseEvent e )
	{
		final Point p = e.getPoint();

		final int flecheIndex = rotaryChoice.pointInFleche( p );

		if( flecheIndex != prevFlecheIndex )
		{
			rotaryChoice.setMouseOverFlecheIndex( flecheIndex );

			prevFlecheIndex = flecheIndex;
		}
	}

	@Override
	public void mouseClicked( final MouseEvent e )
	{
	}

	@Override
	public void mousePressed( final MouseEvent e )
	{
		final int onmask = MouseEvent.BUTTON1_DOWN_MASK;
		final int modifiers = e.getModifiersEx();
		if( (modifiers & onmask ) == onmask )
		{
			if( !rotaryChoice.hasFocus() )
			{
				rotaryChoice.grabFocus();
			}

			final Point p = e.getPoint();
			final int flecheIndex = rotaryChoice.pointInFleche( p );
			if( flecheIndex != 0 )
			{
				rotaryChoice.flechePress( flecheIndex );
			}
		}
	}

	@Override
	public void mouseReleased( final MouseEvent e )
	{
		final Point p = e.getPoint();
		final int flecheIndex = rotaryChoice.pointInFleche( p );
		rotaryChoice.flecheRelease( flecheIndex );
	}

	@Override
	public void mouseEntered( final MouseEvent e )
	{
	}

	@Override
	public void mouseExited( final MouseEvent e )
	{
	}
}
