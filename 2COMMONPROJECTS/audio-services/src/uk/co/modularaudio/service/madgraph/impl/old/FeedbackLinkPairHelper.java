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

package uk.co.modularaudio.service.madgraph.impl.old;


public class FeedbackLinkPairHelper
{
//	
//	private ComponentService componentService = null;
//	private GraphService graphService = null;
//	private MadGraphInstance<?> graphImpl = null;
//	private ComponentType sinkComponentType = FeedbackLinkSinkComponentDefinition.getType();
//	private ComponentType sourceComponentType = FeedbackLinkSourceComponentDefinition.getType();
//	
//	private ComponentInstance sinkInstance = null;
//	private FeedbackLinkSinkComponentDefinition sinkDefinition = null;
//	private SinkChannelDefinition sinkDependencyChannelDefinition = null;
//	private SinkChannelInstance sinkDependencyChannelInstance = null;
//	private SinkChannelDefinition sinkChannelDefinition = null;
//	private SinkChannelInstance  sinkChannelInstance = null;
//	private FeedbackLinkSinkInstanceData sinkInstanceData = null;
//	
//	private ComponentInstance sourceInstance = null;
//	private FeedbackLinkSourceComponentDefinition sourceDefinition = null;
//	private SourceChannelDefinition sourceDependencyChannelDefinition = null;
//	private SourceChannelInstance sourceDependencyChannelInstance = null;
//	private SourceChannelDefinition sourceChannelDefinition = null;
//	private SourceChannelInstance sourceChannelInstance = null;
//	private FeedbackLinkSourceInstanceData sourceInstanceData = null;
//
//	private Link sinkToSourceLink = null;
//	
//	private Link originalLinkToReplace = null;
//	private Link replacementSinkLink = null;
//	private Link replacementSourceLink = null;
//	
//	public FeedbackLinkPairHelper( ComponentService componentService,
//			GraphService graphService,
//			GraphImpl graphImpl,
//			Link linkToReplaceWithFeedbackPair )
//	{
//		originalLinkToReplace = linkToReplaceWithFeedbackPair;
//		this.componentService = componentService;
//		this.graphService = graphService;
//		this.graphImpl = graphImpl;
//	}
//
//	public void insertComponentsInGraph()
//			throws DatastoreException, RecordNotFoundException, UnknownDataRateException, MAConstraintViolationException, AudioProcessingException
//	{
//		String sinkNameToUse = graphService.getNameForNewComponentOfType( graphImpl, sinkComponentType );
//		sinkInstance = componentService.createComponentInstanceByType( sinkComponentType, sinkNameToUse );
//		sinkDefinition = (FeedbackLinkSinkComponentDefinition)sinkInstance.getComponentDefinition();
//		sinkDependencyChannelDefinition = sinkDefinition.findSinkAudioChannelByIndex( sinkDefinition.SINK_DEPENDENCY_CHAN_IDX );
//		sinkDependencyChannelInstance = (SinkChannelInstance) sinkInstance.getChannelInstance( sinkDependencyChannelDefinition );
//		sinkChannelDefinition = sinkDefinition.findSinkAudioChannelByIndex( sinkDefinition.SINK_CHAN_IDX );
//		sinkChannelInstance = (SinkChannelInstance) sinkInstance.getChannelInstance( sinkChannelDefinition );
//		sinkInstanceData = (FeedbackLinkSinkInstanceData) sinkInstance.getInstanceData();
//		
//		// Don't emit any graph changed signals
//		graphImpl.internalAddComponent( sinkInstance );
//		
//		String sourceNameToUse = graphService.getNameForNewComponentOfType( graphImpl, sourceComponentType );
//		sourceInstance = componentService.createComponentInstanceByType( sourceComponentType, sourceNameToUse );
//		sourceDefinition = (FeedbackLinkSourceComponentDefinition)sourceInstance.getComponentDefinition();
//		sourceDependencyChannelDefinition = sourceDefinition.findSourceAudioChannelByIndex( sourceDefinition.SOURCE_DEPENDENCY_CHAN_IDX );
//		sourceDependencyChannelInstance = (SourceChannelInstance) sourceInstance.getChannelInstance( sourceDependencyChannelDefinition );
//		sourceChannelDefinition = sourceDefinition.findSourceAudioChannelByIndex( sourceDefinition.SOURCE_CHAN_IDX );
//		sourceChannelInstance = (SourceChannelInstance) sourceInstance.getChannelInstance( sourceChannelDefinition );
//		sourceInstanceData = (FeedbackLinkSourceInstanceData)sourceInstance.getInstanceData();
//		
//		// Don't emit any graph changed signal
//		graphImpl.internalAddComponent( sourceInstance );
//		
//		// Create the necessary shared data between the two - we create a big ring buffer whilst should be much larger than
//		// is necessary
//		LocklessAudioRingBuffer sharedRingBuffer = new LocklessAudioRingBuffer( DataRate.SR_44100, 200, 1 );
//		sinkInstanceData.setRingBuffer( sharedRingBuffer );
//		sourceInstanceData.setRingBuffer( sharedRingBuffer );
//		
//		sinkToSourceLink = new Link( sourceInstance, sourceDependencyChannelInstance, sinkInstance, sinkDependencyChannelInstance );
//		
//		graphImpl.internalAddLink( sinkToSourceLink );
//		
//		// Finally add the two links to the original components
//		replacementSinkLink = new Link( sourceInstance,
//				sourceChannelInstance,
//				originalLinkToReplace.getSinkComponentInstance(),
//				originalLinkToReplace.getSinkChannelInstance() );
//		
//		graphImpl.internalAddLink( replacementSinkLink );
//		
//		replacementSourceLink = new Link( originalLinkToReplace.getSourceComponentInstance(),
//				originalLinkToReplace.getSourceChannelInstance(),
//				sinkInstance,
//				sinkChannelInstance );
//		
//		// The final link using the normal add link - this should give us the normal fading in
//		graphImpl.addLink( replacementSourceLink );
//	}
//	
//	public void removeComponentsFromGraph() throws DatastoreException, RecordNotFoundException
//	{
//		// Remove the source link with a "normal" delete link - this should give a fade out
//		graphImpl.deleteLink( replacementSourceLink );
//		
//		// Now remove the others with the internal delete with a final graph change event at the end.
//		graphImpl.internalDeleteLink( replacementSinkLink );
//		graphImpl.internalDeleteLink( sinkToSourceLink );
//		// Now the two components used
//		graphImpl.internalDeleteComponent( sourceInstance );
//		graphImpl.internalDeleteComponent( sinkInstance );
//		
//		graphImpl.fireGraphChanged();
//	}
}
