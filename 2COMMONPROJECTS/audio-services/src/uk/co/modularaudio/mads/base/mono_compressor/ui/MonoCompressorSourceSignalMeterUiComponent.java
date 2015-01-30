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

package uk.co.modularaudio.mads.base.mono_compressor.ui;

import javax.swing.JComponent;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.mads.base.mono_compressor.mu.MonoCompressorMadDefinition;
import uk.co.modularaudio.mads.base.mono_compressor.mu.MonoCompressorMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.math.AudioMath;

public class MonoCompressorSourceSignalMeterUiComponent extends PacPanel
	implements IMadUiControlInstance<MonoCompressorMadDefinition, MonoCompressorMadInstance, MonoCompressorMadUiInstance>,
	MeterValueReceiver, ThresholdValueReceiver
{
	private static final long serialVersionUID = 4901900175673258302L;

//	private static Log log = LogFactory.getLog( MonoCompressorSourceSignalMeterUiComponent.class.getName() );

	private final SourceSignalStereoAmpMeter stereoAmpMeter;

	public MonoCompressorSourceSignalMeterUiComponent( final MonoCompressorMadDefinition definition,
			final MonoCompressorMadInstance instance,
			final MonoCompressorMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.setOpaque( false );
//		this.setBackground( Color.GREEN );

		final MigLayout compLayout = new MigLayout("insets 1, gap 0, fill");
		this.setLayout( compLayout );

		stereoAmpMeter = new SourceSignalStereoAmpMeter( uiInstance, uiInstance.getUiDefinition().getBufferedImageAllocator(), false );
		this.add( stereoAmpMeter, "grow, wrap" );

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
	public void receiveMeterReadingLevel( final long currentTimestamp, final float meterReadingLevel )
	{
//		log.debug("Received one: " + meterReadingLevel );
		final float meterReadingDb = (float)AudioMath.levelToDb( meterReadingLevel );
		stereoAmpMeter.receiveMeterReadingInDb( currentTimestamp, meterReadingDb );
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
