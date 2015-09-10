package uk.co.modularaudio.util.swing.colouredlabeltoggle;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ColouredLabelToggleMouseListener implements MouseListener
{
	private final ColouredLabelToggle originCtt;

	public ColouredLabelToggleMouseListener( final ColouredLabelToggle originCtt )
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
