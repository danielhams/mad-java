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

package uk.co.modularaudio.util.swing.lwtc;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.BoundedRangeModel;
import javax.swing.SwingConstants;

public class LWTCSliderMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener
{
//	private static Log log = LogFactory.getLog( LWTCSliderMouseListener.class.getName() );

	private final LWTCSlider slider;
	private final int orientation;
	private BoundedRangeModel model;

	private int startCoord;
	private int startModelValue;

	private boolean inDrag = false;

	public LWTCSliderMouseListener( final LWTCSlider slider,
			final int orientation,
			final BoundedRangeModel model )
	{
		this.slider = slider;
		this.orientation = orientation;
		this.model = model;
	}

	@Override
	public void mouseDragged( final MouseEvent me )
	{
		if( inDrag )
		{
			int curCoord;
			int pixelsAvailable;
			int diffFromStart;
			switch( orientation )
			{
				case SwingConstants.HORIZONTAL:
				{
					curCoord = me.getX();
					pixelsAvailable = slider.getWidth() - 6 - LWTCSliderKnobImage.H_KNOB_WIDTH;
					diffFromStart = curCoord - startCoord;
					break;
				}
				default:
				{
					curCoord = me.getY();
					pixelsAvailable = slider.getHeight() - 6 - LWTCSliderKnobImage.V_KNOB_HEIGHT;
					diffFromStart = startCoord - curCoord;
					break;
				}
			}
			final int mmaxv = model.getMaximum();
			final int mminv = model.getMinimum();
			final int range = mmaxv - mminv;
			final float valuesPerPixel = range / (float)pixelsAvailable;
			final float diffInValueSteps = valuesPerPixel * diffFromStart;
			int attemptedValue = (int)(startModelValue + diffInValueSteps );

			if( attemptedValue > mmaxv )
			{
				attemptedValue = mmaxv;
			}
			else if( attemptedValue < mminv )
			{
				attemptedValue = mminv;
			}

			final int currentValue = model.getValue();
			if( attemptedValue != currentValue )
			{
				model.setValue( attemptedValue );
			}
			me.consume();
		}
	}

	@Override
	public void mouseMoved( final MouseEvent me )
	{
		// Do nothing
	}

	@Override
	public void mouseClicked( final MouseEvent me )
	{
		// Do nothing
	}

	@Override
	public void mouseEntered( final MouseEvent me )
	{
		// Do nothing
	}

	@Override
	public void mouseExited( final MouseEvent me )
	{
		// Do nothing
	}

	@Override
	public void mousePressed( final MouseEvent me )
	{
		if( !slider.hasFocus() )
		{
			slider.grabFocus();
		}

		switch( me.getButton() )
		{
			case 1:
			{
				final int xCoord = me.getX();
				final int yCoord = me.getY();
				if( mouseInKnob( xCoord, yCoord ) )
				{
					final int curValue = model.getValue();
					if( orientation == SwingConstants.HORIZONTAL )
					{
						startCoord = xCoord;
					}
					else
					{
						startCoord = yCoord;
					}
					startModelValue = curValue;
					inDrag = true;
				}
				else
				{
					// Work out direction and move model
					// by major tick spacing in that direction
					final int curValue = model.getValue();
					final int range = model.getMaximum() - model.getMinimum();
					final float normValue = ( (curValue - model.getMinimum()) / (float)range );

					float normClick;
					if( orientation == SwingConstants.HORIZONTAL )
					{
						normClick = (xCoord - 3) / (float)(slider.getWidth() - 6);
					}
					else
					{
						normClick = 1.0f - ((yCoord - 3) / (float)(slider.getHeight() - 6));
					}
					final int sign = (int)Math.signum( normClick - normValue );

					final int numToAdd = sign * slider.getMajorTickSpacing();

					final int prevValue = model.getValue();
					final int attemptedValue = prevValue + numToAdd;
					model.setValue( attemptedValue );
				}
				me.consume();
				break;
			}
			default:
			{
				break;
			}
		}
	}

	private boolean mouseInKnob( final int x, final int y )
	{
		boolean matchesX;
		boolean matchesY;

		final int sliderWidth = slider.getWidth();
		final int sliderHeight = slider.getHeight();

		final int curValue = model.getValue();
		final int range = model.getMaximum() - model.getMinimum();
		final float normValue = ( (curValue - model.getMinimum()) / (float)range );

		switch( orientation )
		{
			case SwingConstants.HORIZONTAL:
			{
				final int knobWidthOver2 = LWTCSliderKnobImage.H_KNOB_WIDTH/2;
				final int knobHeightOver2 = LWTCSliderKnobImage.H_KNOB_HEIGHT/2;
				final int halfHeight = sliderHeight / 2;
				matchesY = ( y > (halfHeight - knobHeightOver2) &&
						y < (halfHeight + knobHeightOver2 ) );

				final int pixelsAvailable = sliderWidth - 6 - LWTCSliderKnobImage.H_KNOB_WIDTH;
				final int knobCenterPos = (int)(3 + (normValue * pixelsAvailable)) + knobWidthOver2;
				matchesX = ( x > (knobCenterPos - knobWidthOver2) &&
						x < (knobCenterPos + knobWidthOver2) );

				if( matchesY && matchesX )
				{
					return true;
				}
				break;
			}
			default:
			{
				final int knobWidthOver2 = LWTCSliderKnobImage.V_KNOB_WIDTH/2;
				final int knobHeightOver2 = LWTCSliderKnobImage.V_KNOB_HEIGHT/2;
				final int halfWidth = sliderWidth / 2;
				matchesX = ( x > (halfWidth - knobWidthOver2) &&
						x < (halfWidth + knobWidthOver2 ) );

				final int pixelsAvailable = sliderHeight - 6 - LWTCSliderKnobImage.V_KNOB_HEIGHT;
				final int knobCenterPos = (int)(3 + (normValue * pixelsAvailable)) + knobHeightOver2;
				final int inversePos = sliderHeight - y;
				matchesY = ( inversePos > (knobCenterPos - knobHeightOver2) &&
						inversePos < (knobCenterPos + knobHeightOver2) );

				if( matchesY && matchesX )
				{
					return true;
				}
				break;
			}
		}

		return false;
	}

	@Override
	public void mouseReleased( final MouseEvent me )
	{
		if( me.getButton() == 1 )
		{
			if( inDrag )
			{
				inDrag = false;
			}
			me.consume();
		}
	}

	public void setModel( final BoundedRangeModel newModel )
	{
		this.model = newModel;
	}

	@Override
	public void mouseWheelMoved( final MouseWheelEvent e )
	{
		// Same for both horizontal and vertical.
		final int numToAdd = e.getWheelRotation() * slider.getMajorTickSpacing() * -1;

		final int prevValue = model.getValue();
		final int attemptedValue = prevValue + numToAdd;
		model.setValue( attemptedValue );
		e.consume();
	}
}
