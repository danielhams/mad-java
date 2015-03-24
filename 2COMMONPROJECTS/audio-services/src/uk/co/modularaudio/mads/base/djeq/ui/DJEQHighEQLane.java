package uk.co.modularaudio.mads.base.djeq.ui;

import uk.co.modularaudio.mads.base.djeq.mu.DJEQMadDefinition;
import uk.co.modularaudio.mads.base.djeq.mu.DJEQMadInstance;
import uk.co.modularaudio.mads.base.djeq.ui.OneEqKill.ToggleListener;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel.ValueChangeListener;


public class DJEQHighEQLane extends DJEQOneEqLane implements ValueChangeListener
{
	private static final long serialVersionUID = 4164721930545400401L;

	private final DJEQMadUiInstance uiInstance;

	public DJEQHighEQLane( final DJEQMadDefinition definition,
			final DJEQMadInstance instance,
			final DJEQMadUiInstance uiInstance,
			final int controlIndex )
	{
		super( definition, instance, uiInstance, controlIndex, "High" );

		this.uiInstance = uiInstance;

		getKnob().getModel().addChangeListener( this );

		getKill().setToggleListener( new ToggleListener()
		{

			@Override
			public void receiveToggleChange( final boolean previousValue, final boolean newValue )
			{
				uiInstance.setHighKilled( newValue );
			}
		} );

	}

	@Override
	public void receiveValueChange( final Object source, final float newValue )
	{
		final float actualValue = AudioMath.dbToLevelF( newValue );
		uiInstance.setHighAmp( actualValue );
	}
}
