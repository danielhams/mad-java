package uk.co.modularaudio.componentdesigner;

import org.springframework.context.support.GenericApplicationContext;

import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.spring.BeanInstantiationListAsPostProcessor;
import uk.co.modularaudio.util.spring.SpringContextHelper;

public class PostRefreshSetMadReleaseLevelContextHelper implements SpringContextHelper
{
	private final boolean showAlpha;
	private final boolean showBeta;

	public PostRefreshSetMadReleaseLevelContextHelper( final boolean showAlpha, final boolean showBeta )
	{
		this.showAlpha = showAlpha;
		this.showBeta = showBeta;
	}

	@Override
	public void preContextDoThings() throws DatastoreException
	{
	}

	@Override
	public void preRefreshDoThings( final GenericApplicationContext appContext ) throws DatastoreException
	{
	}

	@Override
	public void postRefreshDoThings( final GenericApplicationContext appContext,
			final BeanInstantiationListAsPostProcessor beanInstantiationList ) throws DatastoreException
	{
		final MadComponentService componentService = appContext.getBean( MadComponentService.class );
		componentService.setReleaseLevel( showAlpha, showBeta );
	}

	@Override
	public void preShutdownDoThings( final GenericApplicationContext appContext,
			final BeanInstantiationListAsPostProcessor beanInstantiationList ) throws DatastoreException
	{
	}

}
