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

import com.drawing.gestures.Point;
import com.hp.hpl.jena.ontology.OntClass;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.Log;

public class DrawingConcept extends DrawingSingleComposite
{

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 8749184823361802661L;

	
	/**
	 * DrawingConcept
	 *
	 * @author Florian Schneider TU Dresden / SAP NEXT Dresden
	 * 
	 * @param path
	 * @param pathMatrix
	 */
	public DrawingConcept(CustomPath path, Matrix pathMatrix) 
	{
		super(path, pathMatrix);
	}
	
	
	public void addComponent(DrawingComponent component) 
	{
		// check whether the attached object lies within the current one and the DrawingConceptPath is still empty

//		if (component instanceof DrawingCompositeWord)Log.d("DrawingConcept", "addComponent IF " + ((DrawingCompositeWord)component).getResult());
//		else Log.d("DrawingConcept", "addComponent ELSE " + component);
		
		if (component instanceof DrawingCompositeWord) 
		{
			changed = true;

			RectF bounds = ((DrawingCompositeWord) component).getBounds();

			RectF localBounds = new RectF();

			if (path != null)
				path.computeBounds(localBounds, true);

			if (localBounds.contains(bounds))
			{

				try {

					if (children.size() == 0) 
					{
						children.add(component);
						component.parent = this;
						
						componentChild = (DrawingCompositeWord) component;
						
						for (DrawingComponent dc : ((DrawingCompositeWord) component).children)
						{
							dc.setDisplayState(this.displayState);
						}		
						
						//Log.d("displaystate", this.displayState + "   " + component.getDisplayState());
						
						component.setDisplayState(this.displayState);
					}
					else 
					{
						changed = false;
						
						for (DrawingComponent dc : ((DrawingCompositeWord) component).children)
						{
							dc.setDisplayState(DisplayObjectState.NONE);
						}		

						component.setDisplayState(DisplayObjectState.NONE);
						
						component.parent.childrenToAdd.add(component);
					}
					
				} 
				catch (Exception e) 
				{
					e.printStackTrace();

				}

			}
			else
			{
				for (DrawingComponent dc : ((DrawingCompositeWord) component).children)
				{
					dc.setDisplayState(DisplayObjectState.NONE);
				}		

				component.setDisplayState(DisplayObjectState.NONE);
			}
			
		}
		else 
		{
			changed = false;
			Log.d("DrawingConcept", "childrenToAdd");
			component.parent.childrenToAdd.add(component);
		}
		
		updateDrawingComponent();
	}
	
	
	public void drawDrawingConceptIcon(Canvas canvas, Matrix matrix)
	{
		if (!helpText.equalsIgnoreCase(""))
		{
			paint.setAlpha(alpha);
			backgroundPaint.setAlpha(alpha);

			RectF tempRect = new RectF();

			path.computeBounds(tempRect, true);

			if (!isHighlighted()) matrix.mapRect(tempRect);
			
			float relHeight = tempRect.height()/10;
			
			Point relCircleCenter = new Point((float)(tempRect.right), (float)(tempRect.top+(tempRect.height()/2)));
			
			if (isOpen)
			{
				
				int stringCount = helpText.length();

				float rTop = (float) (relCircleCenter.y-relHeight);
				float rLeft = relCircleCenter.x;
				float rRight = (float)(relCircleCenter.x + 2*relHeight +(stringCount*(0.5 * relHeight)));
				float rBottom = (float) (relCircleCenter.y+relHeight);

				RectF helpBackground = new RectF(rLeft, rTop,rRight, rBottom);

				backgroundPaint.setStyle(Style.FILL);
				backgroundPaint.setColor(Color.WHITE);
				backgroundPaint.setAlpha(alpha);
				canvas.drawRect(helpBackground, backgroundPaint);
				
				backgroundPaint.setStyle(Style.STROKE);
				backgroundPaint.setStrokeWidth(2f);
				backgroundPaint.setColor(path.color);
				backgroundPaint.setAlpha(alpha);
				canvas.drawRect(helpBackground, backgroundPaint);
				
				float tempTextSize = (float) 0.8 * relHeight;
				
				Point textPos = new Point((float)(relCircleCenter.x+1.5*relHeight), (float) (relCircleCenter.y+(relHeight*0.25)));
				
				paint.setTextSize(tempTextSize);  

				paint.setColor(Color.BLACK);

				paint.setAlpha(alpha);
				canvas.drawText(helpText, textPos.x, textPos.y, paint);

			}
			
			paint.setColor(path.getColor());
			paint.setAlpha(alpha);
			canvas.drawCircle(relCircleCenter.x, relCircleCenter.y, relHeight, paint);

			float buttonContentHeight = (float) (0.9 * relHeight);

			Path p = new Path();
			p.moveTo(relCircleCenter.x, relCircleCenter.y-buttonContentHeight/2);
			p.lineTo(relCircleCenter.x, relCircleCenter.y+buttonContentHeight/2);
			p.moveTo(relCircleCenter.x-buttonContentHeight/2, relCircleCenter.y);
			p.lineTo(relCircleCenter.x+buttonContentHeight/2, relCircleCenter.y);
			
			buttonPaint.setStrokeWidth((float)(relHeight*0.2));
			buttonPaint.setStyle(Style.STROKE);
			
			if (isOpen)
			{
				buttonPaint.setColor(Color.GRAY);
				buttonPaint.setAlpha(alpha);
				canvas.drawPath(p, buttonPaint);
			}
			else 
			{
				buttonPaint.setColor(Color.WHITE);
				buttonPaint.setAlpha(alpha);
				canvas.drawPath(p, buttonPaint);
			}

		}
		
	}
	
