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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.Log;

public class DrawingIndividual extends DrawingSingleComposite
{
	

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 558093904222781650L;

	/**
	 * DrawingIndividual
	 * 
	 * @param path
	 * @param pathMatrix
	 */
	public DrawingIndividual(CustomPath path, Matrix pathMatrix) 
	{
		super(path, pathMatrix);
	}
	
	
	public void addComponent(DrawingComponent component) 
	{
		// check whether the attached object lies within the current one and the DrawingConceptPathis still empty

		Log.d("DrawingIndividual", "addComponent " + component);
		
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
						
						updateDrawingComponent();
						
						for (DrawingComponent dc : ((DrawingCompositeWord) component).children)
						{
							dc.setDisplayState(this.displayState);
						}

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
			
			component.parent.childrenToAdd.add(component);
		}
		
	}
	
	
	public void removeComponent(DrawingComponent component) 
	{
		// TODO: decide whether to delete a complete component or just the
		// selected element of it
		// -> restructuring neccessary

		if (component instanceof DrawingCompositeWord)Log.d("DrawingIndividual", "removeComponent IF " + ((DrawingCompositeWord)component).getResult());
		if (component instanceof FormalizedPropertyRelationButton)Log.d("DrawingIndividual", "removeComponent IF " + ((FormalizedPropertyRelationButton)component).getItemText());
		else Log.d("DrawingIndividual", "removeComponent ELSE " + component);
		
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
				children.remove(component);
				componentChild = null;
				component.setDisplayState(DisplayObjectState.NONE);
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


	/**
	 * @see com.drawing.datastructure.DrawingComponent#removeCompositeComponent(com.drawing.datastructure.DrawingComponent)
	 */
	public void removeCompositeComponent(DrawingComponent component) 
	{
		if (component instanceof DrawingCompositeWord)Log.d("DrawingIndividual", "removeCompositeComponent IF " + ((DrawingCompositeWord)component).getResult());
		if (component instanceof FormalizedPropertyRelationButton)Log.d("DrawingIndividual", "removeCompositeComponent IF " + ((FormalizedPropertyRelationButton)component).getItemText());
		else Log.d("DrawingIndividual", "removeCompositeComponent ELSE " + component);
		
		changed = true;
	
		if (children.contains(component)) 
		{
			children.remove(component);
			componentChild = null;
			
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
					child.removeCompositeComponent(component);
	
				}
				
			}
			
			updateDrawingComponent();
		}
	}


	public void drawDrawingIndividualIcon(Canvas canvas, Matrix matrix)
	{
		if (!helpText.equalsIgnoreCase(""))
		{
			paint.setAlpha(alpha);
			backgroundPaint.setAlpha(alpha);

			RectF tempRect = new RectF();

			path.computeBounds(tempRect, true);

			if (!isHighlighted()) matrix.mapRect(tempRect);
			
			float relHeight = tempRect.height()/5;
			
			Point relRectPoint = new Point((float)(tempRect.right-relHeight/2), (float)(tempRect.top+(tempRect.height()/2)-relHeight/2));
			
			Point coloredRectCenter = new Point(relRectPoint.x+relHeight/2, relRectPoint.y+relHeight/2);
			
			if (isOpen)
			{
				int stringCount = helpText.length();

				float rTop = (float) (relRectPoint.y);
				float rLeft = relRectPoint.x;
				float rRight = (float) (relRectPoint.x + 2*relHeight +(stringCount*(0.35 * relHeight/2)));
				float rBottom = (float) (relRectPoint.y+relHeight);

				RectF helpBackground = new RectF(rLeft, rTop,rRight, rBottom);

				backgroundPaint.setStyle(Style.FILL);
				backgroundPaint.setColor(Color.WHITE);
				backgroundPaint.setAlpha(alpha);
				canvas.drawRect(helpBackground, backgroundPaint);
				
				backgroundPaint.setStyle(Style.STROKE);
				backgroundPaint.setStrokeWidth(2f);
				backgroundPaint.setColor(path.getColor());
				backgroundPaint.setAlpha(alpha);
				canvas.drawRect(helpBackground, backgroundPaint);
				
				float tempTextSize = (float) 0.8 * (relHeight/2);
				
				Point textPos = new Point((float)(coloredRectCenter.x+1.5*relHeight/2), (float) (coloredRectCenter.y+(relHeight/2*0.25)));
				
				paint.setTextSize(tempTextSize);  

				paint.setColor(Color.BLACK);

				paint.setAlpha(alpha);
				canvas.drawText(helpText, textPos.x, textPos.y, paint);
				
			}
				
			float rTop = (float) (relRectPoint.y);
			float rLeft = relRectPoint.x;
			float rRight = relRectPoint.x + (relHeight);
			float rBottom = (float) (relRectPoint.y+relHeight);

			
			RectF colouredR = new RectF(rLeft, rTop,rRight, rBottom);
			
			paint.setColor(path.color);
			paint.setAlpha(alpha);
			canvas.drawRect(colouredR, paint);
				
			float buttonContentHeight = (float) (0.9 * (relHeight/2));
			
			Path p = new Path();
			p.moveTo(coloredRectCenter.x, coloredRectCenter.y-buttonContentHeight/2);
			p.lineTo(coloredRectCenter.x, coloredRectCenter.y+buttonContentHeight/2);
			p.moveTo(coloredRectCenter.x-buttonContentHeight/2, coloredRectCenter.y);
			p.lineTo(coloredRectCenter.x+buttonContentHeight/2, coloredRectCenter.y);
			
			buttonPaint.setStrokeWidth((float)(relHeight*0.1));
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


	public DrawingCompositeWord getComponentChild()
	{
		return componentChild;
	}

	
	public String getName()
	{
		if (!children.isEmpty()) return ((DrawingCompositeWord) children.get(0)).getResult();
		else return "";
	}

	public void updateDrawingComponent()
	{
		
		if (componentChild != null)
		{
			//new Item added
			this.itemText = componentChild.getResult();
			ontResource.setComment(this.itemText, "EN");
		}
		else
		{
			//item removed
			ontResource.removeComment(this.itemText, "EN");
			this.itemText = "";
		}

		for (DrawingComponent dc : relations)
		{
			if (dc instanceof InstatiationRelation)
				((InstatiationRelation) dc).updateHelpText();
		}
		
	}
}

