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

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LWTCSlider extends JPanel
{
	private static final long serialVersionUID = 5316903589300941235L;

	private static Log log = LogFactory.getLog( LWTCSlider.class.getName() );

	private final int orientation;
	private final BoundedRangeModel model;
	private final int majorTickSpacing;

	private final LWTCSliderPainter painter;

	private final LWTCSliderMouseListener mouseListener;

	public LWTCSlider()
	{
		this( SwingConstants.HORIZONTAL, new DefaultBoundedRangeModel( 50, 0, 0, 100 ) );
	}

	public LWTCSlider( final int orientation )
	{
		this( orientation, new DefaultBoundedRangeModel( 50, 0, 0, 100 ) );
	}

	public LWTCSlider( final int orientation, final BoundedRangeModel model )
	{
		this.setOpaque( false );

		this.orientation = orientation;
		this.model = model;
		this.majorTickSpacing = 10;

		this.painter = new LWTCSliderPainter( LWTCControlConstants.STD_SLIDER_COLOURS );

		this.setMinimumSize( orientation == SwingConstants.HORIZONTAL ?
				LWTCSliderKnobImage.H_KNOB_SIZE :
				LWTCSliderKnobImage.V_KNOB_SIZE );

		this.addKeyListener( new LWTCSliderKeyListener( model, this ) );

		this.setFocusable( true );

		mouseListener = new LWTCSliderMouseListener( this, orientation, model );

		this.addMouseListener( mouseListener );
		this.addMouseMotionListener( mouseListener );

		model.addChangeListener( new ChangeListener()
		{

			@Override
			public void stateChanged( final ChangeEvent arg0 )
			{
				repaint();
			}
		} );
	}

	@Override
	public void paintComponent( final Graphics g )
	{
		final Graphics2D g2d = (Graphics2D)g;
		final int width = getWidth();
		final int height = getHeight();
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
}
