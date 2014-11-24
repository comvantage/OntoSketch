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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;

import com.drawing.application.MainActivity;
import com.drawing.gestures.Point;


/**
 * abstract Component class for the PropertyRelation
 * 
 * @author Florian Schneider TU Dresden / SAP NEXT Dresden
 * 
 */
public abstract class PropertyRelation extends CustomObjectRelation 
{
	private static final long serialVersionUID = 6454438369422880498L;

	transient private Paint arrowPaint;
	transient private Path arrow;

	private int backgroundColor;
	
	transient protected Paint paint;
	transient protected Paint backgroundPaint;
	
	transient private RectF strokeWidthRect;

	public PropertyRelation(CustomPath path, Matrix pathMatrix, DrawingComponent startElement, DrawingComponent endElement) 
	{
		super(path, pathMatrix, startElement, endElement);
		
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
	
	public void updateArrow(Canvas canvas, Matrix matrix)
	{
		strokeWidthRect = new RectF(0,0, 4f, 4f);
		
		RectF tempRect = new RectF();
		
		path.computeBounds(tempRect, true);

		if (!path.isHighlighted()) matrix.mapRect(tempRect);
		matrix.mapRect(strokeWidthRect);

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
		if (path.isVisible()) canvas.drawPath(arrow, arrowPaint);
	}
	
	public void drawPropertyRelation(Canvas canvas, Matrix matrix)
	{
		if (!helpText.equalsIgnoreCase(""))
		{
			if (isOpen && path.isVisible())
			{
				int stringCount = helpText.length();
				
				RectF tempRect = new RectF();

				path.computeBounds(tempRect, true);

				matrix.mapRect(tempRect);
				
				Point circleCenterPoint = new Point((float)(tempRect.left+(0.5*tempRect.width())), (float)(tempRect.top+(0.5*tempRect.height())));

				
				float rTop = (float) (circleCenterPoint.y-tempRect.height()/2);
				float rLeft = circleCenterPoint.x;
				float rRight = (float) (circleCenterPoint.x + tempRect.height() + (stringCount*(0.45 * tempRect.height()/2)));
				float rBottom = (float) (circleCenterPoint.y+tempRect.height()/2);

				RectF helpBackground = new RectF(rLeft, rTop,rRight, rBottom);

				backgroundPaint.setStyle(Style.FILL);
				backgroundPaint.setColor(backgroundColor);
				backgroundPaint.setAlpha(alpha);
				
				canvas.drawRect(helpBackground, backgroundPaint);
				
				backgroundPaint.setStyle(Style.STROKE);
				backgroundPaint.setStrokeWidth(2f);
				backgroundPaint.setColor(path.getColor());
				backgroundPaint.setAlpha(alpha);
				
				canvas.drawRect(helpBackground, backgroundPaint);
				
				float tempTextSize = (float) (tempRect.height()*0.40);
				
				Point textPos = new Point((float)(circleCenterPoint.x+ 0.75*tempRect.height()), (float) (circleCenterPoint.y+(tempRect.height()*0.15)));
				
				paint.setTextSize(tempTextSize);  

				paint.setColor(Color.BLACK);

				paint.setAlpha(alpha);
				canvas.drawText(helpText, textPos.x, textPos.y, paint);
				
			}
			
		}
		else 
		{
			isOpen = false;
		}
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
}
