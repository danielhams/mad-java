package uk.co.modularaudio.mads.base.soundfile_player.ui.runnable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.controller.advancedcomponents.AdvancedComponentsFrontController;
import uk.co.modularaudio.service.audioanalysis.AnalysedData;
import uk.co.modularaudio.service.audioanalysis.AnalysisFillCompletionListener;
import uk.co.modularaudio.service.library.LibraryEntry;

public class GetAnalysisRunnable implements Runnable
{
	private static Log log = LogFactory.getLog( GetAnalysisRunnable.class.getName() );

	private final AdvancedComponentsFrontController acfc;
	private final LibraryEntry libraryEntry;
	private final AnalysisFillCompletionListener analysisListener;

	public GetAnalysisRunnable( final AdvancedComponentsFrontController advancedComponentsFrontController,
			final LibraryEntry libraryEntry,
			final AnalysisFillCompletionListener analysisListener )
	{
		this.acfc = advancedComponentsFrontController;
		this.libraryEntry = libraryEntry;
		this.analysisListener = analysisListener;
	}

	@Override
	public void run()
	{
		try
		{
			final AnalysedData analysedData = acfc.registerForLibraryEntryAnalysis( libraryEntry, analysisListener );
			analysisListener.receiveAnalysedData( analysedData );
		}
		catch ( final Exception e )
		{
			final String msg = "Exception caught during fetch of library entry analysis: " + e.toString();
			log.error( msg, e );
			analysisListener.notifyAnalysisFailure();
		}
	}

}
