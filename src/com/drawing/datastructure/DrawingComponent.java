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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;

import com.drawing.application.MainActivity;
import com.drawing.gestures.Point;
import com.hp.hpl.jena.ontology.OntResource;

/**
 * abstract component class for the composite object structure
 * 
 * @author Florian Schneider TU Dresden / SAP NEXT Dresden
 * @author Christian Brändel TU Dresden / SAP Research Dresden
 * 
 */

public abstract class DrawingComponent implements Serializable{

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -2462398927131280380L;
	
	/**
	 * The object that is above this one in the hierarchy tree
	 */
	protected DrawingComposite parent = null;
	
	transient protected OntResource ontResource = null;
	
	protected String uri = "";
	
	protected boolean isOpen = false;
	
	protected String helpText = "";
	
	protected String itemText = "";

	/**
	 * The component class of the composite object structure
	 * 
	 * @param path
	 *            The respective path object that belongs to the component this
	 *            should only be null if the component is the root element of
	 *            the structure
	 * @param pathMatrix
	 *            The respective transformation matrix that belongs to the path
	 *            object
	 */
	public DrawingComponent(CustomPath path, Matrix pathMatrix) 
	{
		if (path != null)
		{
			List<Point> points = CustomPath.transformVertices(path.getVertices(), pathMatrix);
			path.setVertices(points);
			
			path.setHighlighted(false);
		}

		this.path = path;

		if(pathMatrix == null)
			pathMatrix = new Matrix();
			
		this.pathMatrixValues = new float[9];
		pathMatrix.getValues(this.pathMatrixValues);
		
		highlighted = false;
		
		grouped = false;
		
		relations = new LinkedList<DrawingComponent>();
		
		displayState = DisplayObjectState.NONE;
		
		alpha = 255;

	}
	
	protected DisplayObjectState displayState;
	
	public DisplayObjectState getDisplayState() 
	{
		return displayState;
	}

	public void setDisplayState(DisplayObjectState displayState) 
	{
		this.displayState = displayState;
	}

	protected int alpha;

	protected List<DrawingComponent> relations;
	
	protected CustomPath path;

	protected DrawingComponent annotation;

	protected float[] pathMatrixValues;

	protected float[] annotationMatrixValues;

	protected boolean highlighted;
	
	protected boolean grouped;
	
	/**
	 * Getter for the composite property of the component
	 */
	public boolean isComposite;

	/**
	 * Getter that returns true if the component is highlighted
	 * 
	 * @return true if the component is highlighted
	 */
	public boolean isHighlighted()
	{
		return highlighted;
	}
	
	public boolean hasRelations()
	{
		if(relations.size() <= 0) return false;
		else return true;
	}
	
	public int getPairRelationCount(DrawingComponent endComponent)
	{
		if(relations.size() <= 0) return 0;
		
		int count = 0;
		
		for (DrawingComponent dc : relations)
		{
			if (dc instanceof CustomObjectRelation)
			{
				if (((CustomObjectRelation) dc).endElement.equals(endComponent)) count++;
			}
			else if (dc instanceof DrawingPropertyRelation)
			{
				if (((DrawingPropertyRelation) dc).endElement.equals(endComponent)) count++;
			}
		}

		return count;
	}
	
	
	public int getSelfRelationCount()
	{
		int count = 0;
		
		for (DrawingComponent dc : relations)
		{
			if (dc instanceof CustomObjectRelation)
			{
				if (((CustomObjectRelation) dc).isSelfRelation()) count++;
			}
			else if (dc instanceof DrawingPropertyRelation)
			{
				if (((DrawingPropertyRelation) dc).isSelfRelation()) count++;
			}
		}
		
		return count;
	}
	

	public List<DrawingComponent> getRelations()
	{
		return relations;
	}
	
	public void setAlpha(int alpha)
	{
		this.alpha = alpha;
	}
	
	public int getAlpha()
	{
		return alpha;
	}
	
	public void addRelation(DrawingComponent relation)
	{
		if(!relations.contains(relation)) relations.add(relation);
	}
	
	public void removeRelation(DrawingComponent relation)
	{
		if(relations.contains(relation)) 
		{
			relations.remove(relation);
		}

	}


	protected boolean isRoot = false;
	
	/**
	 * True if this component is the root node of the object tree
	 * @param value true if this component is the root node
	 */
	public void setIsRoot(boolean value)
	{
		isRoot = value;
	}
	
