/**
 * 
 *Copyright (c) 2014, SAP SE: Christian Br�ndel | Florian Schneider | Angelika Salmen | Technische Universit�t Dresden, Chair of Media Design: Marius Brade | Rainer Groh
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

import android.graphics.Matrix;

public class DrawingGroup extends DrawingComposite {

//	private static CustomPath path = new CustomPath();
//
//	private static Matrix matrix = new Matrix();

	public DrawingGroup(ArrayList<DrawingComponent> children, CustomPath path, Matrix matrix) {
		
		super(path, matrix);

		this.children = children;

		try {

			for (DrawingComponent child : children) {

				child.setParent(this);

			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		highlighted = true;
		
		leafState = true;

	}

	/**
	 * @see com.drawing.datastructure.DrawingComponent#updatePath(android.graphics.Matrix,
	 *      android.graphics.Matrix)
	 */
	public void updatePath(Matrix matrix, Matrix backupTransformationMatrix, boolean highlight) {

		changed = true;

		if (this.highlighted != highlight)
		{
			if (!this.highlighted)
			{
				this.highlighted = true;

			}
			else
			{
				this.highlighted = false;
			}

			for (DrawingComponent child : children)
			{
				if (child.isGrouped())
				{
					child.updateGroupedPath(matrix, backupTransformationMatrix, highlight);

				}

			}
		}

	}

	/**
	 * @see com.drawing.datastructure.DrawingComposite#setHighlighted(boolean)
	 */
	public void setHighlighted(boolean highlighted)
	{
		this.highlighted = highlighted;

		for (DrawingComponent child : children)
		{
			child.setHighlighted(highlighted);
		}

	}

}
