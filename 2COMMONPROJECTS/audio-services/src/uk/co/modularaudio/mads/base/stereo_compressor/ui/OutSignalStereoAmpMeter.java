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

import java.awt.Font;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.math.AmpMeterDbToLevelComputer;
import uk.co.modularaudio.util.audio.math.DbToLevelComputer;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;

public class OutSignalStereoAmpMeter extends JPanel
{
	private static final long serialVersionUID = 1358562457507980606L;

	private static Log log = LogFactory.getLog( OutSignalStereoAmpMeter.class.getName() );

	private final static int METER_NUM_STEPS = 1000;

	private final OutSignalAmpMeter leftAmpMeter;
	private final OutSignalAmpMeterLevelMarks ampMeterLabels;
	private final OutSignalAmpMeter rightAmpMeter;

	public OutSignalStereoAmpMeter( final StereoCompressorMadUiInstance uiInstance,
			final BufferedImageAllocator bia,
			final boolean showClipBox )
	{
		setOpaque( false );
		MigLayout compLayout = new MigLayout("insets 1, gap 0, fill");
		this.setLayout( compLayout );

		Font f = getFont().deriveFont( 9.0f );
//		Font f = getFont();

		final DbToLevelComputer dbToLevelComputer = new AmpMeterDbToLevelComputer( METER_NUM_STEPS );

		leftAmpMeter = new OutSignalAmpMeter( uiInstance, dbToLevelComputer, bia, showClipBox );
		this.add( leftAmpMeter, "gaptop " +
				OutSignalAmpMeterLevelMarks.METER_LABEL_NEEDED_TOP_BOTTOM_INSET_PIXELS + ", gapbottom " +
				OutSignalAmpMeterLevelMarks.METER_LABEL_NEEDED_TOP_BOTTOM_INSET_PIXELS + ", alignx right, growy" );
		ampMeterLabels = new OutSignalAmpMeterLevelMarks( dbToLevelComputer, showClipBox, f );
		this.add( ampMeterLabels, "growy, growx 0");
		rightAmpMeter = new OutSignalAmpMeter( uiInstance, dbToLevelComputer, bia, showClipBox );
		this.add( rightAmpMeter, "gaptop " +
				OutSignalAmpMeterLevelMarks.METER_LABEL_NEEDED_TOP_BOTTOM_INSET_PIXELS + ", gapbottom "+
				OutSignalAmpMeterLevelMarks.METER_LABEL_NEEDED_TOP_BOTTOM_INSET_PIXELS + ", alignx left, growy" );
	}

	public void receiveMeterReadingInDb( final long currentFrameTime,
			final int channelNum,
			final float meterReadingDb )
	{
		switch( channelNum )
		{
			case 0:
			{
				leftAmpMeter.receiveMeterReadingInDb( currentFrameTime, meterReadingDb );
				break;
			}
			case 1:
			{
				rightAmpMeter.receiveMeterReadingInDb( currentFrameTime, meterReadingDb );
				break;
			}
			default:
			{
				log.error("Oops. Which channel was this for?");
				break;
			}
		}
	}

	public void receiveDisplayTick( final long currentGuiTime )
	{
		leftAmpMeter.receiveDisplayTick( currentGuiTime );
		rightAmpMeter.receiveDisplayTick( currentGuiTime );
	}

	public void destroy()
	{
		leftAmpMeter.destroy();
		rightAmpMeter.destroy();
	}
}
