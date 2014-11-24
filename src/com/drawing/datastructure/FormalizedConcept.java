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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

/**
 * Formalized Component for a Concept
 * 
 * @author Florian Schneider TU Dresden / SAP NEXT Dresden
 * 
 */
public class FormalizedConcept extends FormalizedObject 
{

	private static final long serialVersionUID = -9143563732227053852L;

	transient private Rect sourceRect;

	transient private Rect destinationRect;

	private float x;

	private float y;
	
	private final float textSize = 20;

	private float textScale;

	float[] points;

	private float scale;

	private final int frameOffset = 3;
	
	
	public FormalizedConcept(CustomPath path, Matrix pathMatrix, String name, String helptext) 
	{
		super(path, pathMatrix, name, helptext);
		
		if (scale == 0)
			scale = 1.0f;
	}
	
	public void drawFormalizedConcept(Canvas canvas, Matrix matrix)
	{
		paint.setAlpha(alpha);
		backgroundPaint.setAlpha(alpha);

		textScale = 1.0f;

		RectF tempRect = new RectF();

		path.computeBounds(tempRect, true);

		matrix.mapRect(tempRect);

		destinationRect = new Rect((int) tempRect.left - frameOffset,
				(int) tempRect.top - frameOffset,
				(int) tempRect.right + frameOffset, (int) tempRect.bottom + frameOffset);

		points = new float[] { path.getVertices().get(0).x, path.getVertices().get(0).y, width, height};
		
		matrix.mapPoints(points);
		
		float relheight = destinationRect.height()/5;
		
		Point circleCenterPoint = new Point((float)(destinationRect.left + destinationRect.width()-(1.5 * relheight)), destinationRect.top+(destinationRect.height()/2));

		if (highlighted) 
		{
			RectF frame = new RectF(destinationRect.left - frameOffset,
					destinationRect.top - frameOffset, destinationRect.right
					+ frameOffset, destinationRect.bottom + frameOffset);
			
			paint.setColor(highlightColor);

			matrix.mapRect(tempRect);
			
			canvas.drawOval(frame, paint);
		}
		
		if (!helpText.equalsIgnoreCase(""))
		{
			if (isOpen)
			{
				
				int stringCount = helpText.length();

				float rTop = (float) (circleCenterPoint.y-relheight);
				float rLeft = circleCenterPoint.x;
				float rRight = (float) (circleCenterPoint.x + 2*relheight +(stringCount*(0.4 * relheight)));
				float rBottom = (float) (circleCenterPoint.y+relheight);

				RectF helpBackground = new RectF(rLeft, rTop,rRight, rBottom);

				backgroundPaint.setStyle(Style.FILL);
				backgroundPaint.setColor(buttonColorActive);
				backgroundPaint.setAlpha(alpha);
				canvas.drawRect(helpBackground, backgroundPaint);
				
				backgroundPaint.setStyle(Style.STROKE);
				backgroundPaint.setStrokeWidth(2f);
				backgroundPaint.setColor(normalColor);
				backgroundPaint.setAlpha(alpha);
				canvas.drawRect(helpBackground, backgroundPaint);
				
				float tempTextSize = (float) 0.8 * relheight;
				
				Point textPos = new Point((float)(circleCenterPoint.x+2*relheight), (float) (circleCenterPoint.y+(relheight*0.25)));
				
				paint.setTextSize(tempTextSize);  

				paint.setColor(Color.BLACK);

				paint.setAlpha(alpha);
				canvas.drawText(helpText, textPos.x, textPos.y, paint);

			}
		}
		
		RectF background = new RectF(destinationRect.left,destinationRect.top, destinationRect.right , destinationRect.bottom);

		backgroundPaint.setStyle(Style.FILL);
		backgroundPaint.setColor(backgroundColor);
		backgroundPaint.setAlpha(alpha);
		
		canvas.drawOval(background, backgroundPaint);
		
		paint.setColor(normalColor);
		paint.setAlpha(alpha);
		canvas.drawCircle(circleCenterPoint.x, circleCenterPoint.y, relheight, paint);
		
		if (!helpText.equalsIgnoreCase(""))
		{
			float buttonContentHeight = (float) (0.9 * relheight);

			Path p = new Path();
			p.moveTo(circleCenterPoint.x, circleCenterPoint.y-buttonContentHeight/2);
			p.lineTo(circleCenterPoint.x, circleCenterPoint.y+buttonContentHeight/2);
			p.moveTo(circleCenterPoint.x-buttonContentHeight/2, circleCenterPoint.y);
			p.lineTo(circleCenterPoint.x+buttonContentHeight/2, circleCenterPoint.y);
			
			buttonPaint.setStrokeWidth((float)(0.2 * relheight));
			buttonPaint.setStyle(Style.STROKE);
			
			if (isOpen)
			{
				buttonPaint.setColor(backgroundColor);
				buttonPaint.setAlpha(alpha);
				canvas.drawPath(p, buttonPaint);
			}
			else 
			{
				buttonPaint.setColor(buttonColorActive);
				buttonPaint.setAlpha(alpha);
				canvas.drawPath(p, buttonPaint);
			}

		}
		
		float tempTextSize = relheight;
		
		paint.setTextSize(tempTextSize);

		paint.setColor(Color.BLACK);

		paint.setAlpha(alpha);
		canvas.drawText(name, points[0]+(relheight/2), (float) (circleCenterPoint.y+(tempTextSize*0.25)), paint);
		
	}
	



}
