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

package uk.co.modularaudio.mads.base.waveroller.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import uk.co.modularaudio.mads.base.waveroller.mu.WaveRollerMadDefinition;
import uk.co.modularaudio.mads.base.waveroller.mu.WaveRollerMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacComponent;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class WaveRollerScaleDisplay extends PacComponent
	implements IMadUiControlInstance<WaveRollerMadDefinition, WaveRollerMadInstance, WaveRollerMadUiInstance>
{
	private static final long serialVersionUID = 1L;

//	private static Log log = LogFactory.getLog( RollPainterScaleDisplay.class.getName() );
		
	public WaveRollerScaleDisplay( WaveRollerMadDefinition definition,
			WaveRollerMadInstance instance,
			WaveRollerMadUiInstance uiInstance,
			int controlIndex )
	{
	}
	
	public void paint( Graphics g )
	{
		g.setColor(  Color.BLACK  );
		g.fillRect( 0, 0, getWidth(), getHeight() );
	}

	@Override
	public void doDisplayProcessing( ThreadSpecificTemporaryEventStorage tempEventStorage,
			MadTimingParameters timingParameters, long currentGuiTime )
	{
	}

	@Override
	public Component getControl()
	{
		return this;
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}
}
