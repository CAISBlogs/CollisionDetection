package com.tut.tutorial;

import com.tut.prerequesits.Entity;
import com.tut.prerequesits.Vector3f;

/**
 * 
 * Runs an algorithm to determine if any two given entities collided with each other.
 * This algorithm generates a sphere that fully encompasses all space that could be occupied
 * by the entities in respect to their origin regardless of specific orientations. If the distance
 * between the origins in WorldSpace is greater than the sum of the radii of the spheres
 * there is no collision, otherwise we assume that there is.
 * 
 * @author CAISBlogss
 *
 */
public class BubbleDetector {
	
	
	
	/**
	 * Runs the bubble algorithm on the two entities, returning the collision
	 * state as a boolean value.
	 * 
	 * @param primary
	 * 				- An entity to check collision. Order insignificant.
	 * @param secondary
	 *           	- Another entity to check collision. Order insignificant.
	 * 
	 * @return 
	 * 				- The collision state: true if there is a collision, false if there is no collision.
	 */
	public static boolean doesCollide(Entity primary, Entity secondary){
		
		float furthestPrimary = findFurthestPoint(primary) * primary.getScale();
		float furthestSecondary = findFurthestPoint(secondary) * secondary.getScale();
		float distOrigin = Vector3f.sub(primary.getPosition(), secondary.getPosition(), null).length();
		return distOrigin <= furthestPrimary + furthestSecondary;
		
		
	}
	
	
	/**
	 * Calculates the length of the vertex farthest from the origin by iterating through all vertices and
	 * storing the longest length found.
	 * 
	 * Optimisation consideration: Any given entity has a set farthest point from origin that never changes -
	 * 		running the search every time can be slow when each entity can store the value and retrieve it when
	 * 		needed.
	 * 
	 * @param e
	 * 			- The entity to extract the farthest point from.
	 * 
	 * @return 
	 * 			- The length of the farthest point in respect to the origin
	 */
	private static float findFurthestPoint(Entity e){
		
		// These are the model vertices, used to render the shape. You should be able to retrieve them from your Entity.
		float[] vertices = e.getVertices();
		float longestDistance = 0;
		for(int i = 0; i < vertices.length/3; i++){
			
			int root = i*3;
			Vector3f vertex = new Vector3f(vertices[root], vertices[root+1], vertices[root+2]);
			float distance = vertex.length();
			if(distance > longestDistance){
				longestDistance = distance;
			}
			
		}
		
		return longestDistance;
		
		
	}
	


}
