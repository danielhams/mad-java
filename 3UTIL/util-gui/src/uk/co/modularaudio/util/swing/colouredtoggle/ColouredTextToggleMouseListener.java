package uk.co.modularaudio.util.swing.colouredtoggle;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ColouredTextToggleMouseListener implements MouseListener
{
	private final ColouredTextToggle originCtt;

	public ColouredTextToggleMouseListener( final ColouredTextToggle originCtt )
	{
		this.originCtt = originCtt;
	}

	@Override
	public void mouseClicked( final MouseEvent e )
	{
		originCtt.receiveClick();
	}

	@Override
	public void mousePressed( final MouseEvent e )
	{
	}

	@Override
	public void mouseReleased( final MouseEvent e )
	{
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
