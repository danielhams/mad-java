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

package uk.co.modularaudio.mads.base.cvsurface.mu;

import java.util.Map;

import uk.co.modularaudio.mads.base.BaseComponentsCreationContext;
import uk.co.modularaudio.util.audio.controlinterpolation.SpringAndDamperDoubleInterpolator;
import uk.co.modularaudio.util.audio.mad.MadChannelBuffer;
import uk.co.modularaudio.util.audio.mad.MadChannelConfiguration;
import uk.co.modularaudio.util.audio.mad.MadChannelConnectedFlags;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.audio.mad.hardwareio.HardwareIOChannelSettings;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadFrameTimeFactory;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.thread.RealtimeMethodReturnCodeEnum;

public class CvSurfaceMadInstance extends MadInstance<CvSurfaceMadDefinition, CvSurfaceMadInstance>
{
//	private static Log log = LogFactory.getLog( CvSurfaceMadInstance.class.getName() );

	private int sampleRate;

	private float desiredX;
	private float desiredY;

	private final SpringAndDamperDoubleInterpolator xSad = new SpringAndDamperDoubleInterpolator( -1.0f, 1.0f );
	private final SpringAndDamperDoubleInterpolator ySad = new SpringAndDamperDoubleInterpolator( -1.0f, 1.0f );

	public CvSurfaceMadInstance( final BaseComponentsCreationContext creationContext,
			final String instanceName,
			final CvSurfaceMadDefinition definition,
			final Map<MadParameterDefinition, String> creationParameterValues,
			final MadChannelConfiguration channelConfiguration )
	{
		super( instanceName, definition, creationParameterValues, channelConfiguration );
		xSad.hardSetValue( 0.0f );
		ySad.hardSetValue( 0.0f );
	}

	@Override
	public void startup( final HardwareIOChannelSettings hardwareChannelSettings, final MadTimingParameters timingParameters, final MadFrameTimeFactory frameTimeFactory )
			throws MadProcessingException
	{
		sampleRate = hardwareChannelSettings.getAudioChannelSetting().getDataRate().getValue();

		xSad.reset( sampleRate );
		ySad.reset( sampleRate );

		xSad.hardSetValue( desiredX );
		ySad.hardSetValue( desiredY );
	}

	@Override
	public void stop() throws MadProcessingException
	{
	}

	@Override
	public RealtimeMethodReturnCodeEnum process( final ThreadSpecificTemporaryEventStorage tempQueueEntryStorage ,
			final MadTimingParameters timingParameters ,
			final long periodStartFrameTime ,
			final MadChannelConnectedFlags channelConnectedFlags ,
			final MadChannelBuffer[] channelBuffers , final int frameOffset , final int numFrames  )
	{
		final MadChannelBuffer outCVXcb = channelBuffers[ CvSurfaceMadDefinition.PRODUCER_OUT_CVX ];
		final float[] outCVXBuffer = outCVXcb.floatBuffer;
		final MadChannelBuffer outCVYcb = channelBuffers[ CvSurfaceMadDefinition.PRODUCER_OUT_CVY ];
		final float[] outCVYBuffer = outCVYcb.floatBuffer;

		final float[] tmpBuffer = tempQueueEntryStorage.temporaryFloatArray;

		final int xOffset = 0;
		final int yOffset = numFrames;
		xSad.checkForDenormal();
		xSad.generateControlValues( tmpBuffer, xOffset, numFrames );
		ySad.checkForDenormal();
		ySad.generateControlValues( tmpBuffer, yOffset, numFrames );


		for( int s = 0 ; s < numFrames ; s++ )
		{
			outCVXBuffer[ frameOffset + s ] = tmpBuffer[xOffset + s];
			outCVYBuffer[ frameOffset + s ] = tmpBuffer[yOffset + s];
		}
		return RealtimeMethodReturnCodeEnum.SUCCESS;
	}

	public void setDesiredX( final float newX )
	{
		this.desiredX = newX;
		xSad.notifyOfNewValue( newX );
	}

	public void setDesiredY( final float newY )
	{
		this.desiredY = newY;
		ySad.notifyOfNewValue( newY );
	}
}
