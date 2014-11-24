/**
 * 
 *Copyright (c) 2014, SAP SE: Christian Brändel | Florian Schneider | Angelika Salmen | Technische Universität Dresden, Chair of Media Design: Marius Brade | Rainer Groh
 *On behalf of Marius Brade, this research project (contract no. 080951799) was funded by the European Social Fond and the Free State of Saxony as well as SAP Research. On behalf of Angelika Salmen, the research leading to these results was partly funded by the European Community's Seventh Framework Programme under grant agreement no. FP7-284928 ComVantage.
 *All rights reserved.
 *Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
 
package com.drawing.datastructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Matrix;
import android.util.Log;

import com.drawing.gestures.Point;

/**
 * 
 * @author Florian Schneider TU Dresden / SAP NEXT Dresden
 * @author Christian Brändel TU Dresden / SAP Research Dresden
 * 
 */
public class ScaledPathArray implements Serializable {
	
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -95471371206112915L;
	private List<CustomPath> paths; 
	private List<float[]> scales; 
		
	/**
	 * object that manages a list of paths and associated transformation matrices
	 */
	public ScaledPathArray()
	{
		paths = new ArrayList<CustomPath>();
		scales = new ArrayList<float[]>();
	}
	
	/**
	 * Attach path to the list of paths and !!! TRANSFORM !!! the respective vertices with the matrix
	 * Only use in terms of initial attachment!
	 * @param path path object that is attached to the list
	 * @param scale transformation matrix that is applied upon the path vertices
	 */
	public void addPath(CustomPath path, Matrix scale)
	{
		List<Point> points =  CustomPath.transformVertices(path.getVertices(), scale) ;
		path.setVertices(points);
		
		paths.add(path);
		float[] values = new float[9]; 
		scale.getValues(values);
		scales.add(values);
	}
	
	
	public void setOnTop(CustomPath path)
	{
		if (paths.indexOf(path) > 0)
		{
			float[] values = new float[9]; 
			values = scales.get(paths.indexOf(path));
			scales.remove(paths.indexOf(path));
			
			scales.add(values);
		}
		
		paths.remove(path);

		paths.add(path);
	}

/**
 * Remove path from the list
 * @param path path object that should be removed
 */
	public void removePath(CustomPath path)
	{
		if(paths.contains(path))
		{
			scales.remove(paths.indexOf(path));
			paths.remove(path);
		}
	}
	
	/**
	 * Remove path from the list by using its index
	 * @param index index of the path that should be removed
	 */
	public void removePath(int index)
	{
		if(paths.get(index) != null)
		{
			paths.remove(index);
			scales.remove(index);
		}
	}
	
	/**
	 * Select a <b>CustomPath<b> object from the list by index
	 * @param index index of the <b>CustomPath</b> that should be removed
	 * @return path with the selected index
	 */
	public CustomPath getPath(int index)
	{
		if(paths.get(index) != null)
		{
			return paths.get(index);
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Replace a <b>CustomPath</b> object of the list with another one
	 * @param index position of the path object that will be replaced
	 * @param path <b>CustomPath</b> object that should be added
	 */
	public void setPath(int index, CustomPath path)
	{
		this.paths.set(index, path);
	}
	
	/**
	 * Select a <b>Matrix<b> object from the list by index
	 * @param index index of the <b>Matrix</b> that should be removed
	 * @return matrix with the selected index
	 */
	public Matrix getScale(int index)
	{
		if(scales.get(index) != null)
		{
			Matrix matrix = new Matrix();
			matrix.setValues(scales.get(index));
			return matrix;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Get all paths that the <b>ScalePathArray</b> contains
	 * @return list of all <b>CustomPath</b> objects the <b>ScaledPathArray</b> contains
	 */
	public List<CustomPath> getPaths() 
	{
		return paths;
	}

	/**
	 * set path list
	 * @param paths list of paths that should be set
	 */
	public void setPaths(List<CustomPath> paths) 
	{
		this.paths = paths;
	}

	/**
	 * get the list of all transformation matrices that are attached to the <b>ScaledPathArray</b>
	 * @return list of all contained transformation matrices
	 */
	public List<Matrix> getScales() 
	{
		List<Matrix> scalesMatrixList = new ArrayList<Matrix>();
		Matrix helperMatrix;
		
		for(float[] array : scales)
		{
			helperMatrix = new Matrix();
			helperMatrix.setValues(array);
			scalesMatrixList.add(helperMatrix);
		}
		
		return scalesMatrixList;
	}

	/**
	 * set the list of transformation matrices
	 * @param scales
	 */
	public void setScales(List<Matrix> scales) 
	{
		List<float[]> scaleArrays = new ArrayList<float[]>();
		float[] helperArray; 
		
		for(Matrix matrix : scales)
		{
			
			helperArray = new float[9];
			matrix.getValues(helperArray);
			
			scaleArrays.add(helperArray);
		}
		
		this.scales = scaleArrays;
	}
	
	/**
	 * clears both the list of paths and the list of matrices
	 */
	public void clear()
	{
		scales.clear();
		paths.clear();
	}

	/**
	 * get an array of all path objects
	 * @return array of all <b>CustomPath</b> objects
	 */
	public CustomPath[] getPathsArray() 
	{
		CustomPath[] array = new CustomPath[paths.size()];
		paths.toArray(array);
		
		return array;
	}

	/**
	 * Figure out whether the <b>ScaledPathArray</b> contains a path that is highlighted
	 * @return true if there is at least one path that is highlighted
	 */
	public boolean ContainsHighlightedPath() 
	{
		boolean containsHighlight = false;
		int counter = 0;
		int size = paths.size();
		
		if(size > 0)
		while(!( counter == size))
		{
			if(paths.get(counter).isHighlighted())
			{
				containsHighlight = true;
				break;
			}
			
			counter ++;
		}
		
		return containsHighlight;
	}

	/**
	 * Add the content of a <b>ScaledPathArray<b> object to the current one
	 * @param pathList object that's content should be added 
	 */
	public void add(ScaledPathArray pathList) 
	{
		for(int i = 0; i < pathList.getPaths().size(); i++)
		{
			
			if(!paths.contains(pathList.getPaths().get(i)))
			{
		
				paths.add(pathList.getPaths().get(i));
				
				float[] helper;
				
				if(pathList.getScales().size() > 0)
				{
					helper = new float[9];
					pathList.getScales().get(i).getValues(helper);
					scales.add(helper);
				}
				else
				{
					helper = new float[9];
					new Matrix().getValues(helper);
					scales.add(helper);
				}
					
			}
		}

	}
}
