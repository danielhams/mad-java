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

package uk.co.modularaudio.mads.base.soundfile_player2.ui;

import javax.swing.JComponent;

import uk.co.modularaudio.mads.base.soundfile_player2.mu.SoundfilePlayer2MadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player2.mu.SoundfilePlayer2MadInstance;
import uk.co.modularaudio.service.blockresampler.BlockResamplingClient;
import uk.co.modularaudio.service.samplecaching.SampleCacheClient;
import uk.co.modularaudio.service.samplecaching.SampleCachingService;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCLabel;

public class SoundfilePlayer2FileInfoUiJComponent extends LWTCLabel
	implements IMadUiControlInstance<SoundfilePlayer2MadDefinition, SoundfilePlayer2MadInstance, SoundfilePlayer2MadUiInstance>,
	SoundfileSampleEventListener
{
	private static final long serialVersionUID = 8010334395193476516L;

	private final SoundfilePlayer2MadUiInstance uiInstance;
	private final SampleCachingService sampleCachingService;

	public SoundfilePlayer2FileInfoUiJComponent( final SoundfilePlayer2MadDefinition definition,
			final SoundfilePlayer2MadInstance instance,
			final SoundfilePlayer2MadUiInstance uiInstance,
			final int controlIndex )
	{
		super( LWTCControlConstants.STD_LABEL_COLOURS, "" );
		this.setOpaque( true );
		setFont( LWTCControlConstants.LABEL_FONT );

		this.uiInstance = uiInstance;
		sampleCachingService = instance.getAdvancedComponentsFrontController().getSampleCachingService();

		uiInstance.addSampleEventListener( this );
	}

	@Override
	public void doDisplayProcessing(final ThreadSpecificTemporaryEventStorage tempEventStorage,
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
	public void receiveSampleChangeEvent( final BlockResamplingClient newSample )
	{
		if( newSample != null )
		{
			final SampleCacheClient sampleCacheClient = newSample.getSampleCacheClient();
			final StringBuilder sb = new StringBuilder();
			sb.append("\"");
			final String fileTitle = sampleCachingService.getSampleFileTitleForCacheClient(sampleCacheClient);
			sb.append( fileTitle );
			sb.append("\"");
			final String displayText = sb.toString();
			setText(displayText);
		}
		else
		{
			setText("");
		}
	}

	@Override
	public void receiveDeltaPositionEvent(final long newPosition)
	{
	}

	@Override
	public void receiveAbsPositionEvent(final long newPosition)
	{
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}

	@Override
	public void receiveCacheRefreshCompletionEvent()
	{
	}

	@Override
	public String getControlValue()
	{
		return "";
	}

	@Override
	public void receiveControlValue( final String value )
	{
	}
}
