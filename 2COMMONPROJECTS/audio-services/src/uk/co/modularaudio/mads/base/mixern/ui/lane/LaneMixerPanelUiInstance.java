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

package uk.co.modularaudio.mads.base.mixern.ui.lane;

import java.awt.Color;

import javax.swing.JComponent;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.mads.base.mixern.mu.MixerNMadDefinition;
import uk.co.modularaudio.mads.base.mixern.mu.MixerNMadInstance;
import uk.co.modularaudio.mads.base.mixern.ui.MixerNMadUiDefinition;
import uk.co.modularaudio.mads.base.mixern.ui.MixerNMadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.math.DbToLevelComputer;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class LaneMixerPanelUiInstance<D extends MixerNMadDefinition<D,I>,
		I extends MixerNMadInstance<D,I>,
		U extends MixerNMadUiInstance<D,I>>
	extends PacPanel
	implements IMadUiControlInstance<D,I,U>,
	AmpSliderChangeReceiver, MeterValueReceiver, PanSliderChangeReceiver
{
//	private static Log log = LogFactory.getLog( ChannelLaneMixerPanelUiInstance.class.getName() );

	private static final long serialVersionUID = -3862457210177904367L;

	private final int laneNumber;

	private final AmpSliderAndMeter<D,I> ampSliderAndMeter;
	private final PanSlider panSlider;
	private final AmpMuteSolo<D,I,U> ampMuteSolo;

	private final DbToLevelComputer dBToLevelComputer;

	private final U uiInstance;

	public LaneMixerPanelUiInstance( final D definition,
			final I instance,
			final U uiInstance,
			final int controlIndex )
	{
		this.setOpaque( true );
		this.setBackground( MixerNMadUiDefinition.LANE_BG_COLOR );
		this.uiInstance = uiInstance;

		laneNumber = controlIndex - 1;

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addRowConstraint( "[grow 0][fill][grow 0]" );
		msh.addLayoutConstraint( "insets 0" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );
//		msh.addLayoutConstraint( "debug" );
		final MigLayout compLayout = msh.createMigLayout();
		this.setLayout( compLayout );

		panSlider = new PanSlider( this, Color.WHITE );
		this.add( panSlider, "gap 4px 4px, growx, growy 0, wrap" );

		ampSliderAndMeter = new AmpSliderAndMeter<D,I>( uiInstance,
				uiInstance.getUiDefinition().getBufferedImageAllocator(),
				true,
				Color.WHITE );
		this.add( ampSliderAndMeter, "grow, wrap" );
		dBToLevelComputer = ampSliderAndMeter.getDbToLevelComputer();

		ampMuteSolo = new AmpMuteSolo<D,I,U>( this );
		this.add( ampMuteSolo, "growx, growy 0" );

		ampSliderAndMeter.setChangeReceiver( this );

		uiInstance.registerLaneMeterReceiver( laneNumber, this );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
	{
		ampSliderAndMeter.receiveDisplayTick( currentGuiTime );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void receiveAmpSliderChange( final float newValue )
	{
		// Now translate this into dB and then into amplitude
		final float dbForValue = dBToLevelComputer.toDbFromNormalisedLevel( newValue );
//		log.debug("Using db " + dbForValue );
		final float ampForDb = (float)AudioMath.dbToLevel( dbForValue );
		uiInstance.sendLaneAmp( laneNumber, ampForDb );
	}

	@Override
	public String getControlValue()
	{
		return ampSliderAndMeter.getControlValue() + ":" + panSlider.getControlValue() + ":" + ampMuteSolo.getControlValue();
	}

	@Override
	public void receiveControlValue( final String value )
	{
		final String[] vals = value.split( ":" );
		if( vals.length > 0 )
		{
			ampSliderAndMeter.receiveControlValue( vals[0] );
		}
		if( vals.length > 1 )
		{
			panSlider.receiveControlValue( vals[1] );
		}
		if( vals.length > 2 )
		{
			ampMuteSolo.receiveControlValue( vals[2] );
		}
	}

	@Override
	public void receiveMeterReadingLevel( final long currentTimestamp, final int channelNum, final float meterReadingLevel )
	{
		final float meterReadingDb = (float)AudioMath.levelToDb( meterReadingLevel );
		ampSliderAndMeter.receiveMeterReadingInDb( currentTimestamp, channelNum, meterReadingDb );
	}

	public void setMuteValue( final boolean muteValue )
	{
//		log.debug("Lane " + laneNumber + " setting mute (" + muteValue + ")");
		uiInstance.sendLaneMute( laneNumber, muteValue );
	}

	public void setSoloValue( final boolean soloValue )
	{
		uiInstance.sendSoloValue( laneNumber, soloValue );
	}

	@Override
	public void receiveMuteSet( final long currentTimestamp, final boolean muted )
	{
//		log.debug("Lane " + laneNumber + " received mute set(" + muted + ")");
		ampMuteSolo.receiveMuteSet( muted );
	}

	@Override
	public void receiveSoloSet( final long currentTimestamp, final boolean solod )
	{
		ampMuteSolo.receiveSoloSet( solod );
	}

	@Override
	public void destroy()
	{
		ampSliderAndMeter.destroy();
	}

	@Override
	public void receivePanChange( final float panValue )
	{
		uiInstance.sendLanePan( laneNumber, panValue );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}

}
