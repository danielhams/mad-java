package uk.co.modularaudio.service.apprenderingstructure;

import uk.co.modularaudio.service.rendering.RenderingPlan;

/**
 * @author dan
 *
 */
public interface HotspotRenderingContainer
{
	/**
	 * @param renderingPlan
	 */
	void startHotspotLooping( RenderingPlan renderingPlan );

	/**
	 *
	 */
	void stopHotspotLooping();
}
