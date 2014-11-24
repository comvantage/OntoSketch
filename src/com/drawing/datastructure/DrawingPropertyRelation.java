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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;

import com.drawing.application.DrawView;
import com.drawing.application.MainActivity;
import com.drawing.gestures.Point;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class DrawingPropertyRelation extends DrawingSingleComposite
{

	private static final long serialVersionUID = 5236571357067681303L;
	
	transient public ArrayList<FormalizedPropertyRelationButton> formalizedRelationbuttons = new ArrayList<FormalizedPropertyRelationButton>();

	transient private ArrayList<OntProperty> ontProperties = new ArrayList<OntProperty>();
	
	protected DrawingComponent startElement;
	
	protected DrawingComponent endElement;
	
	protected Point startPoint;
	protected Point endPoint;
	
	transient private Paint arrowPaint;
	transient private Path arrow;

	private int backgroundColor;
	
	transient protected Paint paint;
	transient protected Paint backgroundPaint;
	
	transient private RectF strokeWidthRect;

	protected double angle;
	
	protected int oldColor = 0;
	
	protected String oldItemName = "";

	public DrawingPropertyRelation(CustomPath path, Matrix pathMatrix, DrawingComponent startElement, DrawingComponent endElement) 
	{
		super(path, pathMatrix);
		
		this.startElement = startElement;
		this.endElement = endElement;
		
		startPoint = startElement.getCenterPoint();
		endPoint = endElement.getCenterPoint();
		
		calculateRelationAngle();

		startElement.addRelation(this);
		endElement.addRelation(this);
		
		paint = new Paint();
		paint.setAntiAlias(true);

		arrowPaint = new Paint();
		arrowPaint.setAntiAlias(true);
		arrowPaint.setColor(Color.WHITE);
		arrowPaint.setStyle(Style.STROKE);
		arrowPaint.setStrokeJoin(Join.ROUND);
		arrowPaint.setStrokeWidth(4f);
		arrowPaint.setAlpha(alpha);
		
		backgroundPaint = new Paint();
		backgroundPaint.setAntiAlias(true);

	}
	
	public void removeReferences()
	{
		startElement.removeRelation(this);
		endElement.removeRelation(this);
	}

	public void updateArrow(Canvas canvas, Matrix matrix)
	{
		strokeWidthRect = new RectF(0,0, 4f, 4f);
		
		RectF tempRect = new RectF();
		
		path.computeBounds(tempRect, true);

		if (!path.isHighlighted()) matrix.mapRect(tempRect);
		matrix.mapRect(strokeWidthRect);
		
		float leftBg = tempRect.left;
		float topBg = tempRect.top;
		float rightBg = tempRect.right - 6*(tempRect.height()/2);
		float bottomBg = tempRect.bottom;
		
		float rad = tempRect.height()/2;
		
		tempRect.set(leftBg, topBg, rightBg, bottomBg);
		
		backgroundPaint.setStyle(Style.FILL);
		backgroundPaint.setColor(path.getColor());
		backgroundPaint.setAlpha(alpha);
		
		canvas.drawCircle(leftBg+rad, topBg+rad, rad, backgroundPaint);

		Point circleCenterPoint = new Point((float)(tempRect.left+(0.5*tempRect.width())), (float)(tempRect.top+(0.5*tempRect.height())));
		
		Matrix rotateMatrix = new Matrix();
		
		Point start = new Point((float)(tempRect.left+(0.25*tempRect.width())), (float)(tempRect.top+(0.5*tempRect.height())));
		Point end = new Point((float)(tempRect.left+(0.75*tempRect.width())), (float)(tempRect.top+(0.5*tempRect.height())));
		Point top = new Point((float)(tempRect.left+(0.5*tempRect.width())), (float)(tempRect.top+(0.25*tempRect.height())));
		Point bottom = new Point((float)(tempRect.left+(0.5*tempRect.width())), (float)(tempRect.top+(0.75*tempRect.height())));
		
		arrow = new Path();
		arrow.moveTo(start.x, start.y);
		arrow.lineTo(end.x, end.y);
		arrow.lineTo(top.x, top.y);
		arrow.lineTo(end.x, end.y);
		arrow.lineTo(bottom.x, bottom.y);
		arrow.lineTo(end.x, end.y);
		arrow.lineTo(start.x, start.y);
		
		rotateMatrix.setRotate((float)(-90+this.angle),circleCenterPoint.x, circleCenterPoint.y);
		
		arrow.transform(rotateMatrix);
		
		arrowPaint.setStrokeWidth(strokeWidthRect.width());
		arrowPaint.setAlpha(alpha);
		canvas.drawPath(arrow, arrowPaint);
	}
	
	
	public void addComponent(DrawingComponent component) 
	{
		// check whether the attached object lies within the current one and the DrawingConceptPathis still empty

		if (component instanceof DrawingCompositeWord)Log.d("DrawingPropertyRelation", "addComponent IF " + ((DrawingCompositeWord)component).getResult());
		else Log.d("DrawingPropertyRelation", "addComponent ELSE " + component);
		
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
						int color = 0;
						
						if (oldColor == 0) color = MainActivity.getNextFreeColor();
						else color = oldColor;
						
						path.setColor(color);
						
						oldColor = color;
						
						children.add(component);
						component.parent = this;
						componentChild = (DrawingCompositeWord) component;
						
						if (formalizedRelationbuttons.size() > 0)
						{
							DrawView.removeFormalizedRelationButtons(formalizedRelationbuttons);
						}

						for (DrawingComponent dc : ((DrawingCompositeWord) component).children)
						{
							dc.setDisplayState(this.displayState);	
						}		

						component.setDisplayState(this.displayState);

					}
					else 
					{
						changed = false;
						path.setColor(Color.BLACK);
						
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
			path.setColor(oldColor);
			
			component.parent.childrenToAdd.add(component);
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
	
		if (component instanceof DrawingCompositeWord)Log.d("DrawingPropertyRelation", "removeComponent IF " + ((DrawingCompositeWord)component).getResult());
		if (component instanceof FormalizedPropertyRelationButton)Log.d("DrawingPropertyRelation", "removeComponent IF " + ((FormalizedPropertyRelationButton)component).getItemText());
		else Log.d("DrawingPropertyRelation", "removeComponent ELSE " + component);
		
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
	
			}
			else 
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
		if (component instanceof DrawingCompositeWord)Log.d("DrawingPropertyRelation", "removeCompositeComponent IF " + ((DrawingCompositeWord)component).getResult());
		else Log.d("DrawingPropertyRelation", "removeCompositeComponent ELSE " + component);
		
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
	
	private void updateDrawingComponent()
	{
		if (componentChild != null)
		{
			//new Item added
			this.itemText = componentChild.getResult();
			if (ontResource != null) ontResource.setComment(this.itemText, "EN");
			
			path.color = oldColor;
			
			
		}
		else
		{
			//item removed
			if (ontResource != null) ontResource.removeComment(this.itemText, "EN");
			this.itemText = "";
			path.color = Color.BLACK;
		}
	}

	private void calculateRelationAngle()
	{
		int quadrant = 0;

		if (startPoint.x < endPoint.x)
		{
			if (startPoint.y < endPoint.y)
			{
				quadrant = 4;
			}
			else if (startPoint.y > endPoint.y)
			{
				quadrant = 1;
			}
			else quadrant = 0;
		}
		else
		{
			if (startPoint.y < endPoint.y)
			{
				quadrant = 3;
			}
			else if (startPoint.y > endPoint.y)
			{
				quadrant = 2;
			}
			else quadrant = 0;
		}
		
		double deg = Math.toDegrees(Math.atan((endPoint.x-startPoint.x)/(endPoint.y-startPoint.y)));
		
		if (quadrant == 1)
			angle = Math.abs(deg);
		else if (quadrant == 2)
			angle = 360 - deg;
		else if (quadrant == 3)
			angle = 180 + Math.abs(deg);
		else if (quadrant == 4)
			angle = 180 - deg;
		else 
			angle = deg;

	}


	/**
	 * @see com.drawing.datastructure.DrawingLeaf#redrawPathsafterDeserialization()
	 */
	public void redrawPathsafterDeserialization(MainActivity ma)
	{
		paint = new Paint();
		paint.setAntiAlias(true);
	
		arrowPaint = new Paint();
		arrowPaint.setAntiAlias(true);
		arrowPaint.setColor(Color.WHITE);
		arrowPaint.setStyle(Style.STROKE);
		arrowPaint.setStrokeJoin(Join.ROUND);
		arrowPaint.setStrokeWidth(4f);
		arrowPaint.setAlpha(alpha);
		
		backgroundPaint = new Paint();
		backgroundPaint.setAntiAlias(true);
		
		super.redrawPathsafterDeserialization(ma);
		
	}

	public boolean isSelfRelation()
	{
		return (startElement == endElement);
	}

	/**
	 * @see com.drawing.datastructure.DrawingComposite#setHighlighted(boolean)
	 */
	public void setHighlighted(boolean highlighted)
	{
		this.highlighted = highlighted;
	}

	
	public void updateFormalizedRelations()
	{
		formalizedRelationbuttons.clear();
		
		ArrayList<OntProperty> temp = new ArrayList<OntProperty>();
		
		OntResource startOr = null;
		OntResource endOr = null;
		
		if (startElement instanceof FormalizedIndividual)
		{
			startOr = ((FormalizedIndividual) startElement).getOntResource();
		}
		else if(startElement instanceof DrawingIndividual)
		{
			startOr = ((DrawingIndividual) startElement).getOntResource();
		}
		
		if (endElement instanceof FormalizedIndividual)
		{
			endOr = ((FormalizedIndividual) endElement).getOntResource();
		}
		else if(endElement instanceof DrawingIndividual)
		{
			endOr = ((DrawingIndividual) endElement).getOntResource();
		}
		
		Log.d("DrawingPropertyRelation", "end of update: " + startOr + " "  + endOr);
		
		if (startOr != null && endOr != null)
		{
			Individual sIndividual = startOr.asIndividual();
			Individual eIndividual =  endOr.asIndividual();

			if (sIndividual == null || eIndividual == null)
			{
				ontProperties = temp;
				return;
			}
			
			OntClass sClass = null;
			OntClass eClass = null;

			ExtendedIterator<OntClass> ei = sIndividual.listOntClasses(true);
			
			while(ei.hasNext())
			{
				OntClass tClass = ei.next();

				if (!tClass.getLocalName().equalsIgnoreCase("Class"))
				{
					sClass = tClass;
					break;
				}
				
			}
			
			ExtendedIterator<OntClass> ei2 = eIndividual.listOntClasses(true);
			
			while(ei2.hasNext())
			{
				OntClass tClass = ei2.next();
				
				if (!tClass.getLocalName().equalsIgnoreCase("Class"))
				{
					eClass = tClass;
					break;
				}
			}

			for (OntProperty op : MainActivity.getProperties())
			{
				OntClass range = null;
				if (op.getRange() != null) range = op.getRange().asClass();
				
				OntClass domain = null;
				if (op.getDomain() != null) domain= op.getDomain().asClass();
				
				if (range != null && domain != null)
				{
					try
					{
						if (eClass.equals(range) && sClass.equals(domain))
						{					
							if (!temp.contains(op)) temp.add(op);
						}
					}
					catch(Exception e)
					{
						
					}
				}	
			}
		}
		
		ontProperties = temp;
	}

	public Point getCenterPoint()
	{
		RectF bounds = new RectF();
		
		path.computeBounds(bounds, true);
	
		Point point = new Point((float)(bounds.centerX()-(2.75*(bounds.height()/2))), bounds.centerY()); //3.25
	
		return point;
	}

	public Point getStartElementCenterPoint() 
	{
		return startElement.getCenterPoint();
	}
	
	public Point getEndElementCenterPoint() 
	{
		return endElement.getCenterPoint();
	}

	public DrawingComponent getStartElement()
	{
		return startElement;
	}

	public void setStartElement(DrawingComponent startElement)
	{
		this.startElement = startElement;
	}

	public DrawingComponent getEndElement()
	{
		return endElement;
	}

	public void setEndElement(DrawingComponent endElement)
	{
		this.endElement = endElement;
	}


	public Point getStartPoint()
	{
		return startPoint;
	}


	public void setStartPoint(Point startPoint)
	{
		calculateRelationAngle();
		
		this.startPoint = startPoint;
	}


	public Point getEndPoint() 
	{
		return endPoint;
	}


	public void setEndPoint(Point endPoint)
	{
		calculateRelationAngle();
		
		this.endPoint = endPoint;
	}


	public double getAngle()
	{
		return angle;
	}
	
	public int getBackgroundColor()
	{
		return backgroundColor;
	}

	public void setBackgroundColor(int backgroundColor)
	{
		backgroundPaint.setColor(backgroundColor);
		this.backgroundColor = backgroundColor;
	}

	public ArrayList<FormalizedPropertyRelationButton> getFormalizedRelationbuttons()
	{
		return formalizedRelationbuttons;
	}

	public boolean hasFormalizedRelations() 
	{
		if (ontProperties.size() > 0) return true;
		else return false;
	}

	public ArrayList<OntProperty> getOntProperties() {
		return ontProperties;
	}

	public void addRelationButton(FormalizedPropertyRelationButton fprb) 
	{
		formalizedRelationbuttons.add(fprb);
	}

	public void removeRelationButtons()
	{
		formalizedRelationbuttons.clear();
	}

	public void removeRelationButton(FormalizedPropertyRelationButton fbutton)
	{
		formalizedRelationbuttons.remove(fbutton);
	}

}
