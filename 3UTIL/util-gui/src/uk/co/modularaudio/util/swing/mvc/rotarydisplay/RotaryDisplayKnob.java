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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import javax.swing.JPanel;

import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayController;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel.ValueChangeListener;

public class RotaryDisplayKnob extends JPanel implements ValueChangeListener, FocusListener
{
//	private static Log log = LogFactory.getLog( RotaryDisplayKnob.class.getName() );

	private static final long serialVersionUID = -187035163013499473L;

	public enum KnobType
	{
		UNIPOLAR,
		BIPOLAR
	};

	protected RotaryDisplayModel sdm;

	protected final KnobType knobType;

	protected RotaryViewColors colours;

	protected final Stroke lineStroke = new BasicStroke( 2.0f,
			BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_ROUND );

	protected final float arcStartDegrees = -120.0f;
	protected final float arcExtentDegress = -300.0f;

	protected final Ellipse2D.Double ellipse = new Ellipse2D.Double();

	protected final Arc2D.Double arc = new Arc2D.Double();

	protected final Line2D.Double line2d = new Line2D.Double();

	protected final RotaryDisplayMouseListener mouseListener;

	protected final RotaryDisplayKnobKeyListener keyListener;

	protected int diameter = -1;

	protected int majorTickSpacing = 10;

	public RotaryDisplayKnob( final RotaryDisplayModel model,
			final RotaryDisplayController controller,
			final KnobType knobType,
			final RotaryViewColors colours,
			final boolean opaque,
			final boolean rightClickToReset )
	{
		this.sdm = model;
		this.knobType = knobType;
		this.colours = colours;
		this.setOpaque( opaque );

		this.setForeground( colours.fgColor );
		this.setBackground( colours.bgColor );

		setFocusable( true );

		addFocusListener( this );

		sdm.addChangeListener( this );

		mouseListener = new RotaryDisplayMouseListener( this, model, controller, rightClickToReset );
		addMouseListener( mouseListener );
		addMouseMotionListener( mouseListener );
		addFocusListener( mouseListener );

		keyListener = new RotaryDisplayKnobKeyListener( controller );
		this.addKeyListener( keyListener );
	}

	public void changeModel( final RotaryDisplayModel newModel )
	{
		this.sdm.removeChangeListener( this );
		this.sdm = newModel;
		this.sdm.addChangeListener( this );
	}

	@Override
	public void paintComponent( final Graphics g )
	{
		final Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		g2d.setRenderingHint( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE );
		g2d.setComposite( AlphaComposite.SrcOver );

//		final int width = getWidth() - 1;
//		final int height = getHeight() - 1;
		final int width = getWidth();
		final int height = getHeight();

		if( hasFocus() )
		{
			g2d.setColor( colours.knobFocusColor );
			g2d.drawRect( 0, 0, width, height );
		}

		final float wo2 = width / 2.0f;
		final float ho2 = height / 2.0f;
		final float min;
		if( diameter == -1 )
		{
			min = (width < height ? width : height);
		}
		else
		{
			min = diameter;
		}
//		log.debug( "Using min of " + min );
		final float mino2 = min/2.0f;
		final float innerIndicatorLength = mino2 * 0.65f;

		// Draw knob circle
		ellipse.setFrame( wo2-mino2, ho2-mino2, min, min );
		g2d.setColor( colours.knobFillColor );
		g2d.fill( ellipse );

		// Draw outline
		g2d.setColor( colours.knobOutlineColor );
		g2d.draw( ellipse );

		// Draw extent indicator
		g2d.setColor( colours.knobExtentColor );
		g2d.setStroke( lineStroke );

		arc.setArcByCenter( wo2, ho2, innerIndicatorLength, arcStartDegrees, arcExtentDegress, Arc2D.OPEN );
		g2d.draw( arc );

		final float curValue = sdm.getValue();
		final float valueRange = (sdm.getMaxValue() - sdm.getMinValue());
		final float scaledValue = (curValue - sdm.getMinValue()) / valueRange;

//		log.debug("Scaled value is " + scaledValue );

		final float scaledDegrees = arcExtentDegress * scaledValue;

//		log.debug("This is " + scaledDegrees + " around");

		final float theta = ((float)Math.PI * 2) * ((scaledDegrees - arcStartDegrees - 60.0f) / 360.0f);
		final float rotatedX = (float)Math.cos( theta );
		final float rotatedY = (float)Math.sin( theta );

//		log.debug("So we have (" + rotatedX + ", " + rotatedY + ")");

		// Draw the current position indicator
		g2d.setColor( colours.knobIndicatorColor );
		line2d.setLine( wo2, ho2, wo2 - (rotatedX * innerIndicatorLength), ho2 + (rotatedY * innerIndicatorLength) );
		g2d.draw( line2d );

		// And draw an arc from the "start" position to the current position
		// where the start is determined by the knob type

		switch( knobType )
		{
			case UNIPOLAR:
			{
				arc.setArcByCenter( wo2, ho2, innerIndicatorLength, arcStartDegrees, scaledDegrees, Arc2D.OPEN );
				break;
			}
			case BIPOLAR:
			default:
			{
				final float curPosStartDegrees = arcStartDegrees + scaledDegrees;
				final float initialValue = sdm.getInitialValue();
				final float diffToInitialValue = (curValue - initialValue) / valueRange;
				final float arcDegressToInitialValue = diffToInitialValue * arcExtentDegress;
				arc.setArcByCenter( wo2, ho2, innerIndicatorLength, curPosStartDegrees, -arcDegressToInitialValue, Arc2D.OPEN );
				break;
			}
		}
		g2d.draw( arc );
	}

	@Override
	public void receiveValueChange( final Object source, final float newValue )
	{
		repaint();
	}

	public void setDiameter( final int diameter )
	{
		this.diameter = diameter;
	}

	@Override
	public void focusGained( final FocusEvent e )
	{
		this.repaint();
	}

	@Override
	public void focusLost( final FocusEvent e )
	{
		this.repaint();
	}

	public float getMajorTickSpacing()
	{
		return sdm.getMajorTickSpacing();
	}

	public void setMajorTickSpacing( final int majorTickSpacing )
	{
		sdm.setMajorTickSpacing( majorTickSpacing );
	}
}
