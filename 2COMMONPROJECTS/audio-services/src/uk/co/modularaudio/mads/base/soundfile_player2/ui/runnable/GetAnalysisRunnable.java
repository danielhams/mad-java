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

package uk.co.modularaudio.mads.base.soundfile_player2.ui.runnable;

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
