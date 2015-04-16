package uk.co.modularaudio.service.audioanalysis;


public interface AnalysisFillCompletionListener
{
	void receiveAnalysisBegin();
	void receivePercentageComplete( int percentageComplete );
	void notifyAnalysisFailure();
	void receiveAnalysedData( AnalysedData analysedData );
}
