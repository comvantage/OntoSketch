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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;

import com.drawing.application.DrawView;
import com.drawing.application.MainActivity;
import com.drawing.gestures.Point;
import com.drawing.ontosketch.R;

/**
 * The composite drawing object class
 * 
 * @author Florian Schneider TU Dresden / SAP NEXT Dresden
 * @author Christian Brändel TU Dresden / SAP Research Dresden
 * 
 */
public class DrawingComposite extends DrawingComponent
{

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 6052347162803933503L;
	
	/**
	 * Indicates if more children are allowed. True if no more children are allowed.
	 */
	protected boolean leafState = false;

	/**
	 * The composite drawing object constructor
	 * 
	 * @param path
	 *            The respective path object that belongs to the composite this
	 *            should only be null if the component is the root element of
	 *            the structure
	 * @param pathMatrix
	 *            The respective transformation matrix that belongs to the path
	 *            object
	 * @see com.drawing.datastructure.DrawingComponent
	 */
	public DrawingComposite(CustomPath path, Matrix pathMatrix) 
	{
		super(path, pathMatrix);

		isComposite = true;

		children = new ArrayList<DrawingComponent>();

		localPathList = new ScaledPathArray();

		changed = true;
	}

	/**
	 * A List of all child objects this composite object contains
	 */
	public List<DrawingComponent> children;

	private ScaledPathArray localPathList;

	/**
	 * True if the component structure of this element has changed
	 */
	public boolean changed;
	
	public List<DrawingComponent> childrenToAdd = new LinkedList<DrawingComponent>();

	/**
	 * Delete all child objects
	 */
	public void deleteChildren()
	{
		changed = true;
		children.clear();
	}

	/**
	 * @see com.drawing.datastructure.DrawingComponent#setHighlighted(boolean)
	 */
	public void setHighlighted(boolean highlighted) 
	{

		if (!grouped)
		{
			this.highlighted = highlighted;
			this.path.setHighlighted(highlighted);

			for (DrawingComponent child : children)
			{
				child.setHighlighted(highlighted);
			}
		} 
		else
		{
			if (parent != null) 
			{
				if (parent.isHighlighted() != highlighted)
					parent.setHighlighted(highlighted);
			}
		}

	}


	/**
	 * @see com.drawing.datastructure.DrawingComponent#getPath()
	 */
	public CustomPath getPath()
	{

		return path;
	}

