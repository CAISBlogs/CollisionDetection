package com.tut.tutorial;

import com.tut.prerequesits.Vector3f;

public class Entity{

	
	private Vector3f position;

	private float rotX;
	private float rotY;
	private float rotZ;
	private float scale;


	private float[] vertices;
	private int[] indices;
	private float[] normals;
	


	public Entity(Vector3f position, float rotX, float rotY, float rotZ, float scale, float[] vertices, float[] normals, int[] indices) {
		
		this.position = position;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
		this.scale = scale;
		this.normals = normals;
		this.vertices = vertices;
		this.indices = indices;
		
		
	}

	public Vector3f getPosition() {
		return position;
	}

	public float getRotX() {
		return rotX;
		
	}

	public float getRotY() {
		return rotY;
	}

	public float getRotZ() {
		return rotZ;
	}

	public float getScale() {
		return scale;
	}
	
	public float[] getVertices() {
		return vertices;
	}

	public int[] getIndices() {
		return indices;
	}

	public float[] getNormals() {
		return normals;
	}
}
