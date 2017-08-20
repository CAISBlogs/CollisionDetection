package com.collision.main;

import com.collision.utils.Maths;
import com.collision.utils.Matrix4f;
import com.collision.utils.Vector3f;

public class FauerbyDetector {
	
	/**
	 * The collision detection engine, will return true if the two entities share an intersection
	 * 
	 * The rough idea of this method is as follows:
	 * 
	 * If primary entity "e" is a unit sphere* (with centre "c"), travelling with some velocity "v" then an intersection with secondary entity "e'"
	 * will be true if any of the following conditions are true:
	 * 
	 * > A point between c --> c+v is 1 unit from any plane in e' and within the triangle used to define e'
	 * > A point between c --> c+v is 1 unit from any vertex in e'
	 * > A point between c --> c+v is 1 unit from any edge of any shape in e'
	 * 
	 * any of these cases may be true, and any may be true exclusively, so all are tested.
	 * 
	 * we may exclude any face that the entire region c --> c+v is greater than 1 (or less than -1 as signed distances are used)
	 * 
	 * 
	 * @param primary The first entity to test
	 * @param velocity The velocity of the entity
	 * @param secondary The second entity to test
	 * @return
	 */
	public static boolean doesCollide(Entity primary, Vector3f velocity, Entity secondary) {
		
		
		// The box that surrounds the first entity, from BoundBoxDetector
		float[] boundingBox = BoundBoxDetector.findAxisAlignedBoundingBox(primary);
		
		// Take the unit sphere that can be used ot represent the box
		Vector3f spheroidSpace = calculateSpheroid(boundingBox);
		
		// The center of the unit sphere
		Vector3f primaryPosition = calculatePosition(boundingBox);
		
		// From this point onwards were are going to be working in unit sphere space, to do this we multiply all locations in worldSpace by the sheroidSpace vector
		
		primaryPosition = new Vector3f(primaryPosition.x * spheroidSpace.x,
										primaryPosition.y * spheroidSpace.y,
										primaryPosition.z * spheroidSpace.z);
		
		// Veclocity also needs to be in spheroid space
		velocity = new Vector3f(velocity.x * spheroidSpace.x,
									velocity.y * spheroidSpace.y,
									velocity.z * spheroidSpace.z);
		
		
		
		Vector3f[] secondaryShape = verticesToSpheroidWorldSpace(secondary, spheroidSpace);
		
		int[] indices = secondary.getIndices();
		
		
		for(int i = 0; i < indices.length/3; i++){
			
			float t0 = 0;
			float t1 = 1;
			
			// Take the infinite plane of the side of the second shape
			Plane collisionPlane = new Plane(secondaryShape[indices[3 * i]], secondaryShape[indices[(3 * i) + 1]], secondaryShape[indices[(3 * i) + 2]]);
			
			
			// This is the case if and only if the object is moving directly parallel to the plane
			if(Vector3f.dot(collisionPlane.normal, velocity) == 0){
				// If this is false then the object is embedded in the plane (and my be colliding), if it is true the objects CANNOT intersect
				if(collisionPlane.signedDistanceTo(primaryPosition) > 1){
					continue;
				}
			} else {
				
				// Test for planar intersection is fairly simple, we take the position of the primary entity at the start of the move, and again at the end,
				// If at any point over this time the entity is 1 unit from the plane it has collided
				t0 = (1f - collisionPlane.signedDistanceTo(primaryPosition)) / (Vector3f.dot(collisionPlane.normal, velocity));
				t1 = ((-1f) - collisionPlane.signedDistanceTo(primaryPosition)) / (Vector3f.dot(collisionPlane.normal, velocity));
				
				
				if(t0 > t1){
					float temp = t0;
					t0 = t1;
					t1 = temp;
				}
				// We want t1 to be the larger number
			}
			if(inRange(t0, 0, 1) || inRange(t1, 0, 1)){
				
				float distance = 99999; // Arbitrarily large number, can be done in other ways (not perfect)
				Vector3f distanceTraveled = new Vector3f(velocity.x * t0,
															velocity.y * t0,
															velocity.z * t0);
				Vector3f planeIntersectionPoint = Vector3f.sub(Vector3f.add(primaryPosition ,distanceTraveled, null),  collisionPlane.normal, null);
				
				
				// THIS IS IMPORTANT: The collision with the plane does NOT mean that a collision has occurred, it must ACTUALLY collide with the triangle on the plane.
				if(collisionPlane.inTriangle(planeIntersectionPoint, secondaryShape[indices[3 * i]], secondaryShape[indices[(3 * i) + 1]], secondaryShape[indices[(3 * i) + 2]])){

					distance = t0;
					return true;
					
					
				} else {
					
					/**
					 * The following code section is long and difficult to follow, it reduces the collision equation to a quadratic formula and tests if the roots are within
					 * an adequate range. I would strongly advice reading the paper at http://www.peroxide.dk/papers/collision/collision.pdf to get a better intuition
					 * of how this is done
					 */
					
					boolean edgehit = false;
					boolean vertex = false;
					
					for(int j = 0; j < 3; j++){ // <- For each vertex in the shape
						float velocitySquared = Vector3f.dot(velocity, velocity);
						float traveledDistance = 2 * Vector3f.dot(velocity, Vector3f.sub(primaryPosition, secondaryShape[indices[(3 * i) + j]], null));
						Vector3f vertexDistance = Vector3f.sub(secondaryShape[indices[(3 * i) + j]], primaryPosition, null);
						float squareDistanceMinusOne = Vector3f.dot(vertexDistance, vertexDistance) - 1;
						float x1 = getLowestRoot(velocitySquared, traveledDistance, squareDistanceMinusOne, t1);
						if(x1 >= 0){

							distance = x1 * velocity.length();
							planeIntersectionPoint = secondaryShape[indices[(3 * i) + j]];
							vertex = true;

						}
					}
					for(int j = 0; j < 3; j++){  // <- for each edge in the triangle
						Vector3f edge = Vector3f.sub(secondaryShape[indices[(3 * i) + (j%2)]], secondaryShape[indices[(3 * i) + j]], null);
						Vector3f baseToVertex = Vector3f.sub(secondaryShape[indices[(3 * i) + j]], primaryPosition, null);
						float distanceFromEdge = Vector3f.dot(edge, edge) * (-Vector3f.dot(velocity, velocity)) + ((float) Math.pow(Vector3f.dot(edge, velocity), 2));
						float intersection = Vector3f.dot(edge, edge) * (2 * (Vector3f.dot(velocity, baseToVertex))) - (2 * (Vector3f.dot(edge, velocity) * Vector3f.dot(edge, baseToVertex)));
						float difference = Vector3f.dot(edge, edge) * (1 - Vector3f.dot(baseToVertex, baseToVertex)) + ((float) Math.pow(Vector3f.dot(edge, baseToVertex), 2));
						float x1 = getLowestRoot(distanceFromEdge, intersection, difference, t1);
						if(x1 >= 0){
							
							// A collision is detected with the infine 3d Vector that the line falls on, we must check if this collision is between the two endpoints of the line
							float f0 = ((Vector3f.dot(edge, velocity) * x1) - Vector3f.dot(edge, baseToVertex)) / Vector3f.dot(edge,edge);
							if(inRange(f0, 0, 1)){
								
								float d = x1 * velocity.length();
								if(d < distance){
									distance = d;
									planeIntersectionPoint = secondaryShape[indices[(3 * i) + j]];
									edgehit = true;
								}
								
								
							}
						}
					}
					
					if(edgehit){
						return true;
					} else if (vertex){
						return true;

					}
				}
				
				
			} else {
				continue;
			}
			
			
			
		}
		
		return false;
		
	}

