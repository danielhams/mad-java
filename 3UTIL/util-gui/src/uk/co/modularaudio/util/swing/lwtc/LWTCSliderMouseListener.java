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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.SwingConstants;

import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;
import uk.co.modularaudio.util.mvc.displayslider.SliderIntToFloatConverter;

public class LWTCSliderMouseListener
	implements MouseListener, MouseMotionListener, MouseWheelListener, FocusListener
{
//	private static Log log = LogFactory.getLog( LWTCSliderMouseListener.class.getName() );

	private final LWTCSlider slider;
	private final int orientation;
	private SliderDisplayModel model;
	private final boolean rightClickToReset;

	private int startCoord;
	private int startModelIntValue;

	private boolean inDrag = false;

	public LWTCSliderMouseListener( final LWTCSlider slider,
			final int orientation,
			final SliderDisplayModel model,
			final boolean rightClickToReset )
	{
		this.slider = slider;
		this.orientation = orientation;
		this.model = model;
		this.rightClickToReset = rightClickToReset;
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
			final SliderIntToFloatConverter sitfc = model.getIntToFloatConverter();
			final float mmaxv = model.getMaxValue();
			final int maxIntValue = sitfc.floatValueToSliderIntValue( model, mmaxv );
			final float mminv = model.getMinValue();
			final int minIntValue = sitfc.floatValueToSliderIntValue( model, mminv );
			final int intRange = maxIntValue - minIntValue;
			final float valuesPerPixel = (float)intRange / pixelsAvailable;
			final int diffInValueSteps = (int)(valuesPerPixel * diffFromStart);
			int attemptedIntValue = (startModelIntValue + diffInValueSteps );

			if( attemptedIntValue > maxIntValue )
			{
				attemptedIntValue = maxIntValue;
			}
			else if( attemptedIntValue < minIntValue )
			{
				attemptedIntValue = minIntValue;
			}

			final float currentValue = model.getValue();
			final int currentIntValue = sitfc.floatValueToSliderIntValue( model, currentValue );
//			log.debug("Drag mouse change from int " + currentIntValue + " to " + attemptedIntValue );
			if( attemptedIntValue != currentIntValue )
			{
				final float attemptedFloatValue = sitfc.sliderIntValueToFloatValue( model, attemptedIntValue );
//				log.debug("Which is " + currentValue + " to " + attemptedFloatValue );
				model.setValue( this, attemptedFloatValue );
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
					final float curValue = model.getValue();
					if( orientation == SwingConstants.HORIZONTAL )
					{
						startCoord = xCoord;
					}
					else
					{
						startCoord = yCoord;
					}
					final SliderIntToFloatConverter sitfc = model.getIntToFloatConverter();
					startModelIntValue = sitfc.floatValueToSliderIntValue( model, curValue );
					inDrag = true;
				}
				else
				{
					// Work out direction and move model
					// by major tick spacing in that direction
					final SliderIntToFloatConverter sitfc = model.getIntToFloatConverter();
					final float curValue = model.getValue();
					final int curIntValue = sitfc.floatValueToSliderIntValue( model, curValue );
					final float minValue = model.getMinValue();
					final int minIntValue = sitfc.floatValueToSliderIntValue( model, minValue );
					final float maxValue = model.getMaxValue();
					final int maxIntValue = sitfc.floatValueToSliderIntValue( model, maxValue );
					final int intRange = maxIntValue - minIntValue;
					final float normValue = ( (curIntValue - minIntValue) / (float)intRange );

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

					model.moveByMajorTick( this, sign );
				}
				me.consume();
				break;
			}
			case 3:
			{
				if( rightClickToReset )
				{
					model.setValue( this, model.getDefaultValue() );
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

		final SliderIntToFloatConverter sitfc = model.getIntToFloatConverter();
		final float curValue = model.getValue();
		final int curIntValue = sitfc.floatValueToSliderIntValue( model, curValue );
		final float minValue = model.getMinValue();
		final int minIntValue = sitfc.floatValueToSliderIntValue( model, minValue );
		final float maxValue = model.getMaxValue();
		final int maxIntValue = sitfc.floatValueToSliderIntValue( model, maxValue );
		final int intRange = maxIntValue - minIntValue;
		final float normValue = (curIntValue - minIntValue) / (float)intRange;

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

	public void setModel( final SliderDisplayModel newModel )
	{
		this.model = newModel;
	}

	@Override
	public void mouseWheelMoved( final MouseWheelEvent e )
	{
		if( slider.hasFocus() )
		{
			// Same for both horizontal and vertical.
			final int direction = e.getWheelRotation() * -1;
			model.moveByMajorTick( this, direction );
			e.consume();
		}
	}

	@Override
	public void focusGained( final FocusEvent e )
	{
		slider.addMouseWheelListener( this );
	}

	@Override
	public void focusLost( final FocusEvent e )
	{
		slider.removeMouseWheelListener( this );
	}
}