	/**
	 * @see com.drawing.datastructure.DrawingComposite#setHighlighted(boolean)
	 */
	public void setHighlighted(boolean highlighted)
	{
		this.highlighted = highlighted;
		
		if(!children.isEmpty()) children.get(0).setHighlighted(highlighted);

	}


	public DrawingCompositeWord getComponentChild() {
		return componentChild;
	}

	
	public String getName()
	{
		if (!children.isEmpty()) return ((DrawingCompositeWord) children.get(0)).getResult();
		else return "";
	}
	
	
	/**
	 * @see com.drawing.datastructure.DrawingComponent#removeCompositeComponent(com.drawing.datastructure.DrawingComponent)
	 */
	public void removeCompositeComponent(DrawingComponent component) 
	{
		if (component instanceof DrawingCompositeWord)Log.d("DrawingConcept", "removeCompositeComponent IF " + ((DrawingCompositeWord)component).getResult());
		else Log.d("DrawingConcept", "removeCompositeComponent ELSE " + component);
		
		changed = true;

		
		if (children.contains(component)) 
		{
			children.remove(component);
			componentChild = null;
			
			component.setDisplayState(DisplayObjectState.NONE);
			
			updateDrawingComponent();
			return;
		} 
		else 
		{
			for (DrawingComponent child : children) 
			{
				if (child.isComposite && !(child instanceof DrawingCompositeWord)) 
				{
					// only consider composite objects that are not an instance
					// of DrawingCompositeWord
					component.setDisplayState(DisplayObjectState.NONE);
					child.removeCompositeComponent(component);
					
				}
			}
		}
		
		updateDrawingComponent();
	}
	
	/**
	 * Remove a component of Type <b>DrawingComponent</b> from the children of
	 * this element
	 * 
	 * @param component
	 *            The element that should be removed
	 */
	public void removeComponent(DrawingComponent component) 
	{
		// TODO: decide whether to delete a complete component or just the
		// selected element of it
		// -> restructuring neccessary
		
		if (component instanceof DrawingCompositeWord)Log.d("DrawingConcept", "removeComponent IF " + ((DrawingCompositeWord)component).getResult());
		else Log.d("DrawingConcept", "removeComponent ELSE " + component);
		
		changed = true;

		if (children.contains(component)) 
		{
			if (component.isComposite) 
			{
				component.setDisplayState(DisplayObjectState.NONE);
				
				children.remove(component);
				componentChild = null;

				for (DrawingComponent child : ((DrawingComposite) component).children) 
				{
					if (!children.contains(child))
					{
						children.add(child);
						child.setDisplayState(DisplayObjectState.NONE);
					}
				}
				
				updateDrawingComponent();

				return;

			} else 
			{
				component.setDisplayState(DisplayObjectState.NONE);
				children.remove(component);
				componentChild = null;
				
				
				updateDrawingComponent();
				
				return;
			}
		} 
		else 
		{
			if (component != null)
			for (DrawingComponent child : children) 
			{
				if (child.isComposite)
				{
					component.setDisplayState(DisplayObjectState.NONE);
					child.removeComponent(component);
					componentChild = null;
					
				}
			}
		}
		
		updateDrawingComponent();
	}

	public void updateDrawingComponent()
	{
		if (componentChild != null)
		{
			//new Item added
			this.itemText = componentChild.getResult();
			ontResource.setComment(this.itemText, "EN");
			Log.d("DrawingConcept", "updateDrawingComponent ADD " + itemText);
		}
		else
		{
			Log.d("DrawingConcept", "updateDrawingComponent REMOVE " + itemText);
			
			//item removed
			ontResource.removeComment(this.itemText, "EN");
			this.itemText = "";
			
		}

		for (DrawingComponent dc : relations)
		{
			if (dc instanceof SubClassRelation)
				((SubClassRelation) dc).updateHelpText();
			else if (dc instanceof InstatiationRelation)
				((InstatiationRelation) dc).updateHelpText();
		}
	}
}
