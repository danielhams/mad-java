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

package uk.co.modularaudio.mads.base.stereo_compressor.ui;

import javax.swing.JComponent;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.mads.base.stereo_compressor.mu.StereoCompressorMadDefinition;
import uk.co.modularaudio.mads.base.stereo_compressor.mu.StereoCompressorMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.math.AudioMath;

public class StereoCompressorAttenuationMeterUiComponent extends PacPanel
	implements IMadUiControlInstance<StereoCompressorMadDefinition, StereoCompressorMadInstance, StereoCompressorMadUiInstance>,
	MeterValueReceiver
{
	private static final long serialVersionUID = 4901900175673258302L;

//	private static Log log = LogFactory.getLog( StereoCompressorAttenuationMeterUiComponent.class.getName() );

	private final AttenuationMeterAndLabels attenuationMeter;

	private final StereoCompressorMadUiInstance uiInstance;

	private boolean previouslyShowing;

	public StereoCompressorAttenuationMeterUiComponent( final StereoCompressorMadDefinition definition,
			final StereoCompressorMadInstance instance,
			final StereoCompressorMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.setOpaque( false );
//		this.setBackground( Color.GREEN );

		this.uiInstance = uiInstance;

		final MigLayout compLayout = new MigLayout("insets 1, gap 0, fill");
		this.setLayout( compLayout );

		attenuationMeter = new AttenuationMeterAndLabels( uiInstance,
				((StereoCompressorMadUiDefinition)uiInstance.getUiDefinition()).getBufferedImageAllocator() );
		this.add( attenuationMeter, "grow" );

		uiInstance.registerAttenuationSignalMeterValueReceiver( this );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
	{
		final boolean showing = isShowing();

		if( previouslyShowing != showing )
		{
			uiInstance.sendUiActive( showing );
			previouslyShowing = showing;
		}

		attenuationMeter.receiveDisplayTick( currentGuiTime );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public String getControlValue()
	{
		return "";
	}

	@Override
	public void receiveControlValue( final String value )
	{
	}

	@Override
	public void receiveMeterReadingLevel( final long currentFrameTime, final int channelNum, final float meterReadingLevel )
	{
		final float meterReadingDb = (float)AudioMath.levelToDb( meterReadingLevel );
		attenuationMeter.receiveMeterReadingInDb( currentFrameTime, channelNum, meterReadingDb );
	}

	@Override
	public void destroy()
	{
		attenuationMeter.destroy();
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}
}
