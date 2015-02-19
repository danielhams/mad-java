package test.uk.co.modularaudio.util.audio.controlinterpolation;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.controlinterpolation.ControlValueInterpolator;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class InterpolatorVisualiser extends JPanel
{
	private static final long serialVersionUID = 335854680655068226L;

	private static Log log = LogFactory.getLog( InterpolatorVisualiser.class.getName() );

	private final ControlValueInterpolator valueInterpolator;
	private final InterpolatorVisualiser controlSrcVisualiser;

	private final BufferedImage bi;
	private final Graphics2D g2d;

	public InterpolatorVisualiser( final ControlValueInterpolator valueInterpolator,
			final InterpolatorVisualiser controlSrcVisualiser )
	{
		this.valueInterpolator = valueInterpolator;
		this.controlSrcVisualiser = controlSrcVisualiser;

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "insets 10" );
//		msh.addLayoutConstraint( "debug" );

		setLayout( msh.createMigLayout() );

		final Dimension size = new Dimension( SwingControlInterpolatorAnalysis.VIS_WIDTH + 1,
				SwingControlInterpolatorAnalysis.VIS_HEIGHT + 1 );

		setPreferredSize( size );

		// Create a buffered image that we'll plot the values in
		bi = new BufferedImage( SwingControlInterpolatorAnalysis.VIS_WIDTH+1, SwingControlInterpolatorAnalysis.VIS_HEIGHT+1,
				BufferedImage.TYPE_INT_ARGB );
		g2d = bi.createGraphics();
	}

	public void interpolateEvents( final TestEvent[] events )
	{
		final float[] vals = new float[SwingControlInterpolatorAnalysis.VIS_WIDTH];

		final int numEvents = events.length;

		int curOutputOffset = 0;

		for( int i = 1 ; i < numEvents ; ++i )
		{
			final int eventOffset = events[i].getOffsetInSamples();

			// Generate using the interpolator up to this event
			final int numForThisEvent = eventOffset - curOutputOffset;
			valueInterpolator.generateControlValues( vals, curOutputOffset, numForThisEvent );

			final float newValue = events[i].getEventValue();
			log.debug("Using newValue " + newValue );
			valueInterpolator.notifyOfNewValue( newValue );

			curOutputOffset += numForThisEvent;
		}

		if( curOutputOffset < SwingControlInterpolatorAnalysis.VIS_WIDTH )
		{
			valueInterpolator.generateControlValues( vals, curOutputOffset, SwingControlInterpolatorAnalysis.VIS_WIDTH - curOutputOffset );
		}
		g2d.setComposite( AlphaComposite.Clear );
		g2d.setColor( Color.WHITE );
		g2d.fillRect( 0, 0, SwingControlInterpolatorAnalysis.VIS_WIDTH+1, SwingControlInterpolatorAnalysis.VIS_HEIGHT+1 );

		g2d.setComposite( AlphaComposite.SrcOver );
		// If we are the src signal,
		// draw some lines where we have events
		if( controlSrcVisualiser == null )
		{
			g2d.setColor( new Color( 0.6f, 0.6f, 1.0f ) );
			for( int i = 1 ; i < numEvents ; ++i )
			{
				final int eventOffset = events[i].getOffsetInSamples();

				g2d.drawLine( eventOffset, 0, eventOffset, SwingControlInterpolatorAnalysis.VIS_HEIGHT + 1 );
			}
		}

		if( controlSrcVisualiser == null )
		{
			g2d.setColor( Color.RED );
		}
		else
		{
			g2d.setColor( Color.BLACK );
		}

		int previousY = (int)(vals[0] * SwingControlInterpolatorAnalysis.VIS_HEIGHT);
		for( int i = 1 ; i < SwingControlInterpolatorAnalysis.VIS_WIDTH ; ++i )
		{
			final float val = vals[i];
			final float asYValue = val * SwingControlInterpolatorAnalysis.VIS_HEIGHT;
			final int asYInt = (int)asYValue;
			final int x1 = i-1;
			final int y1 = previousY;
			final int x2 = i;
			final int y2 = asYInt;
//			log.debug("Drawing line from " + x1 + ", " + y1 + " to " + x2 + ", " + y2 );
			g2d.drawLine( x1, SwingControlInterpolatorAnalysis.VIS_HEIGHT - y1,
					x2, SwingControlInterpolatorAnalysis.VIS_HEIGHT - y2 );

			previousY = y2;
		}

		repaint();
	}

	@Override
	public void paint( final Graphics g )
	{
		g.setColor( Color.WHITE );
		g.fillRect( 0, 0, SwingControlInterpolatorAnalysis.VIS_WIDTH + 1, SwingControlInterpolatorAnalysis.VIS_HEIGHT + 1 );
		if( controlSrcVisualiser != null )
		{
			controlSrcVisualiser.paint( g );
		}
		g.drawImage( bi, 0, 0, null );
	}

}
