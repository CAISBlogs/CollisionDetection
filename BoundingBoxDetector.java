package com.tut.tutorial;

import com.tut.prerequesits.Entity;
import com.tut.prerequesits.Matrix4f;
import com.tut.prerequesits.Vector4f;

/**
 * 
 * Runs an algorithm to determine if any two given entities collided with each other.
 * The algorithm calculates a box that lies on the xyz axis and tightly contains every
 * vertex in the model. The boxes of the two entities are compared with eachother
 * to check for overlap. If there is an overlap a collision is assumed. Otherwise
 * no collision could possibly occur.
 * 
 * @author CAISBlogss
 *
 */

public class BoundingBoxDetector {

	
	/**
	 * Runs the AxisAlignedBoundingBox algorithm on the two entities, returning the collision
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
		
		float[] primaryBox = findAxisAlignedBoundingBox(primary);
		float[] secondaryBox = findAxisAlignedBoundingBox(secondary);
		return intersects(primaryBox, secondaryBox);
		
	}
	
	private static boolean intersects(float[] box1, float[] box2){
		
		if(box1[0] > box2[1]){
			return false;
		}
		if(box1[1] < box2[0]){
			return false;
		}
		
		if(box1[2] > box2[3]){
			return false;
		}
		if(box1[3] < box2[2]){
			return false;
		}
		
		if(box1[4] > box2[5]){
			return false;
		}
		if(box1[5] < box2[4]){
			return false;
		}
		
		return true;
		
	}
	
	
	/**
	 * Calculates the axis aligned bounding box containing all vertices and stores it as the maximum and minimum
	 * position on each axial plane. This method loads in all vertices for a given shape, converts them to WorldSpace,
	 * iterates through them to find each maximum and minimum value and returns the result.
	 * 
	 * @param e
	 * 			- The entity to extract the bounding box from.
	 * 
	 * @return 
	 * 			- The bounding box to be read as {minX, maxX, minY, maxY, minZ, maxZ}
	 */
	public static float[] findAxisAlignedBoundingBox(Entity e){
		
		float[] vertices = e.getVertices();
		Matrix4f transformation = Maths.createTransformationMatrix(e);
		Vector4f vertex = new Vector4f();
		
		
		float minX = 0;
		float maxX = 0;
		float minY = 0;
		float maxY = 0;
		float minZ = 0;
		float maxZ = 0;
		
		for(int i = 0; i < vertices.length/3; i++){
			
			int root = i*3;
			
			vertex.set(vertices[root], vertices[root + 1], vertices[root+2], 1);
			Matrix4f.transform(transformation, vertex, vertex);
			
			if(i == 0 ){
				
				minX = vertex.x;
				maxX = vertex.x;
				minY = vertex.y;
				maxY = vertex.y;
				minZ = vertex.z;
				maxZ = vertex.z;
				
				
			} else {
				
				if(vertex.x > maxX){
					maxX = vertex.x;
				}
				if(vertex.y > maxY){
					maxY = vertex.y;
				}
				if(vertex.z > maxZ){
					maxZ = vertex.z;
				}
				if(vertex.x < minX){
					minX = vertex.x;
				}
				if(vertex.y < minY){
					minY = vertex.y;
				}
				if(vertex.z < minZ){
					minZ = vertex.z;
				}
			}
			
			
			
			
		}
		
		return new float[]{minX, maxX, minY, maxY, minZ, maxZ};
		
		
	}
	
}
