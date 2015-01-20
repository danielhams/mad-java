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

package uk.co.modularaudio.mads.base.spectralamp.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadDefinition;
import uk.co.modularaudio.mads.base.spectralamp.mu.SpectralAmpMadInstance;
import uk.co.modularaudio.mads.base.spectralamp.util.SpecDataListener;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacPanel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class SpectralAmpDisplayUiJComponent extends PacPanel
	implements IMadUiControlInstance<SpectralAmpMadDefinition, SpectralAmpMadInstance, SpectralAmpMadUiInstance>, SpecDataListener
{
//	private static Log log = LogFactory.getLog( SpectralAmpDisplayUiJComponent.class.getName() );

	private static final long serialVersionUID = -4063236010563819354L;

	private PeakDisplay peakDisplay = null;

	private SpectralAmpMadUiInstance uiInstance = null;

	private boolean previouslyShowing = false;
	
	public SpectralAmpDisplayUiJComponent( SpectralAmpMadDefinition definition,
			SpectralAmpMadInstance instance,
			SpectralAmpMadUiInstance uiInstance,
			int controlIndex )
	{
		setOpaque( true );

		this.uiInstance = uiInstance;
		uiInstance.setSpecDataListener( this );
	}
	
	@Override
	public void setBounds( Rectangle bounds )
	{
		super.setBounds( bounds );
		
		peakDisplay = new PeakDisplay( this, bounds.width, bounds.height, uiInstance );
	}

	@Override
	public Component getControl()
	{
		return this;
	}

	@Override
	public void doDisplayProcessing(ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
		boolean showing = isShowing();

		if( previouslyShowing != showing )
		{
			if( showing )
			{
				clearDisplay();
			}
			uiInstance.sendUiActive( showing );
			previouslyShowing = showing;
			// Clear the display when we return to active
		}
	}
	
	@Override
	public void paint(Graphics g)
	{
//		long paintTime = System.nanoTime();
//		log.debug( "ScAPeakDisplay paint at ts:" + paintTime );

		if( isVisible() && peakDisplay != null )
		{
			Graphics2D g2d = (Graphics2D)g;
			peakDisplay.paint( g2d );
		}
	}

	@Override
	public void processScopeData( float[] processedAmpsData )
	{
		peakDisplay.processNewAmps( processedAmpsData );
		// Now repaint
		repaint();
	}
	
	private void clearDisplay()
	{
		peakDisplay.clear();
	}

	@Override
	public void destroy()
	{
		if( peakDisplay != null )
		{
			peakDisplay.destroy();
		}
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}
}
