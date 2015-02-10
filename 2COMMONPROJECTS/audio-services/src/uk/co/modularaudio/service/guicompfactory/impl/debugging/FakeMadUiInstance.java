package uk.co.modularaudio.service.guicompfactory.impl.debugging;

import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiChannelInstance;
import uk.co.modularaudio.util.audio.gui.mad.MadUiDefinition;
import uk.co.modularaudio.util.audio.mad.ioqueue.IOQueueEvent;
import uk.co.modularaudio.util.table.Span;

class FakeMadUiInstance extends AbstractMadUiInstance<FakeMadDefinition, FakeMadInstance>
{
	private final FakeMadInstance mi;
	private final FakeMadUiDefinition uiDef;

	public FakeMadUiInstance( final FakeMadInstance mi, final FakeMadUiDefinition uiDef )
	{
		super( mi, uiDef );
		this.mi = mi;
		this.uiDef = uiDef;
	}

	@Override
	public Span getCellSpan()
	{
		return FakeRackComponent.SPAN;
	}

	@Override
	public FakeMadInstance getInstance()
	{
		return mi;
	}

	@Override
	public MadUiDefinition<FakeMadDefinition, FakeMadInstance> getUiDefinition()
	{
		return uiDef;
	}

	@Override
	public AbstractMadUiControlInstance<?, ?, ?>[] getUiControlInstances()
	{
		return new AbstractMadUiControlInstance<?,?,?>[0];
	}

	@Override
	public MadUiChannelInstance[] getUiChannelInstances()
	{
		return new MadUiChannelInstance[0];
	}

	@Override
	public void receiveComponentNameChange( final String newName )
	{
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public void consumeQueueEntry( final FakeMadInstance instance, final IOQueueEvent nextOutgoingEntry )
	{
	}
}