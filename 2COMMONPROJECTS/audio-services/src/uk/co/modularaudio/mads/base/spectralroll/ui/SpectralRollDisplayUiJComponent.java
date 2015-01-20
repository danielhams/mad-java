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

package uk.co.modularaudio.mads.base.spectralroll.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import uk.co.modularaudio.mads.base.spectralroll.mu.SpectralRollMadDefinition;
import uk.co.modularaudio.mads.base.spectralroll.mu.SpectralRollMadInstance;
import uk.co.modularaudio.mads.base.spectralroll.util.SpecDataListener;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacPanel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class SpectralRollDisplayUiJComponent extends PacPanel
	implements IMadUiControlInstance<SpectralRollMadDefinition, SpectralRollMadInstance, SpectralRollMadUiInstance>,
	SpecDataListener
{
	private static final long serialVersionUID = -2715013080290701990L;
	
//	private static Log log = LogFactory.getLog( SpectralRollDisplayUiJComponent.class.getName() );

	private DoubleImageScrollingCanvas scrollingCanvas = null;
	
	private SpectralRollMadUiInstance uiInstance = null;
	
	private boolean previouslyShowing = false;

	public SpectralRollDisplayUiJComponent( SpectralRollMadDefinition definition,
			SpectralRollMadInstance instance,
			SpectralRollMadUiInstance uiInstance,
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
		scrollingCanvas = new DoubleImageScrollingCanvas( uiInstance, bounds.width, bounds.height );
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
			
		}
	}
	
	private void clearDisplay()
	{
		scrollingCanvas.clear();
	}

	@Override
	public void paint(Graphics g)
	{
		Graphics2D g2d = (Graphics2D)g;
		scrollingCanvas.paint( g2d );
	}

	@Override
	public void processScopeData( float[] newAmps )
	{
		scrollingCanvas.processNewAmps( newAmps );
		repaint();
	}

	@Override
	public void destroy()
	{
		if( scrollingCanvas != null )
		{
			scrollingCanvas.destroy();
		}
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}
}
