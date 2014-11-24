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
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.Log;

/**
 * Formalized Component for a Individual
 * 
 * @author Florian Schneider TU Dresden / SAP NEXT Dresden
 * 
 */
public class FormalizedIndividual extends FormalizedObject 
{

	private static final long serialVersionUID = -5750243262386070594L;

	transient private Rect destinationRect;

	float[] points;

	private float scale;

	private final int frameOffset = 3;
	
	public FormalizedIndividual(CustomPath path, Matrix pathMatrix, String name, String helptext) 
	{
		super(path, pathMatrix, name, helptext);
		
		if (scale == 0)
			scale = 1.0f;
	}
	
	
	
	public void drawFormalizedIndividual(Canvas canvas, Matrix matrix)
	{
		paint.setAlpha(alpha);
		backgroundPaint.setAlpha(alpha);

		RectF tempRect = new RectF();

		path.computeBounds(tempRect, true);

		matrix.mapRect(tempRect);
		
		destinationRect = new Rect((int) tempRect.left - frameOffset, (int) tempRect.top - frameOffset,
				(int) tempRect.right + frameOffset, (int) tempRect.bottom + frameOffset);

		points = new float[] { path.getVertices().get(0).x, path.getVertices().get(0).y, width, height, 4f};

		matrix.mapPoints(points);
		
		float relHeight = destinationRect.height()/2;

		Point coloredRectPoint = new Point((float)(destinationRect.left + destinationRect.width()-(1.25 * relHeight)), destinationRect.top+(destinationRect.height()/2)-(relHeight/2));

		Point coloredRectCenter = new Point(coloredRectPoint.x+(relHeight/2), coloredRectPoint.y+(relHeight/2));

		if (highlighted) 
		{
			RectF frame = new RectF(destinationRect.left - frameOffset,
					destinationRect.top - frameOffset, destinationRect.right
					+ frameOffset, destinationRect.bottom + frameOffset);
			
			paint.setColor(highlightColor);

			matrix.mapRect(tempRect);
			
			canvas.drawRect(frame, paint);
		}
		
		if (!helpText.equalsIgnoreCase(""))
		{
			if (isOpen)
			{
				int stringCount = helpText.length();

				float rTop = (float) (coloredRectPoint.y);
				float rLeft = coloredRectPoint.x;
				float rRight =  (float)  (coloredRectPoint.x + relHeight +(stringCount * (0.25 * relHeight)));
				float rBottom = (float) (coloredRectPoint.y+relHeight);

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
				
				float tempTextSize = (float) 0.8 * (relHeight/2);
				
				Point textPos = new Point((float)(coloredRectCenter.x+2*relHeight/2), (float) (coloredRectCenter.y+(relHeight/2*0.25)));
				
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
		
		canvas.drawRect(background, backgroundPaint);
		
		float rTop = (float) (coloredRectPoint.y);
		float rLeft = coloredRectPoint.x;
		float rRight = coloredRectPoint.x + (relHeight);
		float rBottom = (float) (coloredRectPoint.y+relHeight);

		RectF colouredR = new RectF(rLeft, rTop,rRight, rBottom);
		
		paint.setColor(normalColor);
		paint.setAlpha(alpha);
		canvas.drawRect(colouredR, paint);

		if (!helpText.equalsIgnoreCase(""))
		{
			float buttonContentHeight = (float) (0.9 * (relHeight/2));
			
			Path p = new Path();
			p.moveTo(coloredRectCenter.x, coloredRectCenter.y-buttonContentHeight/2);
			p.lineTo(coloredRectCenter.x, coloredRectCenter.y+buttonContentHeight/2);
			p.moveTo(coloredRectCenter.x-buttonContentHeight/2, coloredRectCenter.y);
			p.lineTo(coloredRectCenter.x+buttonContentHeight/2, coloredRectCenter.y);
			
			buttonPaint.setStrokeWidth((float)(0.2 * (relHeight/2)));
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
		
		float tempTextSize = relHeight/2;

		paint.setColor(Color.BLACK);
		paint.setTextSize(tempTextSize);

		paint.setAlpha(alpha);
		canvas.drawText(name, points[0]+(relHeight/4), (float) (coloredRectCenter.y+(tempTextSize*0.25)), paint);
	}
}
