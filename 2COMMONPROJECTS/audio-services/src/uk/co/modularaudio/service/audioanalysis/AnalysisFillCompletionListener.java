package uk.co.modularaudio.service.audioanalysis;


public interface AnalysisFillCompletionListener
{

	void receiveAnalysedData( AnalysedData analysedData );

	void notifyAnalysisFailure();

	void receivePercentageComplete( int percentageComplete );

}