	/**
	 * Add a component of Type <b>DrawingComponent</b> to the children of this
	 * element
	 * 
	 * @param component
	 *            The element that should be attached
	 */
	public void addComponent(DrawingComponent component) 
	{
		// check whether the attached object lies within the current one
		if (component != null)
		{
			changed = true;

			List<DrawingComponent> componentsToDelete = new ArrayList<DrawingComponent>();

			RectF bounds = new RectF();

			if (component instanceof DrawingCompositeWord || component instanceof DrawingGroup) 
			{

				boolean first = true;
				RectF tempBounds = new RectF();

				for (DrawingComponent child : ((DrawingComposite) component).children) 
				{
					
					if(child instanceof DrawingWordLetter)
					{
			
						if (first) 
						{
							child.getPath().computeBounds(bounds, true);
							first = false;
						} 
						else 
						{
							child.getPath().computeBounds(tempBounds, true);
							bounds.union(tempBounds);
						}
						
						child.setDisplayState(DisplayObjectState.NONE);
					}
					else
					{
						
						RectF childBounds = new RectF();
						boolean childFirst = true;
						
						if(child instanceof DrawingCompositeWord)
						{
						
							//DrawingGroupd can also contain composite objects that got a parent element with an empty path
							//therefore the child bounds have to be calculated
							
							for(DrawingComponent childchild : ((DrawingComposite) child).children)
							{
								if (childFirst) 
								{
									childchild.getPath().computeBounds(childBounds, true);
									childFirst = false;
								} 
								else 
								{
									childchild.getPath().computeBounds(tempBounds, true);
									childBounds.union(tempBounds);
								}
								
								child.setDisplayState(DisplayObjectState.NONE);
							}

						}
						else
						{
						 child.getPath().computeBounds(childBounds, true);
						}
						
						bounds.union(childBounds);
					}
				}
			} 
			else 
			{
				component.getPath().computeBounds(bounds, true);
			}

			RectF localBounds = new RectF();

			if (path != null)
				path.computeBounds(localBounds, true);

			if (isRoot || localBounds.contains(bounds))
			{
//				Log.d("DrawingComposite", "============== try to add component ======================");

//				if(isRoot) Log.d("DrawingComposite", this + " isRoot " + isRoot);
				
//				Log.d("DrawingComposite", component + " is in Bounds of " + this);

				try {

					if (children.size() == 0) 
					{
						component.parent = this;
						
//						Log.d("DrawingComposite", component + " add in children of this " + this);
						
						children.add(component);
						
					} 
					else 
					{

						RectF childBounds = new RectF();

						for (DrawingComponent child : children) 
						{
							if (child instanceof DrawingCompositeWord || child instanceof DrawingGroup)
							{
								boolean first = true;
								RectF tempBounds = new RectF();

								for (DrawingComponent compositeWordChild : ((DrawingComposite) child).children) 
								{
									if (first) 
									{
										compositeWordChild.getPath().computeBounds(childBounds, true);
										first = false;
									} 
									else 
									{
										compositeWordChild.getPath().computeBounds(tempBounds, true);
										childBounds.union(tempBounds);
									}
								}
							} 
							else 
							{
								child.getPath().computeBounds(childBounds, true);
							}

							if (childBounds.contains(bounds) && child.isComposite && !(child instanceof DrawingGroup)) 
							{
//								Log.d("DrawingComposite","add new child into existing: " + child);

								component.parent = this;
								child.addComponent(component);
								
								addChildren(component);
								return;
							} 
							else if (bounds.contains(childBounds)
									&& component.isComposite
									&& !(component instanceof DrawingCompositeWord)) 
							{

//								Log.d("DrawingComposite","add new child as parent of existing: " + child);
								
								// add child component only if it is not already
								// included
								if (!((DrawingComposite) component).children.contains(child) && !(component instanceof DrawingGroup)) 
								{
//									Log.d("DrawingComposite","add in: " + component);
									
									component.addComponent(child);
								}
								
								if(!(component instanceof DrawingGroup))
								componentsToDelete.add(child);

							}

						}

						for (DrawingComponent child : componentsToDelete) 
						{
//							Log.d("DrawingComposite","remove child - componentsToDelete: " + child);
							children.remove(child);
						}
						
						// GROUPING added 30.05.2012
						if (component.isGrouped()) 
						{
							if (component.getParent() != null) 
							{
								component.getParent().children.add(component);
							}
						} 
						else
						{
							if (component instanceof CustomObjectRelation || component instanceof DrawingPropertyRelation)
							{
//								Log.d("DrawingComposite","add component (RELATION) to children at 0: " + component);
								component.parent = this;
								int index = getLastRelationIndex();
								
								children.add(index,component);
							}
							else 
							{
//								Log.d("DrawingComposite","add component to children at end: " + component);
								component.parent = this;
								children.add(component);
							}
							
							Log.d("DrawingComposite","= size0: " + this.children.size() );
						}
					}
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
			else
			{
//				Log.d("DrawingComposite", component + " is NOT in Bounds of " + this);
				
				if (this.getParent() != null || this.isRoot) 
				{
					if (this.getParent() != null)
					{
						component.parent.childrenToAdd.add(component);
					}
					else 
					{
						component.parent = this;
						this.children.add(component);
					}
				}
				else 
				{
//					Log.d("DrawingComposite", "!!!!!!!!!" + component + " this.getParent() == null " + this);
				}
			}

		}
		
		addChildren(component);

	}
	
	private void addChildren(DrawingComponent component)
	{
		for (DrawingComponent child : childrenToAdd)
		{

			if (!(component instanceof CustomObjectRelation) || !(component instanceof DrawingPropertyRelation))
				child.setPathOnTop(true);
			
			child.parent = this;
		}
		
		childrenToAdd.clear();
	}
	


	public int getLastRelationIndex()
	{
		int index = 0;
		
		for (DrawingComponent dc : children)
		{
			if (!(dc instanceof CustomObjectRelation || dc instanceof DrawingPropertyRelation))
			{
				index = children.indexOf(dc);
				if (index > 0) return index;
				else return 0;
			}
		}
		
		return index;
	}

	/**
	 * Remove a component of Type <b>DrawingComponent</b> from the children of
	 * this element
	 * 
	 * @param component
	 *            The element that should be removed
	 */
	public void removeComponent(DrawingComponent component) 
	{
		// TODO: decide whether to delete a complete component or just the
		// selected element of it
		// -> restructuring neccessary

		Log.d("DrawingComposite", "removeComponent " + component);
		
		changed = true;

		if (children.contains(component)) 
		{
			if (component.isComposite) 
			{
				children.remove(component);

				for (DrawingComponent child : ((DrawingComposite) component).children) 
				{
					if (!children.contains(child))
						children.add(child);
				}

				return;

			} else 
			{
				children.remove(component);
				return;
			}
		} 
		else 
		{
			for (DrawingComponent child : children) 
			{
				if (child.isComposite)
					child.removeComponent(component);
			}
		}
	}

	/**
	 * @see com.drawing.datastructure.DrawingComponent#removeCompositeComponent(com.drawing.datastructure.DrawingComponent)
	 */
	public void removeCompositeComponent(DrawingComponent component) 
	{
		Log.d("DrawingComposite", "removeCompositeComponent this: " + this + "  component: "  + component);
		
		changed = true;

		if (children.contains(component)) 
		{
			children.remove(component);
			return;
		} 
		else 
		{
			for (DrawingComponent child : children) 
			{
				if (child.isComposite && !(child instanceof DrawingCompositeWord)) 
				{
					// only consider composite objects that are not an instance
					// of DrawingCompositeWord
					child.removeCompositeComponent(component);
				}
			}
		}
	}

	/**
	 * Getter for the list of child <b>DrawingComponent</b> objects of this
	 * component
	 * 
	 * @return The list list of child <b>DrawingComponent</b> objects of this
	 *         component
	 */
	public List<DrawingComponent> getChild() 
	{
		return children;
	}

	/**
	 * @see com.drawing.datastructure.DrawingComponent#getComponentCount()
	 */
	public int getComponentCount() 
	{
		int count = 0;

		for (DrawingComponent component : children) 
		{
			count += component.getComponentCount();
		}

		return count;
	}

	/**
	 * @see com.drawing.datastructure.DrawingComponent#getPathList()
	 */
	public ScaledPathArray getPathList() 
	{
		if (changed) 
		{
			ScaledPathArray pathArray = new ScaledPathArray();

			if (path != null && !(path.getVertices().size() == 0)) 
			{
				pathArray.getPaths().add(path);

				Matrix matrix = new Matrix();
				matrix.setValues(pathMatrixValues);

				List<Matrix> matrixList = pathArray.getScales();
				matrixList.add(matrix);
				pathArray.setScales(matrixList);

			} 
			else if (path != null) 
			{
				pathArray.getPaths().add(path);

				List<Matrix> matrixList = pathArray.getScales();
				matrixList.add(new Matrix());
				pathArray.setScales(matrixList);
			}

			for (DrawingComponent component : children) 
			{
				pathArray.add(component.getPathList());
			}

			localPathList = pathArray;
			
			changed = false;

			return pathArray;

		} 
		else 
		{
			return localPathList;
		}
	}

	/**
	 * Getter for a list of all highlighted <b>CustomPath</b> objects within the
	 * data structure
	 * 
	 * @return The list of all highlighted <b>CustomPath</b> objects within the
	 *         data structure
	 */
	public List<CustomPath> getHighlightedComponents() {

		List<CustomPath> highlightedPathList = new ArrayList<CustomPath>();

		// ScaledPathArray pathArray = getPathList();

		// TODO: check whether the same could be done with elements of
		// DrawingGroups

		// for (CustomPath path : pathArray.getPaths()) {
		//
		// if (path.isHighlighted() && path.getVertices().size() > 0)
		// highlightedPathList.add(path);
		//
		// }

		for (DrawingComponent child : children)
		{
			if (child instanceof DrawingComposite && !(child instanceof DrawingCompositeWord))
			{
				if (child.isHighlighted())
				{
					highlightedPathList.add(child.getPath());
				}

				highlightedPathList.addAll(((DrawingComposite)child).getHighlightedComponents());
			} 
			else
			{
				if (child instanceof DrawingCompositeWord)
				{
					if (child.isHighlighted() && ((DrawingCompositeWord)child).children.size() > 0)
					{
						highlightedPathList.add(((DrawingCompositeWord) child).children.get(0).getPath());
					}
				} 
				else 
				{
					if (child.isHighlighted()) 
					{
						highlightedPathList.add(child.getPath());
					}
				}
			}
		}

		return highlightedPathList;

	}

	
	/**
	 * @see com.drawing.datastructure.DrawingComponent#getObjectByPathId(java.util.UUID)
	 */
	public DrawingComponent getObjectByPathId(UUID uid)
	{

		DrawingComponent component = null;

		if (path != null) 
		{
			if (path.getUid().compareTo(uid) == 0)
				component = this;
			else 
			{
				for (DrawingComponent child : children)
				{
					component = child.getObjectByPathId(uid);
					
					if (component != null)
						break;

				}
			}

		} 
		else 
		{

			for (DrawingComponent child : children)
			{
				component = child.getObjectByPathId(uid);
				
				if (component != null)
					break;
			}

		}

		return component;

	}

	/**
	 * @see com.drawing.datastructure.DrawingComponent#updatePath(android.graphics.Matrix,
	 *      android.graphics.Matrix, boolean)
	 */
	public void updatePath(Matrix matrix, Matrix backupTransformationMatrix, boolean highlight)
	{
		changed = true;
		
		Log.d("Drawingcomposite","updatePath");

		if (grouped) 
		{
			if (parent != null) 
			{
				parent.updatePath(matrix, backupTransformationMatrix, highlight);
			}

		} 
		else 
		{
			if (path != null) 
			{
				if (!path.isHighlighted()) 
				{

					path.setHighlighted(true);

					path.applyTransformation(backupTransformationMatrix, matrix);

					this.highlighted = true;

					for (DrawingComponent child : children) 
					{
						child.updatePath(matrix, backupTransformationMatrix, true);
					}

				}
				else
				{
					path.setHighlighted(false);

					path.applyTransformation(matrix, backupTransformationMatrix);

					this.highlighted = false;

				}
			}

		}
	}
	
	/**
	 * @see com.drawing.datastructure.DrawingComponent#updateGroupedPath(android.graphics.Matrix, android.graphics.Matrix, boolean)
	 */
	public void updateGroupedPath(Matrix matrix, Matrix backupTransformationMatrix, boolean highlight)
	{
		
		if (path != null ) 
		{
			if (!path.isHighlighted()) 
			{

				if(grouped && highlight != highlighted)
				{
					path.setHighlighted(true);
	
					path.applyTransformation(backupTransformationMatrix, matrix);
	
					this.highlighted = true;
				}
				
				for (DrawingComponent child : children) 
				{
					if(child.isGrouped())
					child.updateGroupedPath(matrix, backupTransformationMatrix, true);
				}

			} 
			else 
			{
				if(grouped && highlight != highlighted)
				{
					path.setHighlighted(false);
	
					path.applyTransformation(matrix, backupTransformationMatrix);
	
					this.highlighted = false;
				
				}
				
				for (DrawingComponent child : children)
				{
					if(child.isGrouped())
					child.updateGroupedPath( backupTransformationMatrix, matrix, false);
				}
			}
		}
		
	}
	

	/**
	 * @see com.drawing.datastructure.DrawingComponent#redrawPathsafterDeserialization()
	 */
	public void redrawPathsafterDeserialization(MainActivity ma) 
	{

		boolean first = true;

		CustomPath tempPath = new CustomPath();

		if (path != null) 
		{
			if ((path.getOntoType() == OntologyObjectTypes.DRAWNPROPERTYRELATION))
			{
				Point p = path.getVertices().get(0);
				
				float radius = (float) ma.getResources().getIntArray(R.array.sizes)[0];
				
				tempPath.moveTo(p.x, p.y);
				tempPath.addCircle(p.x, p.y, (float) radius, Path.Direction.CW);
				
				Point pathPoint = new Point(p.x, p.y-radius); 

				tempPath.moveTo((float)(pathPoint.x-(0.2*radius)), pathPoint.y);
				
				tempPath.lineTo(pathPoint.x+1*radius, pathPoint.y);
				tempPath.lineTo(pathPoint.x+2*radius, pathPoint.y);
				tempPath.lineTo(pathPoint.x+3*radius, pathPoint.y);
				tempPath.lineTo(pathPoint.x+4*radius, pathPoint.y);
				tempPath.lineTo(pathPoint.x+5*radius, pathPoint.y);
				tempPath.lineTo(pathPoint.x+6*radius, pathPoint.y);
				tempPath.lineTo(pathPoint.x+7*radius, pathPoint.y);
				
				tempPath.lineTo(pathPoint.x+7*radius, pathPoint.y+2*radius);
				
				tempPath.lineTo(pathPoint.x+6*radius, pathPoint.y+2*radius);
				tempPath.lineTo(pathPoint.x+5*radius, pathPoint.y+2*radius);
				tempPath.lineTo(pathPoint.x+4*radius, pathPoint.y+2*radius);
				tempPath.lineTo(pathPoint.x+3*radius, pathPoint.y+2*radius);
				tempPath.lineTo(pathPoint.x+2*radius, pathPoint.y+2*radius);
				tempPath.lineTo(pathPoint.x+1*radius, pathPoint.y+2*radius);
				tempPath.lineTo((float)(pathPoint.x-(0.2*radius)), pathPoint.y+2*radius);

				tempPath.close();
			}
			else
			{
				for (Point point : path.getVertices())
				{

					if (first)
					{
						tempPath.moveTo(point.x, point.y);
						first = false;
					}
					else
					{
						tempPath.lineTo(point.x, point.y);
					}
				}

			}
			
			tempPath.setOntoType(path.getOntoType());
			tempPath.setHighlighted(path.isHighlighted());
			tempPath.setType(path.getGestureType());
			tempPath.setColor(path.color);
			tempPath.setVisible(path.isVisible());

			path.set(tempPath);
		}

		for (DrawingComponent child : children) 
		{
			child.redrawPathsafterDeserialization(ma);
			
			child.parent = this;
		}

	}

	/**
	 * @see com.drawing.datastructure.DrawingComponent#determineDrawnExtrema(float[])
	 */
	public float[] determineDrawnExtrema(float[] extrema)
	{
		if (path != null) 
		{
			if (path.minX < extrema[0])
				extrema[0] = path.minX;

			if (path.maxX > extrema[1])
				extrema[1] = path.maxX;

			if (path.minY < extrema[2])
				extrema[2] = path.minY;

			if (path.maxY > extrema[3])
				extrema[3] = path.maxY;
		}

		for (DrawingComponent child : children) 
		{
			extrema = child.determineDrawnExtrema(extrema);
		}

		return extrema;

	}

	/**
	 * @see com.drawing.datastructure.DrawingComponent#ContainsContactObjects()
	 */
	public boolean ContainsFormalizedIndividualObjects() 
	{
		for (DrawingComponent child : children) 
		{
			if (child.ContainsFormalizedIndividualObjects()) 
			{
				return true;
			}
		}

		return false;
	}
	
	public boolean ContainsFormalizedConceptObjects()
	{
		for (DrawingComponent child : children) 
		{
			if (child.ContainsFormalizedConceptObjects()) 
			{
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Getter for LeafState
	 */
	public boolean hasLeafState() 
	{
		return leafState;
	}

	
	/**
	 * Drawing function for the objects that represent a Contact from the
	 * address book of the device
	 * 
	 * @param canvas
	 *            The <b>Canvas</b> object associated with the respective
	 *            <b>DrawView</b>
	 * @param matrix
	 *            The current transformation Matrix of the <b>DrawView</b>
	 * @param backupTransformationMatrix The current backup transformation matrix of the {@link DrawView} 
	 * @param containsHighlight True if there are highlighted path objects
	 */
	public void drawFormalizedConceptObjects(Canvas canvas, Matrix matrix, Matrix backupTransformationMatrix, boolean containsHighlight) 
	{
		for (DrawingComponent child : children)
		{
			if (child instanceof FormalizedConcept) 
			{
				if (containsHighlight) 
				{
					if (child.highlighted)
						((FormalizedConcept) child).drawFormalizedConcept(canvas, matrix);
					else
						((FormalizedConcept) child).drawFormalizedConcept(canvas, backupTransformationMatrix);
				} else
					((FormalizedConcept) child).drawFormalizedConcept(canvas, matrix);

			} 
			else if (child.isComposite) 
			{
				((DrawingComposite) child).drawFormalizedConceptObjects(canvas, matrix, backupTransformationMatrix, containsHighlight);
			}

		}

	}
	
	/**
	 * Drawing function for the objects that represent a Contact from the
	 * address book of the device
	 * 
	 * @param canvas
	 *            The <b>Canvas</b> object associated with the respective
	 *            <b>DrawView</b>
	 * @param matrix
	 *            The current transformation Matrix of the <b>DrawView</b>
	 * @param backupTransformationMatrix The current backup transformation matrix of the {@link DrawView} 
	 * @param containsHighlight True if there are highlighted path objects
	 */
	public void drawFormalizedIndividualObjects(Canvas canvas, Matrix matrix, Matrix backupTransformationMatrix, boolean containsHighlight) 
	{

		for (DrawingComponent child : children)
		{
			if (child instanceof FormalizedIndividual) 
			{
				if (containsHighlight) 
				{
					if (child.highlighted)
						((FormalizedIndividual) child).drawFormalizedIndividual(canvas, matrix);
					else
						((FormalizedIndividual) child).drawFormalizedIndividual(canvas, backupTransformationMatrix);
				} else
					((FormalizedIndividual) child).drawFormalizedIndividual(canvas, matrix);

			} 
			else if (child.isComposite) 
			{
				((DrawingComposite) child).drawFormalizedIndividualObjects(canvas, matrix, backupTransformationMatrix, containsHighlight);
			}

		}

	}

	/**
	 * Match the bounds of a recognized gesture with existing objects in order to figure out whether there is an intersection
	 * @param bounds The bounds that represent the hit area
	 * @return The {@link DrawingCompositeWord} object that is intersected by the respective bounding rectangle
	 */
	public DrawingCompositeWord retrieveWordMatch(RectF bounds) 
	{	
		DrawingCompositeWord wordMatch = null;

		RectF childBounds = new RectF();

		for (DrawingComponent child : children)
		{
			if (child instanceof DrawingCompositeWord)
			{

				boolean first = true;
				RectF tempBounds = new RectF();

				for (DrawingComponent compositeWordChild : ((DrawingCompositeWord) child).children)
				{
					if (first)
					{
						compositeWordChild.getPath().computeBounds(childBounds, true);
						first = false;
					} 
					else
					{
						compositeWordChild.getPath().computeBounds(tempBounds, true);
						childBounds.union(tempBounds);
					}
				}

				if (RectF.intersects(bounds, childBounds)) 
				{
					return (DrawingCompositeWord) child;
				}

			}
			else
			{
				if(child instanceof DrawingComposite)
				{
					wordMatch = ((DrawingComposite) child).retrieveWordMatch(bounds);
				}
			}
		}
		
		return wordMatch;
	}

	/**
	 * Method that collects all included objects of this {@link DrawingComposite}
	 * @return The list including all {@link DrawingComponent} objects of this element
	 */
	public ArrayList<DrawingComponent> getLinearChildObjectList()
	{

		ArrayList<DrawingComponent> linearComponentList = new ArrayList<DrawingComponent>();

		for(DrawingComponent child : children)
		{			
			
			if(child instanceof DrawingComposite)
			linearComponentList.addAll((Collection<? extends DrawingComponent>) ((DrawingComposite)child).getLinearChildObjectList());
			else
			{
				if(!(child instanceof DrawingWordLetter) && !linearComponentList.contains(child))
				{
					linearComponentList.add(child);	
				}	
			}
		}
		
		if(! (this instanceof DrawingCompositeWord))
		children.clear();
		
		linearComponentList.add(this);
		
		return linearComponentList;
	}




}