	/**
	 * Setter for the highlighted property of the component
	 * 
	 * @param highlighted
	 *            true if the component should be highlighted
	 */
	public void setHighlighted(boolean highlighted) 
	{
		if(!grouped)
		{
			this.highlighted = highlighted;
			this.path.setHighlighted(highlighted);
		}
		else
		{
			if(parent != null)
			{
				this.highlighted = highlighted;
				this.path.setHighlighted(highlighted);
				
				if(parent.isHighlighted() != highlighted)
					parent.setHighlighted(highlighted);
			}
		}
	}
	
	public Point getCenterPoint()
	{
		RectF bounds = new RectF();
		
		path.computeBounds(bounds, true);

		Point point = new Point(bounds.centerX(), bounds.centerY());

		return point;
	}

	public boolean isGrouped()
	{
		return grouped;
	}

	public void setGrouped(boolean grouped)
	{
		this.grouped = grouped;
	}

	/**
	 * Getter for the count of objects included
	 * 
	 * @return The number of included objects
	 */
	public abstract int getComponentCount();

	/**
	 * Getter for the <b>CustomPath</b> object of the component
	 * 
	 * @return The <b>CustomPath</b> object of the component
	 */
	public CustomPath getPath()
	{
		return path;
	}

	/**
	 * Setter for the <b>CustomPath</b> object of the component
	 * 
	 * @param path
	 *            The <b>CustomPath</b> object that should be attached to this
	 *            component
	 */
	public void setPath(CustomPath path) 
	{
		this.path = path;
		
		if(path.isHighlighted())
		{
			setHighlighted(true);
		}else
		{
			setHighlighted(false);
		}
	}

	public ArrayList<CustomPath> setPathOnTop(boolean down)
	{
		ArrayList<CustomPath> ret = new ArrayList<CustomPath>();
		
		if (!isRoot)
		{
			if (down && parent != null)
			{
				ret.addAll(parent.setPathOnTop(true));
			}
			else
			{
				
				if (down)
				{
					return ret;
				}
				
			}

			if (this instanceof DrawingComposite)
			{
				for (DrawingComponent dc : ((DrawingComposite)this).children)
				{
					ret.add(path);
					ret.addAll(dc.setPathOnTop(false));
				}
			}
			else 
			{
				ret.add(path);
			}
			
		}
		else
		{
			return ret;
		}
		
		return ret;
		
	}

	public String getHelpText()
	{
		return helpText;
	}

	public void setHelpText(String helpText)
	{
		if (helpText.contains("Resource")) this.helpText = "";
		else this.helpText = helpText;
		
	}

	public boolean isOpen()
	{
		return isOpen;
	}

	public void setOpen(boolean isOpen)
	{
		this.isOpen = isOpen;
	}

	/**
	 * Getter for the list of paths of this and the encapsulated components
	 * 
	 * @return The list of paths of this and the encapsulated components
	 */
	public abstract ScaledPathArray getPathList();


	/**
	 * Retrieve a <b>DrawingComponent</b> by the respective <b>UUID</b>
	 * 
	 * @param uid
	 *            Unique identifier of a <b>CustomPath</b> object
	 * @return The <b>DrawingComponent</b> object that contains the path with
	 *         the respective <b>UUID</b>
	 */
	public abstract DrawingComponent getObjectByPathId(UUID uid);

	/**
	 * Integrate a new component into the tree structure
	 * 
	 * @param component
	 *            The <b>DrawingComponent</b> object that should be attached
	 * @return true if this component is a <b>DrawingLeaf</b> and the respective
	 *         <b>DrawingComponent</b> should be attached to the direct parent
	 *         object
	 */
	public abstract void addComponent(DrawingComponent component);

	/**
	 * Remove the respective component from the data structure
	 * @param component The component that shall be removed
	 */
	public void removeComponent(DrawingComponent component)
	{
		Log.d("Drawing Component", "removeComponent NOT IMPLEMENTED");

	}
	
	/**
	 * Remove the respective composite component and its children from the data structure
	 * @param component The composite component that shall be removed
	 */
	public void removeCompositeComponent(DrawingComponent component)
	{
		
	}
	
	/**
	 * Update the path according to current transformations and manage the highlighting
	 * @param matrix The current transformation matrix of the canvas
	 * @param backupTransformationMatrix The current backupMatrix of the canvas for temporary transformations
	 * @param highlight true if the respective component should be highlighted
	 */
	public abstract void updatePath(Matrix matrix, Matrix backupTransformationMatrix, boolean highlight); 
	

