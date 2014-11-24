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

import android.graphics.Matrix;

/**
 * Formalized Component for a InstantiationRelation
 * 
 * @author Florian Schneider TU Dresden / SAP NEXT Dresden
 * 
 */
public class InstatiationRelation extends CustomObjectRelation
{

	private static final long serialVersionUID = 7284888900894995962L;

	public InstatiationRelation(CustomPath path, Matrix pathMatrix, DrawingComponent startElement, DrawingComponent endElement)
	{
		super(path, pathMatrix, startElement, endElement);
		
	}

	public void updateHelpText()
	{
		if (endElement instanceof DrawingIndividual)
		{
			if (!startElement.getItemText().equalsIgnoreCase("")) endElement.setHelpText("of type " + startElement.getItemText());
			else endElement.setHelpText("of type EMPTY CONCEPT");
		}
		else
		{
			if (!endElement.getItemText().equalsIgnoreCase("")) startElement.setHelpText("of type " + endElement.getItemText());
			else startElement.setHelpText("of type EMPTY CONCEPT");
		}
	}

	public void resetHelpText()
	{
		endElement.setHelpText("");
	}

}
