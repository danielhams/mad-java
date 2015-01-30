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

package uk.co.modularaudio.mads.base.audioanalyser.ui;

import java.awt.Component;
import java.awt.Dimension;

import uk.co.modularaudio.mads.base.audioanalyser.mu.AudioAnalyserMadDefinition;
import uk.co.modularaudio.mads.base.audioanalyser.mu.AudioAnalyserMadInstance;
import uk.co.modularaudio.mads.base.audioanalyser.ui.tabbedpane.AudioAnalyserTabbedPane;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class AudioAnalyserDisplayUiJComponent extends PacPanel
	implements IMadUiControlInstance<AudioAnalyserMadDefinition, AudioAnalyserMadInstance, AudioAnalyserMadUiInstance>
{
	private static final long serialVersionUID = -3318742569146304946L;
	
//	private static Log log = LogFactory.getLog( AudioAnalyserDisplayUiJComponent.class.getName() );
	
	private final AudioAnalyserMadUiInstance uiInstance;
	
	// Width/height from the AAOscilloscope test
	//	new Rectangle(  6, 26, 546, 312 )
	private final static int FULL_WIDTH = 546;
	private final static int FULL_HEIGHT = 312;
	
	private AudioAnalyserTabbedPane tabbedPane;
	
	private boolean previouslyShowing = false;
		
	public AudioAnalyserDisplayUiJComponent( AudioAnalyserMadDefinition definition,
			AudioAnalyserMadInstance instance,
			AudioAnalyserMadUiInstance uiInstance,
			int controlIndex )
	{
		setOpaque( false );
		this.uiInstance = uiInstance;
		
		setPreferredSize( new Dimension( FULL_WIDTH, FULL_HEIGHT ) );
		
		MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addLayoutConstraint("fill");
		msh.addLayoutConstraint("gap 0");
		msh.addLayoutConstraint("insets 0");
		
		setLayout( msh.createMigLayout() );
		
		tabbedPane = new AudioAnalyserTabbedPane( uiInstance.getUiBufferState(), uiInstance.getUiDefinition().getBufferedImageAllocator() );
		add( tabbedPane, "grow");
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}

	@Override
	public void doDisplayProcessing( ThreadSpecificTemporaryEventStorage tempEventStorage,
			MadTimingParameters timingParameters,
			long currentGuiTime)
	{
		boolean showing = isShowing();
		
		if( previouslyShowing != showing )
		{
			uiInstance.sendUiActive( showing );
			previouslyShowing = showing;
		}
		
		if( showing )
		{
			tabbedPane.doDisplayProcessing( tempEventStorage,
				timingParameters,
				currentGuiTime );
		}
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

}
