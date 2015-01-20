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

import java.awt.Font;

import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadInstance;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.SampleCachingService;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacLabel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;

public class SoundfilePlayerFileInfoUiJComponent extends PacLabel
	implements IMadUiControlInstance<SoundfilePlayerMadDefinition, SoundfilePlayerMadInstance, SoundfilePlayerMadUiInstance>,
	SoundfileSampleEventListener
{
	private static final long serialVersionUID = 8010334395193476516L;
	
	private final SoundfilePlayerMadUiInstance uiInstance;
	private final SampleCachingService sampleCachingService;
	
	public SoundfilePlayerFileInfoUiJComponent( SoundfilePlayerMadDefinition definition,
			SoundfilePlayerMadInstance instance,
			SoundfilePlayerMadUiInstance uiInstance,
			int controlIndex )
	{
		this.setOpaque( true );

//		Font f = this.getFont().deriveFont( 9f );
		Font f = this.getFont();
		setFont( f );
//		this.setBackground( Color.WHITE );
//		this.setBorder( new LineBorder( Color.BLACK ) );
		
		this.uiInstance = uiInstance;
		sampleCachingService = instance.getAdvancedComponentsFrontController().getSampleCachingService();
		
		uiInstance.addSampleEventListener( this );
		setText("");
	}

	@Override
	public void doDisplayProcessing(ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void destroy()
	{
		uiInstance.removeFileInfoReceiver( this );
	}

	@Override
	public void receiveSampleChangeEvent( BlockResamplingClient newSample )
	{
		if( newSample != null )
		{
			SampleCacheClient sampleCacheClient = newSample.getSampleCacheClient();
			StringBuilder sb = new StringBuilder();
			sb.append("\"");
			String fileTitle = sampleCachingService.getSampleFileTitleForCacheClient(sampleCacheClient);
			sb.append( fileTitle );
			sb.append("\"");
			String displayText = sb.toString();
			setText(displayText);
		}
		else
		{
			setText("");
		}
	}

	@Override
	public void receiveDeltaPositionEvent(long newPosition)
	{
	}

	@Override
	public void receiveAbsPositionEvent(long newPosition)
	{
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}
}
