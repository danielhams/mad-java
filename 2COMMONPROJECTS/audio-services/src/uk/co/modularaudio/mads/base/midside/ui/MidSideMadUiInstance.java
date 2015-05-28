package uk.co.modularaudio.mads.base.midside.ui;

import uk.co.modularaudio.mads.base.midside.mu.MidSideIOQueueBridge;
import uk.co.modularaudio.mads.base.midside.mu.MidSideMadDefinition;
import uk.co.modularaudio.mads.base.midside.mu.MidSideMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.helper.NoEventsNoNameChangeNonConfigurableMadUiInstance;

public class MidSideMadUiInstance extends
		NoEventsNoNameChangeNonConfigurableMadUiInstance<MidSideMadDefinition, MidSideMadInstance>
{
	public MidSideMadUiInstance( final MidSideMadInstance instance, final MidSideMadUiDefinition uiDefinition )
	{
		super( uiDefinition.getCellSpan(), instance, uiDefinition );
	}

	public void setMidSideType( final boolean isLrToMs )
	{
		sendTemporalValueToInstance( MidSideIOQueueBridge.COMMAND_IN_MS_TYPE, isLrToMs ? 1 : 0 );

	}
}
