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

import com.drawing.gestures.Point;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;

/**
 * Component class for the CustomObjectRelation
 * 
 * @author Florian Schneider TU Dresden / SAP NEXT Dresden
 * 
 */
public abstract class CustomObjectRelation extends DrawingLeaf
{
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -2776574014659633683L;

	protected DrawingComponent startElement;
	
	protected DrawingComponent endElement;
	
	protected Point startPoint;
	protected Point endPoint;

	protected double angle;
	
	public CustomObjectRelation(CustomPath path, Matrix pathMatrix, DrawingComponent startElement, DrawingComponent endElement) 
	{
		super(path, pathMatrix);
		
		this.startElement = startElement;
		this.endElement = endElement;
		
		startPoint = startElement.getCenterPoint();
		endPoint = endElement.getCenterPoint();
		
		calculateRelationAngle();

		startElement.addRelation(this);
		endElement.addRelation(this);
		
	}
	
	private void calculateRelationAngle()
	{
		if (this instanceof FormalizedPropertyRelationButton && ((FormalizedPropertyRelationButton)this).getParentRelation() != null) 
		{
			angle = ((FormalizedPropertyRelationButton)this).getParentRelation().angle;
			return;
		}
		
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

	public void removeReferences()
	{
		startElement.removeRelation(this);
		endElement.removeRelation(this);
	}

	/**
	 * @see com.drawing.datastructure.DrawingComposite#setHighlighted(boolean)
	 */
	public void setHighlighted(boolean highlighted)
	{
		this.highlighted = highlighted;
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
	
	public boolean isSelfRelation()
	{
		return (startElement == endElement);
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
	

}
