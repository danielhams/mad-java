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

import java.awt.Color;
import java.awt.Component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.soundfile_player2.mu.SoundfilePlayer2MadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player2.mu.SoundfilePlayer2MadInstance;
import uk.co.modularaudio.service.audioanalysis.AnalysedData;
import uk.co.modularaudio.service.audioanalysis.AnalysisFillCompletionListener;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.mvc.rotarydisplay.models.SoundFileBpmRotaryDisplayModel;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayController;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel.ValueChangeListener;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants.StdRotaryViewColor;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayKnob.KnobType;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayView;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayView.SatelliteOrientation;

public class SoundfilePlayer2SongBpmDialUiJComponent
	implements IMadUiControlInstance<SoundfilePlayer2MadDefinition, SoundfilePlayer2MadInstance, SoundfilePlayer2MadUiInstance>,
	AnalysisFillCompletionListener
{
	private static Log log = LogFactory.getLog( SoundfilePlayer2SongBpmDialUiJComponent.class.getName() );

	private final SoundFileBpmRotaryDisplayModel model;
	private final RotaryDisplayView view;

	private static final float MIN_BPM_VALUE = 40.0f;
	private static final float MAX_BPM_VALUE = 240.0f;
	private static final float DEFAULT_BPM_VALUE = 127.0f;

	// Look into making this something in the preferences
	public static final float TARGET_PLAYER_DB = -12.0f;

	private static class GainDialColours extends StdRotaryViewColor
	{
		public GainDialColours()
		{
			this.labelColor = Color.black;
		}
	};

	private final static GainDialColours DC = new GainDialColours();

	public SoundfilePlayer2SongBpmDialUiJComponent( final SoundfilePlayer2MadDefinition definition,
			final SoundfilePlayer2MadInstance instance,
			final SoundfilePlayer2MadUiInstance uiInstance,
			final int controlIndex )
	{
		model = new SoundFileBpmRotaryDisplayModel(
				MIN_BPM_VALUE, MAX_BPM_VALUE,
				DEFAULT_BPM_VALUE, DEFAULT_BPM_VALUE );

		final RotaryDisplayController controller = new RotaryDisplayController( model );

		view = new RotaryDisplayView(
				model,
				controller,
				KnobType.BIPOLAR,
				SatelliteOrientation.LEFT,
				SatelliteOrientation.RIGHT,
				"SBPM:",
				DC,
				false,
				true );

		view.setDiameter( 27 );

		model.addChangeListener( new ValueChangeListener()
		{

			@Override
			public void receiveValueChange( final Object source, final float newValue )
			{
				uiInstance.setSongBpm( newValue );
			}
		} );

		uiInstance.addAnalysisFillListener( this );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}

	@Override
	public String getControlValue()
	{
		return Float.toString( model.getValue() );
	}

	@Override
	public void receiveControlValue( final String value )
	{
		model.setValue( this, Float.parseFloat( value ) );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters, final long currentGuiTime )
	{
	}

	@Override
	public Component getControl()
	{
		return view;
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public void receiveAnalysisBegin()
	{
	}

	@Override
	public void receivePercentageComplete( final int percentageComplete )
	{
	}

	@Override
	public void notifyAnalysisFailure()
	{
	}

	@Override
	public void receiveAnalysedData( final AnalysedData analysedData )
	{
		final float analysedBpm = analysedData.getBpm();

		if( log.isDebugEnabled() )
		{
			log.debug("Received analysed bpm: " + analysedBpm );
		}

		model.setValue( this, analysedBpm );
	}
}
