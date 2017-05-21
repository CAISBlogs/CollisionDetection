package com.tut.tutorial;

import com.tut.prerequesits.Entity;
import com.tut.prerequesits.Matrix4f;
import com.tut.prerequesits.Vector3f;

public class Maths {
	
	
	public static Matrix4f createTransformationMatrix(Entity entity){
		
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(entity.getPosition(), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(entity.getRotX()), new Vector3f(1,0,0), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(entity.getRotY()), new Vector3f(0,1,0), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(entity.getRotZ()), new Vector3f(0,0,1), matrix, matrix);
		Matrix4f.scale(new Vector3f(entity.getScale(), entity.getScale(), entity.getScale()), matrix, matrix);
		
		return matrix;
	}

}
