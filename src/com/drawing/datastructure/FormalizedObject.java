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

import java.util.UUID;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

import com.drawing.application.MainActivity;
import com.hp.hpl.jena.ontology.OntResource;

/**
 * Superclass of a Formalized Component
 * 
 * @author Florian Schneider TU Dresden / SAP NEXT Dresden
 * 
 */
public abstract class FormalizedObject extends DrawingLeaf
{

	private static final long serialVersionUID = 3227186474319569560L;

	transient private Rect sourceRect;

	transient private Rect destinationRect;

	transient protected Paint paint;
	transient protected Paint backgroundPaint;
	
	transient protected Paint buttonPaint;
	
	transient protected OntResource ontResource;
	
	protected String name;

	private float x;

	private float y;

	protected int width;

	protected int height;
	
	protected int normalColor;
	
	protected int backgroundColor;
	
	protected int buttonColorActive;
	protected int buttonColorInactive;

	protected int highlightColor;
	
	public FormalizedObject(CustomPath path, Matrix pathMatrix, String name, String helptext) 
	{
		super(path, pathMatrix);
		
		paint = new Paint();
		paint.setAntiAlias(true);
		
		buttonPaint = new Paint();
		buttonPaint.setAntiAlias(true);
		
		backgroundPaint = new Paint();
		backgroundPaint.setAntiAlias(true);
		
		this.name = name;
		this.itemText = name;
		
		if (helptext.contains("Resource")) this.helpText = "";
		else this.helpText = helptext;
	}

	
	/**
	 * Setter for the Position of the upper left corner of the
	 * <b>PersonObject</b>
	 * 
	 * @param x
	 *            x Position of the upper left corner
	 * @param y
	 *            y Position of the upper left corner
	 */
	public void setPosition(float x, float y, float width, float height) {

		float[] anchor = new float[] { x, y, width, height };

		Matrix matrix = new Matrix();

		matrix.setValues(pathMatrixValues);

		Matrix inverse = new Matrix();

		matrix.invert(inverse);

		inverse.mapPoints(anchor);

		this.x = anchor[0];
		this.y = anchor[1];
		this.width = (int) anchor[2];
		this.height = (int) anchor[3];

		sourceRect = new Rect((int) this.x, (int) this.y, (int) (this.x + width), (int) (this.y + height));

		CustomPath tempPath = new CustomPath();

		tempPath = generateVertices(this.x, this.y, (int) width, (int) height);


		tempPath.setHighlighted(path.isHighlighted());
		tempPath.setType(path.getGestureType());
		
		tempPath.setOntoType(path.getOntoType());

		UUID uuid = path.getUid();

		path = tempPath;
		
		path.setColor(Color.TRANSPARENT);

		path.setUid(uuid);

	}

	/**
	 * Creating a path representation for the <b>FormalizedObject</b> including the
	 * respective vertex data
	 * 
	 * @param x0
	 *            The upper left x coordinate
	 * @param y0
	 *            The upper left y coordinate
	 * @param width
	 *            The width of the object
	 * @param height
	 *            The height of the object
	 * @return A <b>CustomPath</b> object representing the <b>PersonObject</b>
	 */
	public CustomPath generateVertices(float x0, float y0, int width, int height) {

		CustomPath tempPath = new CustomPath();

		float x1, y1, x2, y2, x3, y3, tempCounterX, tempCounterY = 0;

		float samplingOffset = 5;

		x1 = x0;
		y1 = y0 + height;
		x2 = x0 + width;
		y2 = y0;
		x3 = x2;
		y3 = y1;

		tempPath.moveTo(x0, y0);

		tempCounterY = y0;

		while (tempCounterY + samplingOffset < y1) {
			tempCounterY += samplingOffset;

			tempPath.lineTo(x0, tempCounterY);
		}

		tempPath.lineTo(x1, y1);

		tempCounterX = x1;
		tempCounterY = y1;

		while (tempCounterX + samplingOffset < x2
				&& tempCounterY + samplingOffset < y2) {

			tempCounterX += samplingOffset;
			tempCounterY += samplingOffset;

			tempPath.lineTo(tempCounterX, tempCounterY);

		}

		tempPath.lineTo(x2, y2);

		tempCounterX = x2;

		while (tempCounterX - samplingOffset > x0) {

			tempCounterX -= samplingOffset;

			tempPath.lineTo(tempCounterX, y0);

		}

		tempPath.lineTo(x0, y0);

		tempCounterX = x0;
		tempCounterY = y0;

		while (tempCounterX + samplingOffset < x3
				&& tempCounterY + samplingOffset < y3) {

			tempCounterX += samplingOffset;
			tempCounterY += samplingOffset;

			tempPath.lineTo(tempCounterX, tempCounterY);

		}

		tempPath.lineTo(x3, y3);

		tempCounterY = y3;

		while (tempCounterY - samplingOffset > y2) {
			tempCounterY -= samplingOffset;

			tempPath.lineTo(x2, tempCounterY);
		}

		tempPath.lineTo(x2, y2);

		tempCounterX = x2;
		tempCounterY = y2;

		while (tempCounterX - samplingOffset > x1
				&& tempCounterY - samplingOffset > y1) {

			tempCounterX -= samplingOffset;
			tempCounterY -= samplingOffset;

			tempPath.lineTo(tempCounterX, tempCounterY);

		}

		tempPath.lineTo(x1, y1);

		tempCounterX = x1;

		while (tempCounterX + samplingOffset < x3) {
			tempCounterX += samplingOffset;

			tempPath.lineTo(tempCounterX, y3);
		}

		return tempPath;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
	
	public int getNormalColor() {
		return normalColor;
	}

	public void setNormalColor(int normalColor)
	{
		this.normalColor = normalColor;
	}

	public int getHighlightColor()
	{
		return highlightColor;
	}

	public void setHighlightColor(int highlightColor)
	{
		this.highlightColor = highlightColor;
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


	public int getButtonColorActive()
	{
		return buttonColorActive;
	}


	public void setButtonColorActive(int buttonColorActive)
	{
		this.buttonColorActive = buttonColorActive;
	}


	public int getButtonColorInactive()
	{
		return buttonColorInactive;
	}


	public void setButtonColorInactive(int buttonColorInactive)
	{	
		this.buttonColorInactive = buttonColorInactive;
	}
	

	/**
	 * @see com.drawing.datastructure.DrawingLeaf#redrawPathsafterDeserialization()
	 */
	public void redrawPathsafterDeserialization(MainActivity ma)
	{
		paint = new Paint();
		paint.setAntiAlias(true);
		
		buttonPaint = new Paint();
		buttonPaint.setAntiAlias(true);
		
		backgroundPaint = new Paint();
		backgroundPaint.setAntiAlias(true);
		
		super.redrawPathsafterDeserialization(ma);
		
		ontResource = MainActivity.getOntologyResource(uri);
	}
}
