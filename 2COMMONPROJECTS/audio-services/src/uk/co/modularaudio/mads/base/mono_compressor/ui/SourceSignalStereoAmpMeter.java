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

import java.awt.Font;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.util.audio.math.AmpMeterDbToLevelComputer;
import uk.co.modularaudio.util.audio.math.DbToLevelComputer;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;

public class SourceSignalStereoAmpMeter extends JPanel
{
	private static final long serialVersionUID = 1358562457507980606L;

//	private static Log log = LogFactory.getLog( SourceSignalStereoAmpMeter.class.getName() );

	private final static int METER_NUM_STEPS = 1000;

	private final SourceSignalAmpMeter leftAmpMeter;
	private final SourceSignalAmpMeterLevelMarks ampMeterLabels;

	private final DbToLevelComputer dbToLevelComputer;

	public SourceSignalStereoAmpMeter( final MonoCompressorMadUiInstance uiInstance, final BufferedImageAllocator bia, final boolean showClipBox )
	{
		setOpaque( false );
		final MigLayout compLayout = new MigLayout("insets 1, gap 0, filly");
		this.setLayout( compLayout );

		final Font f = getFont().deriveFont( 9.0f );

		dbToLevelComputer = new AmpMeterDbToLevelComputer( METER_NUM_STEPS );

		leftAmpMeter = new SourceSignalAmpMeter( uiInstance, dbToLevelComputer, bia, showClipBox );
		this.add( leftAmpMeter, "gaptop " +
				SourceSignalAmpMeterLevelMarks.METER_LABEL_NEEDED_TOP_BOTTOM_INSET_PIXELS + ", gapbottom " +
				SourceSignalAmpMeterLevelMarks.METER_LABEL_NEEDED_TOP_BOTTOM_INSET_PIXELS + ", alignx right, growy" );
		ampMeterLabels = new SourceSignalAmpMeterLevelMarks( dbToLevelComputer, showClipBox, f );
		this.add( ampMeterLabels, "growy, growx 0");
	}

	public void receiveMeterReadingInDb( final long currentTimestamp,
			final float meterReadingDb )
	{
//		log.debug("Received one: " + meterReadingDb );
		leftAmpMeter.receiveMeterReadingInDb( currentTimestamp, meterReadingDb );
	}

	public void receiveDisplayTick( final long currentGuiTime )
	{
		leftAmpMeter.receiveDisplayTick( currentGuiTime );
	}

	public void destroy()
	{
		leftAmpMeter.destroy();
	}

	public void setThresholdDb( final float newThresholdDb )
	{
		leftAmpMeter.setThresholdDb( newThresholdDb );
	}

}
