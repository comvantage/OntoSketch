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

import java.util.ArrayList;
import java.util.List;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;

/**
 * Composite container class with no visual repräsentation
 * 
 * @author Florian Schneider TU Dresden / SAP NEXT Dresden
 * @author Christian Brändel TU Dresden / SAP Research Dresden
 * 
 */
public class DrawingCompositeWord extends DrawingSingleComposite {

	/**
	 * Serial version UID
	 */
	 private static final long serialVersionUID = -5696530248227643663L;
	 
	static CustomPath path = new CustomPath();

	private String result;

	/**
	 * Getter for the encapsulated word of this {@link DrawingCompositeWord}
	 * @return The word that is represented by this object
	 */
	public String getResult() 
	{
		return result;
	}

	private static Matrix matrix = new Matrix();

	/**
	 * Constructor of <b>DrawingCompositeWord</b>
	 * 
	 * @param children
	 *            A list of <b>DrawingComponent</b> child elements that will be
	 *            attached to this component
	 * @param result
	 *            True if the object was created successfully
	 */
	public DrawingCompositeWord(ArrayList<DrawingComponent> children, String result) 
	{

		super(path, matrix);

		this.children = children;
		this.result = result;

		try 
		{
			for (DrawingComponent child : children) 
			{
				((DrawingWordLetter) child).parent = this;
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		this.leafState = true; //unused?!?

	}
	
	
	public RectF getBounds()
	{
		RectF childBounds = new RectF();
		boolean childFirst = true;
		
		RectF tempBounds = new RectF();

		for (DrawingComponent child : children)
		{
			if (childFirst)
			{
				child.getPath().computeBounds(childBounds, true);
				childFirst = false;
			} 
			else
			{
				child.getPath().computeBounds(tempBounds, true);
				childBounds.union(tempBounds);
			}
		}

		return childBounds;
	}

	/**
	 * @see com.drawing.datastructure.DrawingComponent#updatePath(android.graphics.Matrix,
	 *      android.graphics.Matrix)
	 */
	public void updatePath(Matrix matrix, Matrix backupTransformationMatrix, boolean highlight) 
	{
		Log.d("DrawingcompositeWord","update drawingwordcomposite");
		
		if(!grouped)
		{
			applyTransformationAndHighlight(matrix, backupTransformationMatrix, highlight);
		}
		else
		{
			if(parent != null)
			{
				parent.updatePath(matrix, backupTransformationMatrix, highlight);
			}
		}
		
	}
	
	/**
	 * @see com.drawing.datastructure.DrawingComposite#updateGroupedPath(android.graphics.Matrix, android.graphics.Matrix, boolean)
	 */
	public void updateGroupedPath(Matrix matrix, Matrix backupTransformationMatrix, boolean highlight)
	{
		
		applyTransformationAndHighlight(matrix, backupTransformationMatrix, highlight);
		
	}
	
	 
	/**
	 * Method that actually applies transformation and highlight upon the {@link DrawingCompositeWord} object and its children
	 * @param matrix The current transformation matrix of the canvas
	 * @param backupTransformationMatrix The current backupMatrix of the canvas for temporary transformations

	 * @param highlight True if the respective component should be highlighted

	 */
	public void applyTransformationAndHighlight(Matrix matrix, Matrix backupTransformationMatrix, boolean highlight)
	{
		
		if (this.highlighted != highlight) 
		{

			changed = true;

				if (highlight) 
				{
					CustomPath tempPath;

					for (DrawingComponent child : children) 
					{
						tempPath = child.getPath();

						tempPath.applyTransformation(backupTransformationMatrix, matrix);

						child.setPath(tempPath);

						child.setHighlighted(highlight);

					}

					this.highlighted = highlight;

				}
				else
				{

					CustomPath tempPath = new CustomPath();

					for (DrawingComponent child : children) 
					{
						// Caution: the retransformation of a highlighted object
						// differs from the previously applied transformation

						tempPath = child.getPath();

						tempPath.applyTransformation(matrix, backupTransformationMatrix);

						child.setPath(tempPath);

						child.setHighlighted(highlight);
					}

					this.highlighted = highlight;

				}

			
		}
		
	}
	
	/**
	 * @see com.drawing.datastructure.DrawingComponent#setGrouped(boolean)
	 */
	public void setGrouped(boolean grouped) 
	{

		this.grouped = grouped;

		for (DrawingComponent child : children) 
		{
			child.setGrouped(grouped);
		}

	}
	

	/**
	 * @see com.drawing.datastructure.DrawingComposite#addComponent(com.drawing.datastructure.DrawingComponent)
	 */
	public void addComponent(DrawingComponent component) 
	{
		changed = true;

		component.parent.childrenToAdd.add(component);
	}
	

}
