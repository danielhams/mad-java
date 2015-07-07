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
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayController;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel;

public class RotaryDisplayMouseListener implements MouseMotionListener, MouseListener, MouseWheelListener
{
	private final RotaryDisplayKnob knob;
	private final RotaryDisplayModel model;
	private final RotaryDisplayController controller;

	private float startDragValue;
	private int startDragPointY = -1;
	private boolean inDrag = false;
	private final boolean rightClickToReset;

	public RotaryDisplayMouseListener(
			final RotaryDisplayKnob knob,
			final RotaryDisplayModel model,
			final RotaryDisplayController controller,
			final boolean rightClickToReset )
	{
		this.knob = knob;
		this.model = model;
		this.controller = controller;
		this.rightClickToReset = rightClickToReset;
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
		switch( e.getButton() )
		{
			case 1:
			{
				startDragPointY = e.getYOnScreen();
				startDragValue = model.getValue();
				inDrag = true;
				e.consume();
				break;
			}
			case 3:
			{
				if( rightClickToReset )
				{
					final float defaultValue = model.getDefaultValue();
					controller.setValue( this, defaultValue );
					e.consume();
				}
				break;
			}
			default:
			{
				break;
			}
		}
	}

	@Override
	public void mouseReleased( final MouseEvent e )
	{
		switch( e.getButton() )
		{
			case 1:
			{
				if( inDrag )
				{
					inDrag = false;
				}
				break;
			}
			default:
			{
			}
		}
	}

	@Override
	public void mouseDragged( final MouseEvent e )
	{
		if( inDrag )
		{
			final Point curPosition = e.getLocationOnScreen();

			final int yDelta = curPosition.y - startDragPointY;
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
	}

	@Override
	public void mouseMoved( final MouseEvent e )
	{
	}

	@Override
	public void mouseWheelMoved( final MouseWheelEvent e )
	{
		final int wheelRotation = e.getWheelRotation() * -1;
		controller.moveByMajorTick( this, wheelRotation );
		e.consume();
	}

}
