/**
 *
 * Copyright (C) 2015 - Daniel Hams, Modular Audio Limited
 *                      daniel.hams@gmail.com
 *
 * Mad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mad.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.co.modularaudio.util.swing.mvc.rotarydisplay;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayController;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel;

public class RotaryDisplayMouseListener implements MouseMotionListener, MouseListener
{
	private final RotaryDisplayKnob knob;
	private final RotaryDisplayModel model;
	private final RotaryDisplayController controller;

	private float startDragValue;
	private Point startDragPoint = new Point();

	public RotaryDisplayMouseListener(
			final RotaryDisplayKnob knob,
			final RotaryDisplayModel model,
			final RotaryDisplayController controller )
	{
		this.knob = knob;
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
		if( !knob.hasFocus() )
		{
			knob.grabFocus();
		}
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

		final float mmaxv = model.getMaxValue();
		final float mminv = model.getMinValue();
		final float scaledOffset = (mmaxv - mminv) * scaledDelta * ySigNum;
		float newValueToSet = startDragValue - scaledOffset;

		if( newValueToSet > mmaxv )
		{
			newValueToSet = mmaxv;
		}
		else if( newValueToSet < mminv )
		{
			newValueToSet = mminv;
		}

		final float currentValue = model.getValue();
		if( currentValue != newValueToSet )
		{
			controller.setValue( this, newValueToSet );
		}
	}

	@Override
	public void mouseMoved( final MouseEvent e )
	{
	}

}
