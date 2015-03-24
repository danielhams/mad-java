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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import javax.swing.JPanel;

import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayController;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel.ValueChangeListener;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryIntToFloatConverter;

public class RotaryDisplayKnob extends JPanel implements ValueChangeListener
{
//	private static Log log = LogFactory.getLog( RotaryDisplayKnob.class.getName() );

	private static final long serialVersionUID = -187035163013499473L;

	public enum KnobType
	{
		UNIPOLAR,
		BIPOLAR
	};

	private RotaryDisplayModel sdm;

	private final KnobType knobType;

	private final Color foregroundColor;
	private final Color knobColor;
	private final Color outlineColor;
	private final Color indicatorColor;

//	private final static Color ERASE_COLOR = new Color( 0.0f, 0.0f, 0.0f, 1.0f );

	private final Stroke lineStroke = new BasicStroke( 2.0f,
			BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_ROUND );

	private final float arcStartDegrees = -120.0f;
	private final float arcExtentDegress = -300.0f;

	private final Ellipse2D.Double ellipse = new Ellipse2D.Double();

	private final Arc2D.Double arc = new Arc2D.Double();

	private final Line2D.Double line2d = new Line2D.Double();

	private final RotaryDisplayMouseListener mouseListener;

	public RotaryDisplayKnob( final RotaryDisplayModel model,
			final RotaryDisplayController controller,
			final KnobType knobType,
			final Color backgroundColor,
			final Color foregroundColor,
			final Color knobColor,
			final Color outlineColor,
			final Color indicatorColor,
			final boolean opaque )
	{
		this.sdm = model;
		this.knobType = knobType;
		this.foregroundColor = foregroundColor;
		this.knobColor = knobColor;
		this.outlineColor = outlineColor;
		this.indicatorColor = indicatorColor;
		this.setOpaque( opaque );

		this.setForeground( foregroundColor );
		this.setBackground( backgroundColor );

		sdm.addChangeListener( this );

		mouseListener = new RotaryDisplayMouseListener( model, controller );
		addMouseListener( mouseListener );
		addMouseMotionListener( mouseListener );
	}

	public void changeModel( final RotaryDisplayModel newModel )
	{
		this.sdm.removeChangeListener( this );
		this.sdm = newModel;
		this.sdm.addChangeListener( this );
	}

	public int getInitialValue()
	{
		final RotaryIntToFloatConverter itfc = sdm.getSliderIntToFloatConverter();
		return itfc.floatValueToSliderIntValue( sdm, sdm.getInitialValue() );
	}

	@Override
	public void paint( final Graphics g )
	{
		final Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		g2d.setComposite( AlphaComposite.SrcOver );

		final int width = getWidth() - 1;
		final int height = getHeight() - 1;
		final float wo2 = width / 2.0f;
		final float ho2 = height / 2.0f;
		final float min = (width < height ? width : height);
		final float mino2 = min/2.0f;
		final float innerIndicatorLength = mino2 * 0.65f;

		// Draw knob circle
		ellipse.setFrame( wo2-mino2, ho2-mino2, min, min );
		g2d.setColor( knobColor );
		g2d.fill( ellipse );

		// Draw outline
		g2d.setColor( outlineColor );
		g2d.draw( ellipse );

		// Draw extent indicator
		g2d.setColor( foregroundColor );
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
		g2d.setColor( indicatorColor );
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


}