	/**
	 * Update a grouped path according to current transformations and manage the highlighting
	 * @param matrix The current transformation matrix of the canvas
	 * @param backupTransformationMatrix The current backupMatrix of the canvas for temporary transformations
	 * @param highlight true if the respective component should be highlighted
	 */
	public abstract void updateGroupedPath(Matrix matrix, Matrix backupTransformationMatrix, boolean highlight);
	

	/**
	 * 	All paths need to be redrawn due to an deserialization bug of the original Path object 
	 *  of Android, because otherwise nothing would be drawn on the canvas 
	 */
	public abstract void redrawPathsafterDeserialization(MainActivity ma);
	

	/**
	 * Determine the maximum extension of the paths drawn in order to translate the view port of the canvas
	 * in a way that every drawn path is included
	 * @return The array containing <b>[minX, maxX, minY, maxY]</b>
	 */
	public float[] determineDrawnExtrema(float[] extrema)
	{
		
		if(path != null)
		{
			
			if(path.minX < extrema[0])
				extrema[0]=path.minX;
			
			if(path.maxX > extrema[1])
				extrema[1] = path.maxX;
			
			if(path.minY < extrema[2])
				extrema[2]=path.minY;
			
			if(path.maxY > extrema[3])
				extrema[3]=path.maxY;
		}
						
		return extrema;
		
	}
	
	/**
	 * Method that validates to true, if the component structure contains an element of type PersonObject
	 * @return true if the component structure contains an element of type PersonObject
	 */
	
	public abstract boolean ContainsFormalizedConceptObjects();
	
	public abstract boolean ContainsFormalizedIndividualObjects();

	/**
	 * Getter for the parent property of this element
	 * @return The parent object of this element or null if none has been defined
	 */
	public DrawingComposite getParent()
	{
		return parent;
	}

	/**
	 * Setter for the parent property of this element
	 * @param parent The parent object of this element
	 */
	public void setParent(DrawingComposite parent)
	{
		this.parent = parent;
	}

	public String getUri() 
	{
		return uri;
	}

	public void setUri(String uri) 
	{
		this.uri = uri;
	}

	public String getItemText() {
		return itemText;
	}

	public void setItemText(String itemText) {
		this.itemText = itemText;
	}

	public boolean hasSuperClass()
	{
		for (DrawingComponent rel : relations)
		{
			if (rel instanceof SubClassRelation)
			{
				if (((SubClassRelation)rel).getEndElement().equals(this)) return true;
			}		
		}

		return false;
	}
	
	public boolean hasInstanziation()
	{
		for (DrawingComponent rel : relations)
		{
			if (rel instanceof InstatiationRelation)
			{
				return true;
			}
		}

		return false;
	}

	public DrawingComponent getInstanziationRelation()
	{
		for (DrawingComponent rel : relations)
		{
			if (rel instanceof InstatiationRelation)
			{
				return rel;
			}
		}

		return null;
	}
	
	public RectF getBounds() 
	{
		RectF bounds = new RectF();
		
		path.computeBounds(bounds, true);
		
		return bounds;
	}

	public int containsRelation(DrawingComponent relation)
	{
		int count = 0;
		
		if (relations.size() == 0) return count;
		else
		{
			for (DrawingComponent rel : relations)
			{
				if (rel instanceof CustomObjectRelation && relation instanceof CustomObjectRelation)
				{
					if (rel.getUri().equalsIgnoreCase(relation.getUri())
							&& ((CustomObjectRelation) rel).getStartElement() == ((CustomObjectRelation) relation).getStartElement()
							&& ((CustomObjectRelation) rel).getEndElement() == ((CustomObjectRelation) relation).getEndElement()) 
					{
						count++;
					}
				}
			}
		}

		return count;
	}

	public boolean containsRelation(String uri, DrawingComponent endComponent)
	{
		if (relations.size() == 0) return false;
		else
		{
			for (DrawingComponent rel : relations)
			{
				if (rel instanceof CustomObjectRelation)
				{
					if (rel.getUri().equalsIgnoreCase(uri)
							&& ((CustomObjectRelation)rel).getEndElement().equals(endComponent)) 
					{
						return true;
					}
				}
			}
		}

		return false;
	}

	public OntResource getOntResource()
	{
		return ontResource;
	}

	public void setOntResource(OntResource ontRes)
	{
		ontResource = ontRes;
	}
	
}
