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

import java.awt.Color;
import java.awt.Component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadInstance;
import uk.co.modularaudio.service.audioanalysis.AnalysedData;
import uk.co.modularaudio.service.audioanalysis.AnalysisFillCompletionListener;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.mvc.rotarydisplay.models.SoundFileGainRotaryDisplayModel;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayController;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel.ValueChangeListener;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants.StdRotaryViewColor;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayKnob.KnobType;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayView;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayView.SatelliteOrientation;

public class SoundfilePlayerGainDialUiJComponent
	implements IMadUiControlInstance<SoundfilePlayerMadDefinition, SoundfilePlayerMadInstance, SoundfilePlayerMadUiInstance>,
	AnalysisFillCompletionListener
{
	private static Log log = LogFactory.getLog( SoundfilePlayerGainDialUiJComponent.class.getName() );

	private final SoundFileGainRotaryDisplayModel model;
	private final RotaryDisplayView view;

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

	public SoundfilePlayerGainDialUiJComponent( final SoundfilePlayerMadDefinition definition,
			final SoundfilePlayerMadInstance instance,
			final SoundfilePlayerMadUiInstance uiInstance,
			final int controlIndex )
	{
		model = new SoundFileGainRotaryDisplayModel(
				-SoundfilePlayerMadInstance.GAIN_MAX_DB,
				SoundfilePlayerMadInstance.GAIN_MAX_DB,
				0.0f,
				0.0f );

		final RotaryDisplayController controller = new RotaryDisplayController( model );

		view = new RotaryDisplayView(
				model,
				controller,
				KnobType.BIPOLAR,
				SatelliteOrientation.LEFT,
				SatelliteOrientation.RIGHT,
				"Gain:",
				DC,
				false,
				true );

		view.setDiameter( 27 );

		model.addChangeListener( new ValueChangeListener()
		{

			@Override
			public void receiveValueChange( final Object source, final float newValue )
			{
				uiInstance.sendGain( AudioMath.dbToLevelF( newValue ) );
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
		final float absPeakDb = analysedData.getAbsPeakDb();
//		final float averageRmsDb = analysedData.getRmsAverageDb();
		final float peakRmsDb = analysedData.getRmsPeakDb();

		if( log.isDebugEnabled() )
		{
			log.debug("Received analysed data: " + analysedData.toString() );
		}
		float adjustmentDb = TARGET_PLAYER_DB - peakRmsDb;

		// Check we're not clipping due to the adjustment. If we are,
		// set the gain to unity
		float clippingAmount = absPeakDb + adjustmentDb;
		if( clippingAmount > 0.0f )
		{
			// Add a wee bit of headroom
			clippingAmount += 1.0f;
			if( log.isDebugEnabled() )
			{
				log.debug("Gain adjustment would introduce clipping. Bringing back by " + clippingAmount );
			}
			adjustmentDb -= clippingAmount;
		}

		model.setValue( this, adjustmentDb );
	}
}