	/**
	 * Simple test, will return true if the value falls between the two integers provided.
	 * 
	 * It is assumed that the values are in order already
	 * 
	 * @param value the value to test
	 * @param min the lowest bound
	 * @param max the highest bound
	 * @return true if the value is between the points, false if else
	 */
	private static boolean inRange(float value, int min, int max) {

		return(value >= min && value <= max);
	}

	
	/**
	 * Converts the vertices of a given entity to fall within a given translation of 3D space provided by the vector
	 * 
	 * @param e  The entity to test
	 * @param spheroidSpace a vector containing a translation of 3d space
	 * @return
	 */
	private static Vector3f[] verticesToSpheroidWorldSpace(Entity e, Vector3f spheroidSpace) {
		
		float[] vertices = e.getVertices();
		Vector3f[] vecs = new Vector3f[vertices.length/3];
		Matrix4f transformation = Maths.createTransformationMatrix(e);
		
		for(int i = 0; i < vertices.length/3; i++){
			Vector3f worldPosition =  Matrix4f.transform(transformation, new Vector3f(vertices[(3 * i)], vertices[(3 * i) + 1], vertices[(3 * i) + 2]), null);
			vecs[i] = worldPosition = new Vector3f(worldPosition.x * spheroidSpace.x,
													worldPosition.y * spheroidSpace.y,
													worldPosition.z * spheroidSpace.z);
		}
		return vecs;
	}

