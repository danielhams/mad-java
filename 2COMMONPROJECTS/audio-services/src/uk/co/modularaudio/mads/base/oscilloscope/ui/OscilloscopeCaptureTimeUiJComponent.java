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

package uk.co.modularaudio.mads.base.oscilloscope.ui;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;

import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadDefinition;
import uk.co.modularaudio.mads.base.oscilloscope.mu.OscilloscopeMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacFloatSlider;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class OscilloscopeCaptureTimeUiJComponent extends PacFloatSlider
	implements IMadUiControlInstance<OscilloscopeMadDefinition, OscilloscopeMadInstance, OscilloscopeMadUiInstance>
{
	private static final long serialVersionUID = 2929944605857719387L;
//	private static Log log = LogFactory.getLog( OscilloscopeCaptureTimeUiJComponent.class.getName() );
		
	private OscilloscopeMadUiInstance uiInstance = null;

	public OscilloscopeCaptureTimeUiJComponent( OscilloscopeMadDefinition definition,
			OscilloscopeMadInstance instance,
			OscilloscopeMadUiInstance uiInstance,
			int controlIndex )
	{
		super( uiInstance.valueFloatSliderModel );

		this.uiInstance = uiInstance;
		this.setOpaque( false );
		setFont( this.getFont().deriveFont( 9f ) );
//		this.setPaintLabels( true );
//		this.setMinimum( 1 );
//		this.setMaximum( 20000 );
		// Default value
		this.passChangeToInstanceData( uiInstance.valueFloatSliderModel.getDoubleValue() );
	}

	public JComponent getControl()
	{
		return this;
	}

	public void stateChanged( ChangeEvent e )
	{
		this.passChangeToInstanceData( this.getValue() );
	}

	private void passChangeToInstanceData( double value )
	{
		// Convert it into a float
		float floatValue = (float)value;
		
//		currentCaptureBufferLength = AudioTimingUtils.getNumSamplesForMillisAtSampleRate( DataRate.SR_44100.getValue(), captureMillis );
//		currentCaptureBufferLength = (currentCaptureBufferLength < 1 ? 1 : (currentCaptureBufferLength > maxCaptureBufferLength ? maxCaptureBufferLength : currentCaptureBufferLength ) );
//		instance.sendCaptureMillis( captureMillis );
		uiInstance.sendCaptureMillis( floatValue );
	}

	@Override
	public void doDisplayProcessing(ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		// log.debug("Received display tick");
	}

	@Override
	public void receiveValueUpdate( double previousValue, double newValue )
	{
		if( previousValue != newValue )
		{
			passChangeToInstanceData( newValue );
		}
		
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}
}
