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

package uk.co.modularaudio.mads.base.mixer.ui;

import java.awt.Color;

import javax.swing.JComponent;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.mads.base.mixer.mu.MixerMadDefinition;
import uk.co.modularaudio.mads.base.mixer.mu.MixerMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacPanel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.math.DbToLevelComputer;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class ChannelLaneMixerPanelUiInstance extends PacPanel
	implements IMadUiControlInstance<MixerMadDefinition, MixerMadInstance, MixerMadUiInstance>,
	AmpSliderChangeReceiver, MeterValueReceiver, PanSliderChangeReceiver
{
//	private static Log log = LogFactory.getLog( ChannelLaneMixerPanelUiInstance.class.getName() );

	private static final long serialVersionUID = -3862457210177904367L;

	private int laneNumber = -1;

	private AmpSliderAndMeter ampSliderAndMeter = null;
	private PanSlider panSlider = null;
	private AmpMuteSolo ampMuteSolo = null;

	private DbToLevelComputer dBToLevelComputer = null;

	private MixerMadUiInstance uiInstance = null;

	public ChannelLaneMixerPanelUiInstance( MixerMadDefinition definition,
			MixerMadInstance instance,
			MixerMadUiInstance uiInstance,
			int controlIndex )
	{
		this.setOpaque( true );
		this.setBackground( MixerMadUiDefinition.LANE_BG_COLOR );
		this.uiInstance = uiInstance;

		laneNumber = controlIndex - 1;

		MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addRowConstraint( "[grow 0][fill][grow 0]" );
		msh.addLayoutConstraint( "insets 0" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );
//		msh.addLayoutConstraint( "debug" );
		MigLayout compLayout = msh.createMigLayout();
		this.setLayout( compLayout );

		panSlider = new PanSlider( this, Color.WHITE );
		this.add( panSlider, "gap 4px 4px, growx, growy 0, wrap" );

		ampSliderAndMeter = new AmpSliderAndMeter( uiInstance,
				uiInstance.getUiDefinition().getBufferedImageAllocator(),
				true,
				Color.WHITE );
		this.add( ampSliderAndMeter, "grow, wrap" );
		dBToLevelComputer = ampSliderAndMeter.getDbToLevelComputer();

		ampMuteSolo = new AmpMuteSolo( this );
		this.add( ampMuteSolo, "growx, growy 0" );

		ampSliderAndMeter.setChangeReceiver( this );

		uiInstance.registerLaneMeterReceiver( laneNumber, this );
	}

	@Override
	public void doDisplayProcessing( ThreadSpecificTemporaryEventStorage tempEventStorage,
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
	public void receiveAmpSliderChange( float newValue )
	{
		// Now translate this into dB and then into amplitude
		float dbForValue = dBToLevelComputer.toDbFromNormalisedLevel( newValue );
//		log.debug("Using db " + dbForValue );
		float ampForDb = (float)AudioMath.dbToLevel( dbForValue );
		uiInstance.sendLaneAmp( laneNumber, ampForDb );
	}

	@Override
	public String getControlValue()
	{
		return ampSliderAndMeter.getControlValue() + ":" + panSlider.getControlValue() + ":" + ampMuteSolo.getControlValue();
	}

	@Override
	public void receiveControlValue( String value )
	{
		String[] vals = value.split( ":" );
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
	public void receiveMeterReadingLevel( long currentTimestamp, int channelNum, float meterReadingLevel )
	{
		float meterReadingDb = (float)AudioMath.levelToDb( meterReadingLevel );
		ampSliderAndMeter.receiveMeterReadingInDb( currentTimestamp, channelNum, meterReadingDb );
	}

	public void setMuteValue( boolean muteValue )
	{
//		log.debug("Lane " + laneNumber + " setting mute (" + muteValue + ")");
		uiInstance.sendLaneMute( laneNumber, muteValue );
	}

	public void setSoloValue( boolean soloValue )
	{
		uiInstance.sendSoloValue( laneNumber, soloValue );
	}

	@Override
	public void receiveMuteSet( long currentTimestamp, boolean muted )
	{
//		log.debug("Lane " + laneNumber + " received mute set(" + muted + ")");
		ampMuteSolo.receiveMuteSet( muted );
	}

	@Override
	public void receiveSoloSet( long currentTimestamp, boolean solod )
	{
		ampMuteSolo.receiveSoloSet( solod );
	}

	@Override
	public void destroy()
	{
		ampSliderAndMeter.destroy();
	}

	@Override
	public void receivePanChange( float panValue )
	{
		uiInstance.sendLanePan( laneNumber, panValue );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}

}