	/**
	 * Calculates the inverse width, height and depth of a 3d spheroid.
	 * 
	 * @param boundingBox a box that suggly surrounds a group of points
	 * @return a vector of floats which represent the inverse of the width, height and depth of the spheroid.
	 * 			multiplying any vector by this vector will return a new vector in a space when the radius of this spheroid is 2
	 */
	private static Vector3f calculateSpheroid(float[] boundingBox) {

		return new Vector3f(1f/ boundingBox[1] - boundingBox[0], 1f/ boundingBox[3] - boundingBox[2], 1f/ boundingBox[5] - boundingBox[4]);
	}
	
	
	/**
	 * The center of a bounding box
	 * 
	 * @param boundingBox
	 * @return the position at the center of the box
	 */
	private static Vector3f calculatePosition(float[] boundingBox) {

		return new Vector3f(
				boundingBox[0] + ((boundingBox[1] - boundingBox[0]) /2f),
				boundingBox[2] + ((boundingBox[3] - boundingBox[2]) /2f), 
				boundingBox[4] + ((boundingBox[5] - boundingBox[4]) /2f)
				);
	}
	
	
	/**
	 * A solution to the quadratic formula
	 * for an erquation of the form ax^2 + bx + c
	 * 
	 * @param a the x^2 component
	 * @param b the x component
	 * @param c the constant component
	 * @param maxR the largest value considered, for our purposes this is usually 1
	 * @return the lowest solution to the equation if there is one, or -1 if not (the equation should be assumed to always have positive roots)
	 */
	private static float getLowestRoot(float a, float b, float c, float maxR){
		
		float determinant = (b*b) - (4f*a*c);
		if(determinant < 0){
			return -1;
		}
		float sqrtD = (float) Math.sqrt(determinant);
		float r1 = (-b - sqrtD) / (2 * a);
		float r2 = (-b + sqrtD) / (2 * a);
		if(r1 > r2){
			float temp = r1;
			r1 = r2;
			r2 = temp;
		}
		if(r1 > 0 && r1 < maxR){
			return r1;
		}
		if(r2 > 0 && r2 < maxR){
			return r2;
		}
		return -1;
		
		
	}
	
	
	/**
	 * Abstract idea of an infinite plane with methods attached
	 * 
	 * @author matt
	 *
	 */
	private static class Plane{
		
		// A plane is defines as an equation (az + by + cz + d) or as a point on the plane and the normal to it. We store both
		float[] equation = new float[4];
		Vector3f normal;
		Vector3f origin;
		
		/**
		 * Define a plane by a point on it's surface and the normal to the plane
		 * 
		 * @param point a point on the plane's surface
		 * @param normal the noraml to the plane
		 */
		protected Plane(Vector3f point, Vector3f normal){
			
			this.normal = normal;
			this.origin = point;
			equation[0] = normal.x;
			equation[1] = normal.y;
			equation[2] = normal.z;
			equation[3] = -(normal.x*origin.x + normal.y * origin.y + normal.z * origin.z);
			
		}
		
		/**
		 * Define a plane by three points on it's surface (a triangle)
		 * 
		 * @param p1 the first point
		 * @param p2 the second point
		 * @param p3 the third point
		 */
		protected Plane(Vector3f p1, Vector3f p2, Vector3f p3){
			
			this.normal = Vector3f.cross(Vector3f.sub(p3, p1, null), Vector3f.sub(p2, p1, null), null);
			normal.normalise();
			this.origin = p1;
			equation[0] = normal.x;
			equation[1] = normal.y;
			equation[2] = normal.z;
			equation[3] = -(normal.x*origin.x + normal.y * origin.y + normal.z * origin.z);
		}
		
		/**
		 * Are two points in 3d space on the same side of this plane (will return false if they are both on the plane)
		 * 
		 * @param point1 the first point to check
		 * @param point2 the second point to check
		 * @return
		 */
		protected boolean onSameSide(Vector3f point1, Vector3f point2){
			float p1 = equation[0] * point1.x + equation[1] * point1.y + equation[2] * point1.z + equation[3];
			float p2 = equation[0] * point2.x + equation[1] * point2.y + equation[2] * point2.z + equation[3];
			return p1 * p2 > 0;
		}
		
		/**
		 * The distance from a 3d point in space to the nearest point of the infinite plane (signed in respect to the normal)
		 * 
		 * @param point a point to check
		 * @return the distance as a qint
		 */
		protected float signedDistanceTo(Vector3f point){
			return Vector3f.dot(normal, point) + equation[3];
			
		}
		
		/**
		 * is a point on the plane within a triangle defined by 3 points also on the plane.
		 * (Although no checks are performed it is assumed and required that the three points be on the specified plane)
		 * 
		 * @param point the point to check
		 * @param pa the first point
		 * @param pb the second point
		 * @param pc the final point
		 * @return a boolean of if the point lays in the triangle
		 */
		protected boolean inTriangle(Vector3f point, Vector3f pa, Vector3f pb, Vector3f pc){
			// A point is within the triangle if, for all three sides defined by two vertices, the point is on the same side as the final vertex
			return sameSide(point, pa, pb, pc) && sameSide(point, pb, pa, pc) && sameSide(point, pc, pa, pb);
			
		}
		
		/**
		 * is a point 'point' and a second point 'pointPrime' (which both lie on the plane) on the same side of an infinite line drawn on the plane
		 * 
		 * @param point a point to check
		 * @param pointPrime a second point to check, this will be the third vertex of the triangle pa, pb, p'
		 * @param pa one point of the infinite line
		 * @param pb another point of the infinite line
		 * @return
		 */
		protected boolean sameSide(Vector3f point, Vector3f pointPrime, Vector3f pa, Vector3f pb){

			Plane p = new Plane(pa, Vector3f.cross((Vector3f) Vector3f.sub(pa, pb, null).normalise(), normal, null));
			return p.onSameSide(point, pointPrime);
		}
		
	}
	
	

}
