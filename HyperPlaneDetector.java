package com.tut.tutorial;

import java.util.ArrayList;
import java.util.Arrays;

import com.tut.prerequesits.Matrix4f;
import com.tut.prerequesits.Vector3f;
import com.tut.prerequesits.Vector4f;


/**
 * 
 * Runs an algorithm to determine if any two given entities collided with each other.
 * The algorithm checks every face to see if projecting all vertices of the two
 * shapes onto the pane defined by its normal gives an intersection. If it does in
 * all cases then a collision has occurred, if not then we say no collision has happened.
 * 
 * @author CAISBlogss
 *
 */

public class HyperPlaneDetector {
	
	public static boolean doesCollide(Entity primary, Entity secondary){
		
		Vector3f[] shapePrimary = verticesToWorldPosition(primary);
		Vector3f[] shapeSecondary = verticesToWorldPosition(secondary);
		Vector3f[] normals = cull(combine(normalsToWorldPosition(primary), normalsToWorldPosition(secondary)));
		
		for(int i = 0; i < normals.length; i++){
			
			if(!projectionOverlap(normals[i], shapePrimary, shapeSecondary)){
				return false;
			}
		}
		return true;
		
	}
	
	
	/**
	 * Parser method, shape vertices are stored as float arrays, this method will convert them to the lwjgl vector3f class for easier processing. The class also places them in accurate world position
	 * 
	 * @param e An entity, the vertices that define it will be given in the worldspace
	 * 
	 * @return an array of vectors representing the worldSpace vertices of the given shape
	 */
	
	private static Vector3f[] verticesToWorldPosition(Entity e){
		
		float[] vertices = e.getVertices();
		Matrix4f transformation = Maths.createTransformationMatrix(e);
		Vector3f[] verts = new Vector3f[vertices.length/3];
		
		for(int i = 0; i < vertices.length/3; i++){
			
			int root = i*3;
			Vector4f vertex = Matrix4f.transform(transformation, new Vector4f(vertices[root], vertices[root+1], vertices[root+2], 1), null);
			verts[i] = new Vector3f(vertex.x, vertex.y, vertex.z);
		}
		return verts;
		
	}
	/**
	 * Parser method for normals, as scale and translation are unnecessary for normalised vectors this class uses only the roational information to align the normals with the shape.
	 * 
	 * @param floats a float array of normals as found in the getNormals() method of textured model. This information should be sourced from there
	 * @param matrix a rotation only matrix representing the orientation of the object
	 * @return an array of normalised vectors representing the normals to the oriented shape
	 */
	private static Vector3f[] normalsToWorldPosition(Entity e){
		
		float[] normals = e.getNormals();
		Matrix4f transformation = Maths.createTransformationMatrix(e);
		Vector3f[] norms = new Vector3f[normals.length/3];
		for(int i = 0; i < normals.length/3; i++){
			
			int root = i*3;
			Vector4f normal = Matrix4f.transform(transformation, new Vector4f(normals[root], normals[root+1], normals[root+2], 0), null);
			norms[i] = (Vector3f) new Vector3f(normal.x, normal.y, normal.z).normalise();
		}
		return norms;
	}
	
	/**
	 * Simple method combines arrays of Vector3f, this is used if they mustbe generated separealy but used together
	 * 
	 * @param vecs any number of Vector3f arrays
	 * @return a new array containing the items in each input array
	 */
	private static Vector3f[] combine(Vector3f[] v1, Vector3f[] v2){
		
		ArrayList<Vector3f> returnVecs = new ArrayList<Vector3f>();
		returnVecs.addAll(Arrays.asList(v1));
		returnVecs.addAll(Arrays.asList(v2));
		return returnVecs.toArray(new Vector3f[returnVecs.size()]);
		
	}
	
	/**
	 * This method uses hashing to remove duplicate entries from the array, this method saves a lot of computation as each normal need only be checked for intersection once
	 * @param vecs an array of Vector3f possibly containing duplicate items to check
	 * @return a new array of Vector3f with no duplicate vectors
	 */
	private static Vector3f[] cull(Vector3f[] norms){
		
		ArrayList<Vector3f> returnNorms = new ArrayList<Vector3f>();
		for(int i = 0; i < norms.length; i++){
			
			if(!returnNorms.contains(norms[i])){
				returnNorms.add(norms[i]);
			}
		}
		return returnNorms.toArray(new Vector3f[returnNorms.size()]);

		
	}
	
	/**
	 * Method projects a 3d shape defined by an array of vertices to a line along a plane orthogonal to the normal in respect to the normal, the highest and lowest values of line are returned
	 * 
	 * @param plane the 'plane' to project onto (given as the normal vector) and the vector to consider the axis
	 * @param verts the vertices of the shape in a soup
	 * @return a float array where [0] is the lowest value and [1] is the highest
	 */
	private static float[] projectOnPlane(Vector3f plane, Vector3f[] verts){
		float max = Vector3f.dot(plane, verts[0]);
		float min = Vector3f.dot(plane, verts[0]);
		
		for(int i = 1; i < verts.length; i++){
			
			float val = Vector3f.dot(plane, verts[i]);
			if(val > max){
				max = val;
			}
			if(val < min){
				min = val;
			}
			
		}
		return new float[]{min, max};
		
	}
	
	
	/**
	 * Method to check if two given lines overlap at any point. If they do will return true, if not they return false
	 * 
	 * @param line1 a line defined as two endpoints with single float values stored [min][max]
	 * 
	 * @param line2 a line defined as two endpoints with single float values stored [min][max]
	 * 
	 * @return the result of the overlap test
	 */
	private static boolean lineOverlap(float[] line1, float[] line2){
		
		return !(line1[0] > line2[1] || line1[1] < line2[0]);
		
	}
	
	
	/**
	 * If given two shapes defined by their verices in worldspace will project both onto a plane at an arbitrary location
	 * defined as haveing a normal to the normal provided. Will return true if the pojections overlap, and false if they do
	 * not
	 * 
	 * @param normal, the plane to project points onto, will be normal to a given face on either shape
	 * 
	 * @param primary, the first shape to check
	 * @param secondary, the second shape to check
	 * 
	 * @return the result of the overlap test
	 */
	private static boolean projectionOverlap(Vector3f normal, Vector3f[] primary, Vector3f[] secondary){
		
		return lineOverlap(projectOnPlane(normal, primary), projectOnPlane(normal, secondary));
		
	}

}
