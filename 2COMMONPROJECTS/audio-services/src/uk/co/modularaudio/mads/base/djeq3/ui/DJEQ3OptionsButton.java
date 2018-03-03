package uk.co.modularaudio.mads.base.djeq3.ui;

import java.awt.Component;

import uk.co.modularaudio.mads.base.djeq3.mu.DJEQ3MadDefinition;
import uk.co.modularaudio.mads.base.djeq3.mu.DJEQ3MadInstance;
import uk.co.modularaudio.mads.base.djeq3.ui.crossfreqdiag.EQCrossoverFreqDialog;
import uk.co.modularaudio.mads.base.djeq3.ui.crossfreqdiag.EQCrossoverFreqDialogCallback;
import uk.co.modularaudio.mads.base.djeq3.ui.crossfreqdiag.EQCrossoverPresetChoiceUiJComponent;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.lang.StringUtils;
import uk.co.modularaudio.util.swing.imagebutton.LWTCOptionsButton;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;

public class DJEQ3OptionsButton extends LWTCOptionsButton
implements IMadUiControlInstance<DJEQ3MadDefinition,DJEQ3MadInstance,DJEQ3MadUiInstance>
{
	private static final long serialVersionUID = -3538983202783891265L;

	private final DJEQ3MadUiInstance uiInstance;
	private float lowerFreq = EQCrossoverPresetChoiceUiJComponent.PresetChoice.DJ_EQ_BANDS1.getLowFreq();
	private float upperFreq = EQCrossoverPresetChoiceUiJComponent.PresetChoice.DJ_EQ_BANDS1.getHighFreq();

	public DJEQ3OptionsButton( final DJEQ3MadDefinition definition,
			final DJEQ3MadInstance instance,
			final DJEQ3MadUiInstance uiInstance,
			final int controlIndex )
	{
		super( LWTCControlConstants.STD_BUTTON_COLOURS, true );
		this.uiInstance = uiInstance;
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters, final int U_currentGuiTime, final int framesSinceLastTick )
	{
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

	@Override
	public void receiveClick()
	{
		final EQCrossoverFreqDialog coFreqDialog = new EQCrossoverFreqDialog();
		final String valueForDialog = getControlValue();
		final EQCrossoverFreqDialogCallback callback = new EQCrossoverFreqDialogCallback()
		{
			@Override
			public void dialogClosedReceiveValues( final String valueOrNull )
			{
				coFreqDialog.dispose();
				internalSetValues( valueOrNull );
			}
		};
		coFreqDialog.setValues(
				this,
				"Some message",
				"Some title",
				valueForDialog,
				callback );
		coFreqDialog.setVisible( true );
	}

	@Override
	public String getControlValue()
	{
		return Float.toString( lowerFreq ) + ":" + Float.toString( upperFreq );
	}

	@Override
	public void receiveControlValue( final String strValue )
	{
		internalSetValues( strValue );
	}

	private void internalSetValues( final String valueOrNull )
	{
		if( !StringUtils.isEmpty(valueOrNull) )
		{
			final String[] vals = valueOrNull.split( ":" );
			final float min = Float.parseFloat(vals[0]);
			final float max = Float.parseFloat(vals[1]);
			internalSetValues( min, max );
		}
	}

	private void internalSetValues( final float min, final float max )
	{
		uiInstance.setNewCrossoverFrequencies( min, max );
		lowerFreq = min;
		upperFreq = max;
	}
}
