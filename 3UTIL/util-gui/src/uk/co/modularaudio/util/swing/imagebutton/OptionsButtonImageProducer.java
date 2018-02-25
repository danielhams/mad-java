package uk.co.modularaudio.util.swing.imagebutton;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

import uk.co.modularaudio.util.swing.imagebutton.ImageButton.ImageButtonProducer;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class OptionsButtonImageProducer implements ImageButtonProducer
{
	private final Rectangle clipBounds = new Rectangle();

	@Override
	public void draw( final Graphics2D g2d )
	{
		g2d.getClipBounds( clipBounds );
		final int width = clipBounds.width;
		final int height = clipBounds.height;
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		g2d.setRenderingHint( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE );

		g2d.setColor( LWTCControlConstants.CONTROL_LABEL_FOREGROUND );

		g2d.setStroke( new BasicStroke( 1.0f ) );

		final float minDimension = (width < height ? width : height) - 10;

		final float centerX = width / 2.0f;
		final float centerY = height / 2.0f;

		final float maxRadius = minDimension / 2.0f;

		final float innerRadius = maxRadius  * .55f;
		final float outerRadius = maxRadius;
		final float gearDepth = maxRadius * .2f;
		final float gearBaseRadius = maxRadius - gearDepth;

		final Ellipse2D.Float circle = new Ellipse2D.Float( centerX - innerRadius,
				centerY - innerRadius,
				innerRadius * 2,
				innerRadius * 2 );
		g2d.draw( circle );

		final int numGearFaces = 8;
		float currentAngle = 0.0f;
		final float oneFaceAngleDiff = 360.0f / numGearFaces;
		final float halfFaceAngleDiff = oneFaceAngleDiff / 2.0f;

		final GeneralPath path = new GeneralPath();

		path.moveTo( centerX + outerRadius, centerY );
		for( int i = 0 ; i < numGearFaces ; ++i )
		{
			final Arc2D.Float outerArc = new Arc2D.Float(
					centerX - outerRadius,
					centerY - outerRadius,
					outerRadius * 2,
					outerRadius * 2,
					currentAngle,
					halfFaceAngleDiff,
					Arc2D.OPEN );
//			g2d.draw( outerArc );

			path.append( outerArc, true );

			currentAngle += halfFaceAngleDiff;
			// Draw other bits
			final Arc2D.Float innerArc = new Arc2D.Float(
					centerX - gearBaseRadius,
					centerY - gearBaseRadius,
					gearBaseRadius * 2,
					gearBaseRadius * 2,
					currentAngle,
					halfFaceAngleDiff,
					Arc2D.OPEN );
//			g2d.draw( innerArc );

			path.append( innerArc, true );
			currentAngle += halfFaceAngleDiff;
		}

		path.closePath();

		g2d.draw( path );

	}
}
