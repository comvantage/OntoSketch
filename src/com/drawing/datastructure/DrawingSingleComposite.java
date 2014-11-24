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

import com.drawing.application.MainActivity;
import com.hp.hpl.jena.ontology.OntResource;

import android.graphics.Matrix;
import android.graphics.Paint;

/**
 * The single composite drawing object class which accepts only one DrawingCompositeWord as child
 * 
 * @author Florian Schneider TU Dresden / SAP NEXT Dresden
 * 
 */
public abstract class DrawingSingleComposite extends DrawingComposite 
{

	private static final long serialVersionUID = 4141437007000296040L;
	
	protected DrawingCompositeWord componentChild;
	
	transient protected Paint paint;
	transient protected Paint backgroundPaint;
	
	transient protected Paint buttonPaint;
	
	public DrawingCompositeWord getComponentChild() 
	{
		return componentChild;
	}

	protected String name;

	public DrawingSingleComposite(CustomPath path, Matrix pathMatrix)
	{
		super(path, pathMatrix);
		
		leafState = true;
		
		paint = new Paint();
		paint.setAntiAlias(true);
		
		buttonPaint = new Paint();
		buttonPaint.setAntiAlias(true);
		
		backgroundPaint = new Paint();
		backgroundPaint.setAntiAlias(true);
		
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
