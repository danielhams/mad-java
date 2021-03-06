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

public class StereoCompressorSourceSignalMeterUiComponent extends PacPanel
	implements IMadUiControlInstance<StereoCompressorMadDefinition, StereoCompressorMadInstance, StereoCompressorMadUiInstance>,
	MeterValueReceiver, ThresholdValueReceiver
{
	private static final long serialVersionUID = 4901900175673258302L;

//	private static Log log = LogFactory.getLog( StereoCompressorSourceSignalMeterUiComponent.class.getName() );

	private final SourceSignalStereoAmpMeter stereoAmpMeter;

	public StereoCompressorSourceSignalMeterUiComponent( final StereoCompressorMadDefinition definition,
			final StereoCompressorMadInstance instance,
			final StereoCompressorMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.setOpaque( false );
//		this.setBackground( Color.GREEN );

		final MigLayout compLayout = new MigLayout("insets 1, gap 0, fill");
		this.setLayout( compLayout );

		stereoAmpMeter = new SourceSignalStereoAmpMeter( uiInstance,
				((StereoCompressorMadUiDefinition)uiInstance.getUiDefinition()).getBufferedImageAllocator(),
				false );
		this.add( stereoAmpMeter, "grow" );

		uiInstance.registerSourceSignalMeterValueReceiver( this );
		uiInstance.registerThresholdValueReceiver( this );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
	{
		stereoAmpMeter.receiveDisplayTick( currentGuiTime );
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
		stereoAmpMeter.receiveMeterReadingInDb( currentFrameTime, channelNum, meterReadingDb );
	}

	@Override
	public void destroy()
	{
		stereoAmpMeter.destroy();
	}

	@Override
	public void receiveNewDbValue( final float newThresholdDb )
	{
		stereoAmpMeter.setThresholdDb( newThresholdDb );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}

}
