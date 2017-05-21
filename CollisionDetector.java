package com.tut.tutorial;

import com.tut.prerequesits.Entity;

/**
 * 
 * This static class is a wrapper for all collision detectors and acts as
 * a simple interface with {@link BubbleDetector} and {@link BoundingBoxDetector}
 * 
 * @author CAISBlogss
 *
 */

public class CollisionDetector {
	
	
	/**
	 * Algorithm determines if two entities have collided - this uses the
	 * {@link BubbleDetector}'s algorithm.
	 * 
	 * @param primary
	 * 				- An entity to check collision. Order insignificant.
	 * @param secondary
	 *           	- Another entity to check collision. Order insignificant.
	 * 
	 * @return 
	 * 				- The collision state: true if there is a collision, false if there is no collision.
	 */
	public static boolean bubbleCollide(Entity primary, Entity secondary){
		return BubbleDetector.doesCollide(primary, secondary);
	}
	
	
	/**
	 * Algorithm determines if two entities have collided - this uses the
	 * {@link BoundingBoxDetector}'s algorithm.
	 * 
	 * @param primary
	 * 				- An entity to check collision. Order insignificant.
	 * @param secondary
	 *           	- Another entity to check collision. Order insignificant.
	 * 
	 * @return collisionState
	 */
	public static boolean boxCollide(Entity primary, Entity secondary){
		return BoundingBoxDetector.doesCollide(primary, secondary);
	}


}
