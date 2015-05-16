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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class LWTCSlider extends JPanel
{
	private static final long serialVersionUID = 5316903589300941235L;

//	private static Log log = LogFactory.getLog( LWTCSlider.class.getName() );

	private final int orientation;
	protected BoundedRangeModel model;
	private int majorTickSpacing;

	private LWTCSliderPainter painter;

	private final LWTCSliderMouseListener mouseListener;
	private final LWTCSliderKeyListener keyListener;
	private final ValueChangeListener valueChangeListener;

	private int numUsablePixels;

	private boolean myHasFocus = false;

	private class ValueChangeListener implements ChangeListener
	{
		private BoundedRangeModel model;
		private int lastValueReceived;

		public ValueChangeListener( final BoundedRangeModel model )
		{
			this.model = model;
			lastValueReceived = model.getValue();
			model.addChangeListener( this );
		}

		@Override
		public void stateChanged( final ChangeEvent ce )
		{
			final int newValue = model.getValue();
			modelValueChangeDeltaRepaint( lastValueReceived, newValue );
			lastValueReceived = newValue;
		}

		public void setModel( final BoundedRangeModel newModel )
		{
			model.removeChangeListener( this );
			lastValueReceived = newModel.getValue();
			model = newModel;
			model.addChangeListener( this );
		}
	};

	public LWTCSlider( final boolean opaque )
	{
		this( SwingConstants.HORIZONTAL, new DefaultBoundedRangeModel( 50, 0, 0, 100 ),
				LWTCControlConstants.STD_SLIDER_COLOURS,
				opaque);
	}

	public LWTCSlider( final int orientation, final boolean opaque )
	{
		this( orientation, new DefaultBoundedRangeModel( 50, 0, 0, 100 ),
				LWTCControlConstants.STD_SLIDER_COLOURS,
				opaque );
	}

	public LWTCSlider( final int orientation, final BoundedRangeModel model, final boolean opaque )
	{
		this( orientation, model, LWTCControlConstants.STD_SLIDER_COLOURS, opaque );
	}

	public LWTCSlider( final int orientation,
			final BoundedRangeModel model,
			final LWTCSliderColours sliderColours,
			final boolean opaque )
	{
		setUI( LWTCLookAndFeelHelper.getInstance().getComponentUi( this ) );
		this.setOpaque( opaque );

		this.orientation = orientation;
		this.model = model;
		this.majorTickSpacing = 10;

		setSliderColours( sliderColours );

		this.setMinimumSize( orientation == SwingConstants.HORIZONTAL ?
				LWTCSliderKnobImage.H_SLIDER_MIN_SIZE :
				LWTCSliderKnobImage.V_SLIDER_MIN_SIZE );

		keyListener = new LWTCSliderKeyListener( model, this );
		this.addKeyListener( keyListener );

		this.setFocusable( true );

		mouseListener = new LWTCSliderMouseListener( this, orientation, model );

		this.addMouseListener( mouseListener );
		this.addMouseMotionListener( mouseListener );

		valueChangeListener = new ValueChangeListener( model );

		this.addFocusListener( new FocusListener()
		{

			@Override
			public void focusLost( final FocusEvent e )
			{
				repaint();
				myHasFocus = false;
			}

			@Override
			public void focusGained( final FocusEvent e )
			{
				repaint();
				myHasFocus = true;
			}
		});
	}

	public void setSliderColours( final LWTCSliderColours sliderColours )
	{
		this.painter = new LWTCSliderPainter( sliderColours );
	}

	@Override
	public void paintComponent( final Graphics g )
	{
		super.paintComponent( g );
		final Graphics2D g2d = (Graphics2D)g;
		final int width = getWidth();
		final int height = getHeight();

		if( myHasFocus )
		{
			g2d.setColor( LWTCControlConstants.STD_SLIDER_COLOURS.getFocus() );
			if( orientation == SwingConstants.HORIZONTAL )
			{
				final int boxHeight = LWTCSliderKnobImage.H_KNOB_HEIGHT + 4;
				final int yStartOffset = (height - boxHeight) / 2;
				g2d.drawRect( 0, yStartOffset, width - 1, boxHeight );
			}
			else
			{
				final int boxWidth = LWTCSliderKnobImage.V_KNOB_WIDTH + 4;
				final int xStartOffset = (width - boxWidth) / 2;
				g2d.drawRect( xStartOffset, 0, boxWidth, height - 1 );
			}
		}
		painter.paintSlider( g2d,
				orientation,
				width,
				height,
				model );

	}

	public int getMajorTickSpacing()
	{
		return majorTickSpacing;
	}

	public void setMajorTickSpacing( final int majorTickSpacing )
	{
		this.majorTickSpacing = majorTickSpacing;
	}

	private final void modelValueChangeDeltaRepaint( final int prevValue, final int newValue )
	{
//		log.debug("modelValueChangeDeltaRepaint(" + prevValue + ", " + newValue +")");

		final int width = getWidth();
		final int height = getHeight();

		final int prevStartPos = modelValueToSliderStart( prevValue );
		final int newStartPos = modelValueToSliderStart( newValue );

		int minPos, maxPos;

		if( prevStartPos < newStartPos )
		{
			minPos = prevStartPos;
			maxPos = newStartPos;
		}
		else
		{
			minPos = newStartPos;
			maxPos = prevStartPos;
		}
		int rangeStart, rangeEnd;
		if( orientation == SwingConstants.HORIZONTAL )
		{
			rangeStart = minPos;
			rangeEnd = maxPos + LWTCSliderKnobImage.H_KNOB_WIDTH;
			final int xBegin = 3 + rangeStart;
			final int yBegin = (height - LWTCSliderKnobImage.H_KNOB_HEIGHT) / 2;
			final int rpWidth = (rangeEnd - rangeStart) + 1;
			final int rpHeight = LWTCSliderKnobImage.H_KNOB_HEIGHT + 1;
			repaint( xBegin, yBegin, rpWidth, rpHeight );
//			log.debug("Deltarepaint(" + xBegin + ", " + yBegin + ")-(" + rpWidth + ", " +
//					rpHeight + ")");
		}
		else
		{
			rangeStart = minPos;
			rangeEnd = maxPos + LWTCSliderKnobImage.V_KNOB_HEIGHT;
			final int xBegin = (width - LWTCSliderKnobImage.V_KNOB_WIDTH) / 2;
			final int yBegin = (height - maxPos) - LWTCSliderKnobImage.V_KNOB_HEIGHT - 3;
			final int rpWidth = LWTCSliderKnobImage.V_KNOB_WIDTH + 1;
			final int rpHeight = (maxPos - minPos) + LWTCSliderKnobImage.V_KNOB_HEIGHT + 1;
			repaint( xBegin, yBegin, rpWidth, rpHeight );
//			log.debug("Deltarepaint(" + xBegin + ", " + yBegin + ")-(" + rpWidth + ", " +
//					rpHeight + ")");
		}
	}

	private final int modelValueToSliderStart( final int modelValue )
	{
		final int min = model.getMinimum();
		final int max = model.getMaximum();
		final int range = max - min;
		final float normalisedValue = (modelValue-min) / (float)range;

		return (int)(normalisedValue * numUsablePixels);
	}

	@Override
	public void setBounds( final int x, final int y, final int width, final int height )
	{
		super.setBounds( x, y, width, height );

		if( orientation == SwingConstants.HORIZONTAL )
		{
			numUsablePixels = width - (2*3) - LWTCSliderKnobImage.H_KNOB_WIDTH;
		}
		else
		{
			numUsablePixels = height - (2*3) - LWTCSliderKnobImage.V_KNOB_HEIGHT;
		}
	}

	public void setValue( final int value )
	{
		model.setValue( value );
	}

	public void setModel( final BoundedRangeModel newModel )
	{
		valueChangeListener.setModel( newModel );
		mouseListener.setModel( newModel );
		keyListener.setModel( newModel );
		this.model = newModel;
	}
}
