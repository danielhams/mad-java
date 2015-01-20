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
import uk.co.modularaudio.util.audio.math.AttenuationMeterDbToLevelComputer;
import uk.co.modularaudio.util.audio.math.DbToLevelComputer;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;

public class AttenuationMeterAndLabels extends JPanel
{
	private static final long serialVersionUID = 1358562457507980606L;
	
//	private static Log log = LogFactory.getLog( AttenuationMeterAndLabels.class.getName() );
	
	private static int METER_NUM_STEPS = 1000;
		
	private AttenuationMeter attenuationMeter = null;
	private AttenuationMeterLevelMarks meterLabels = null;
	
	private DbToLevelComputer dbToLevelComputer = null;
	
	public AttenuationMeterAndLabels( MonoCompressorMadUiInstance uiInstance, BufferedImageAllocator bia )
	{
		setOpaque( false );
		MigLayout compLayout = new MigLayout("insets 1, gap 0, filly");
		this.setLayout( compLayout );
		
		Font f = getFont().deriveFont( 9.0f );
		
		dbToLevelComputer = new AttenuationMeterDbToLevelComputer( METER_NUM_STEPS );
		
		attenuationMeter = new AttenuationMeter( uiInstance, dbToLevelComputer, bia);
		this.add( attenuationMeter, "gaptop " +
				SourceSignalAmpMeterLevelMarks.METER_LABEL_NEEDED_TOP_BOTTOM_INSET_PIXELS + ", gapbottom " +
				SourceSignalAmpMeterLevelMarks.METER_LABEL_NEEDED_TOP_BOTTOM_INSET_PIXELS + ", alignx right, growy" );
		meterLabels = new AttenuationMeterLevelMarks( dbToLevelComputer, f );
		this.add( meterLabels, "growy, growx 0");
	}

	public void receiveMeterReadingInDb( long currentTimestamp,
			float meterReadingDb )
	{
		attenuationMeter.receiveMeterReadingInDb( currentTimestamp, meterReadingDb );
	}

	public void receiveDisplayTick( long currentGuiTime )
	{
		attenuationMeter.receiveDisplayTick( currentGuiTime );
	}

	public void destroy()
	{
		attenuationMeter.destroy();
	}
}
