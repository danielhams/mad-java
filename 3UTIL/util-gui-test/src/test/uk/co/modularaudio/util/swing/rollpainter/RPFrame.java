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

package test.uk.co.modularaudio.util.swing.rollpainter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.mvc.displayslider.SimpleSliderIntToFloatConverter;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayController;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView.SatelliteOrientation;

public class RPFrame extends JFrame implements ActionListener
{
	private static final long serialVersionUID = -6805016213201375423L;
//	private static Log log = LogFactory.getLog( RPFrame.class.getName() );

	private final RPPlayStopToggleButton playStopToggle;

	private final RPSpeedSlider speedSlider;
	private final RPDebuggingDrawable debuggingDrawable;
	private final RPCanvas canvas;

	private Timer timer;

	public RPFrame() throws DatastoreException
	{
		setTitle("RollPainterTester");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		this.setMinimumSize(  new Dimension(RPConstants.RP_CANVAS_WIDTH * 3, RPConstants.RP_CANVAS_HEIGHT + 20) );

		final MigLayoutStringHelper lh = new MigLayoutStringHelper();
//		lh.addLayoutConstraint( "debug" );
		lh.addLayoutConstraint( "fill" );
		lh.addLayoutConstraint( "insets 0" );
		lh.addLayoutConstraint( "gap 0" );

		lh.addRowConstraint( "[][fill]" );
		final MigLayout layout = lh.createMigLayout();
		setLayout( layout );


		final SliderDisplayModel model = new SliderDisplayModel( -20.0f, 20.0f, 0.0f, 0.0f, 40, 1, new SimpleSliderIntToFloatConverter(), 3, 0, "pixels");
		final SliderDisplayController controller = new SliderDisplayController( model );
		final SliderDisplayView.SatelliteOrientation labelOrientation = SatelliteOrientation.LEFT;
		final SliderDisplayView.DisplayOrientation displayOrientation = DisplayOrientation.HORIZONTAL;
		final SliderDisplayView.SatelliteOrientation textboxOrientation = SatelliteOrientation.RIGHT;
		final String labelText = "Pixels To Scroll";
		final Color labelColor = Color.BLACK;
		final Color unitsColor = Color.BLACK;
		final boolean opaque = true;

		speedSlider = new RPSpeedSlider( model, controller, labelOrientation, displayOrientation, textboxOrientation, labelText, labelColor, unitsColor, opaque );
		canvas = new RPCanvas();
		playStopToggle = new RPPlayStopToggleButton(canvas);
		debuggingDrawable = new RPDebuggingDrawable( canvas );
		model.addChangeListener( canvas );

		add( playStopToggle, "" );
		add( speedSlider, "wrap" );
		add( debuggingDrawable, "grow, pushy, spanx 2");

		validate();
	}

	public void startCallbacks()
	{
		timer = new Timer( 1000 / RPConstants.RP_FPS, this );
		timer.start();
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
//		log.debug("Callback!");
		canvas.receivedCallbackTick();
		Toolkit.getDefaultToolkit().sync();
	}

	@Override
	public void dispose()
	{
		if( timer != null )
		{
			timer.stop();
		}
		super.dispose();
	}
}
