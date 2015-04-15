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

package uk.co.modularaudio.mads.base.soundfile_player.ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class WaveOverviewPositionClickListener implements MouseListener
{
	private final SoundfilePlayerWaveOverviewUiJComponent waveOverviewComponent;

	public WaveOverviewPositionClickListener( final SoundfilePlayerWaveOverviewUiJComponent waveOverviewComponent )
	{
		this.waveOverviewComponent = waveOverviewComponent;
	}

	@Override
	public void mouseClicked( final MouseEvent me )
	{
		waveOverviewComponent.handleOverviewClickAtPoint( me.getPoint() );
	}

	@Override
	public void mouseEntered( final MouseEvent me )
	{
	}

	@Override
	public void mouseExited( final MouseEvent me )
	{
	}

	@Override
	public void mousePressed( final MouseEvent me )
	{
	}

	@Override
	public void mouseReleased( final MouseEvent me )
	{
	}
}
