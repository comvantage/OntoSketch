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
 * Atomar letter element of a composite word Container
 * @author Christian Brändel TU Dresden / SAP Research Dresden
 *
 */
public class DrawingWordLetter extends DrawingLeaf
{

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -7650083221846791892L;

	/**
	 * Object that represents a drawn stroke within a recognized word
	 * @param path The respective path describing the visual representation
	 * @param pathMatrix The respective transformation matrix
	 */
	public DrawingWordLetter(CustomPath path, Matrix pathMatrix)
	{
		super(path, pathMatrix);
	}

	
	/**
	 * @see com.drawing.datastructure.DrawingComponent#setHighlighted(boolean)
	 */
	public void setHighlighted(boolean highlighted)
	{
		this.highlighted = highlighted;
		this.path.setHighlighted(highlighted);
	}

	/**
	 * @see com.drawing.datastructure.DrawingComponent#updatePath(android.graphics.Matrix, android.graphics.Matrix)
	 */
	public void updatePath(Matrix matrix, Matrix backupTransformationMatrix, boolean highlight)
	{		
		parent.updatePath(matrix, backupTransformationMatrix, highlight);
	}

}
