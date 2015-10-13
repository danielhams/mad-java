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

package uk.co.modularaudio.mads.base.imixern.ui.lane;

import java.awt.Color;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.common.ampmeter.AmpMeterMarks;
import uk.co.modularaudio.mads.base.common.ampmeter.DPAmpMeter;
import uk.co.modularaudio.mads.base.imixern.mu.MixerNMadDefinition;
import uk.co.modularaudio.mads.base.imixern.mu.MixerNMadInstance;
import uk.co.modularaudio.mads.base.imixern.ui.MixerNMadUiInstance;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class LaneStereoAmpMeter<D extends MixerNMadDefinition<D, I>, I extends MixerNMadInstance<D, I>>
	extends JPanel
	implements MeterValueReceiver
{
	private static final long serialVersionUID = 1358562457507980606L;

//	private static Log log = LogFactory.getLog( LaneStereoAmpMeter.class.getName() );

	private final DPAmpMeter leftMeter;
	private final AmpMeterMarks meterMarks;
	private final DPAmpMeter rightMeter;

	public LaneStereoAmpMeter( final MixerNMadUiInstance<D,I> uiInstance,
			final BufferedImageAllocator bia,
			final boolean showClipBox )
	{
		super();
		setOpaque( false );
		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "filly" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "insets 1" );

		this.setLayout( msh.createMigLayout() );

		leftMeter = new DPAmpMeter( bia, showClipBox );
		this.add( leftMeter, "gapbottom " +
				AmpMeterMarks.METER_LABEL_NEEDED_TOP_BOTTOM_INSET_PIXELS + ", alignx right, growy" );

		meterMarks = new AmpMeterMarks( Color.BLACK, false );
		this.add( meterMarks, "growy, growx 0");

		rightMeter = new DPAmpMeter( bia, showClipBox );
		this.add( rightMeter, "gapbottom "+
				AmpMeterMarks.METER_LABEL_NEEDED_TOP_BOTTOM_INSET_PIXELS + ", alignx left, growy" );
	}

	public void receiveDisplayTick( final long currentGuiTime )
	{
		leftMeter.receiveDisplayTick( currentGuiTime );
		rightMeter.receiveDisplayTick( currentGuiTime );
	}

	public void destroy()
	{
		leftMeter.destroy();
		rightMeter.destroy();
	}

	@Override
	public void setFramesBetweenPeakReset( final int framesBetweenPeakReset )
	{
		leftMeter.setFramesBetweenPeakReset( framesBetweenPeakReset );
		rightMeter.setFramesBetweenPeakReset( framesBetweenPeakReset );
	}

	@Override
	public void receiveMeterReadingLevel( final long currentTimestamp,
			final float leftMeterReading,
			final float rightMeterReading )
	{
		final float leftMeterReadingDb = AudioMath.levelToDbF( leftMeterReading );
		final float rightMeterReadingDb = AudioMath.levelToDbF( rightMeterReading );
		leftMeter.receiveMeterReadingInDb( currentTimestamp, leftMeterReadingDb );
		rightMeter.receiveMeterReadingInDb( currentTimestamp, rightMeterReadingDb );
	}

}
