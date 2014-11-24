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
 
package com.drawing.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.DiscretePathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.SumPathEffect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.drawing.datastructure.CustomObjectRelation;
import com.drawing.datastructure.CustomPath;
import com.drawing.datastructure.DisplayObjectState;
import com.drawing.datastructure.DrawingComponent;
import com.drawing.datastructure.DrawingComposite;
import com.drawing.datastructure.DrawingCompositeWord;
import com.drawing.datastructure.DrawingConcept;
import com.drawing.datastructure.DrawingGroup;
import com.drawing.datastructure.DrawingIndividual;
import com.drawing.datastructure.DrawingLeaf;
import com.drawing.datastructure.DrawingPropertyRelation;
import com.drawing.datastructure.DrawingWordLetter;
import com.drawing.datastructure.FormalizedConcept;
import com.drawing.datastructure.FormalizedIndividual;
import com.drawing.datastructure.FormalizedObject;
import com.drawing.datastructure.FormalizedPropertyRelation;
import com.drawing.datastructure.FormalizedPropertyRelationButton;
import com.drawing.datastructure.GestureTypes;
import com.drawing.datastructure.InstatiationRelation;
import com.drawing.datastructure.OntologyObjectTypes;
import com.drawing.datastructure.PropertyRelation;
import com.drawing.datastructure.ScaledPathArray;
import com.drawing.datastructure.SubClassRelation;
import com.drawing.gestures.Point;
import com.drawing.gestures.Recognizer;
import com.drawing.gestures.Result;
import com.drawing.ontosketch.R;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * SkechBoard
 * @author Florian Schneider TU Dresden / SAP NEXT Dresden
 * @author Christian Brändel TU Dresden / SAP Research Dresden
 * 
 */

@TargetApi(13)
public class DrawView extends GeneralView implements OnTouchListener, MainActivity.OnInkViewListener {

	static private LinearLayout alternativeWordListView;

	/**
	 * minimal vertex count of a path that should be recognized
	 */
	static final int MINRECOGNITIONLENGTH = 10;

	/**
	 * Threshold for checking if a zoom event was detected
	 */
	static final float threshold = 50f;

	/**
	 * Set to true if the pan mode was enabled by the System menu
	 */
	private boolean scaleView = false;

	/**
	 * helper array for displaying and storing temporary paths
	 */
	List<CustomPath> newPaths = new ArrayList<CustomPath>();

	/**
	 * Annotation management object
	 */
	ScaledPathArray annotationArray = new ScaledPathArray();

	/**
	 * Helper object for immediate display of new paths
	 */
	CustomPath tempPath = new CustomPath();

	Paint defaultPaint = new Paint();
	
	/**
	 * Abstract <b>Paint</b> as a placeholder for specific relations paints
	 */
	Paint relationPaint;
	
	Paint subclassRelationPaint = new Paint();
	Paint instanziiationRelationPaint = new Paint();
	Paint propertyRelationPaint = new Paint();
	Paint filledInstanziiationRelationPaint = new Paint();

	/**
	 * The <b>Paint</b> for background of Concepts and Individuals
	 */
	private Paint filledPaint = new Paint();
	
	/**
	 * The <b>Paint</b> for Text (handwriting recognition output)
	 */
	Paint textPaint = new Paint();

	/**
	 * true if a new path was created and no vertices were attached to it so far
	 */
	boolean firstPointOfPath = true;

	/**
	 * The center between two contact points in case of multi touch
	 */
	PointF mid = new PointF();

	/**
	 * helper distance for the calculation of zoom gestures
	 */
	float oldDist = 1f;
	
	/**
	 * The scale parameter that is applied upon the canvas' transformation
	 * <b>Matrix</b>
	 */
	float scale = 1f;

	/**
	 * The time span that a single touch interaction did last
	 */
	float downTime = -1;

	/**
	 * modified $1 Gesture Recognizer
	 */
	Recognizer recognizer;
	
	/**
	 * Result of the modified $1 Gesture Recognizer
	 */
	Result result;
	
	/**
	 * Input points for the modified $1 gesture recognizer
	 */
	List<Point> pointsForRecognizer;

	/**
	 * debug object
	 */
	Paint debugPaint = new Paint();
	Paint debugPaint2 = new Paint();
	
	/**
	 * debug object
	 */
	List<Point[]> debugList = new ArrayList<Point[]>();

	private boolean changeAlternatives;

	private List<CustomPath> tempHandwritingPaths;

	private boolean gestureRecognitionEnabled;

	/**
	 * Boolean to manage scale
	 */
	private boolean allowScale = true;
	
	/**
	 * Boolean to manage pan
	 */
	private boolean allowTranslate = true;
	
	/**
	 * Boolean to manage drawing of debug rectangles
	 */
	public boolean drawDebugRegions = false;
	
	private float instanziationStrokeSize;
	private RectF instanziationStrokeRect;

	private Context context;
	
	/**
	 * Constructor of the DrawView
	 * 
	 * @param context
	 * @param attrSet
	 * @see android.widget.ImageView
	 */
	public DrawView(Context context, AttributeSet attrSet) 
	{
		super(context, attrSet);
		this.context = context;

		setFocusable(true);
		setFocusableInTouchMode(true);

		this.setOnTouchListener(this);

		// initialize stroke paints
		paint.setColor(getResources().getColor(R.color.sketch));
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2.5f);

		filledPaint.setColor(getResources().getColor(R.color.sketchBackground));
		filledPaint.setAntiAlias(true);
		filledPaint.setStyle(Paint.Style.FILL);
		filledPaint.setStrokeWidth(2.5f);
		
		defaultPaint = paint;

		// tint path effect
		PathEffect effect = new CornerPathEffect(20);
		PathEffect randomLines = new DiscretePathEffect(5, 2);
		PathEffect dashEffect = new DashPathEffect(new float[] { 2, 1, 5, 0, 3,
				8, 7, 4, 2, 3 }, 5);
		PathEffect sumEffect = new SumPathEffect(effect, randomLines);

		@SuppressWarnings("unused")
		PathEffect composeEffect = new SumPathEffect(dashEffect, sumEffect);

		highlightedPaint.setColor(getResources().getColor(R.color.highlight));
		highlightedPaint.setAntiAlias(true);
		highlightedPaint.setStyle(Paint.Style.STROKE);
		highlightedPaint.setStrokeWidth(3);

		highlightedPaint.setPathEffect(effect);
		paint.setPathEffect(effect);
		filledPaint.setPathEffect(effect);
		
		//propertyRelationPaint
		propertyRelationPaint.setColor(getResources().getColor(R.color.sketch));
		propertyRelationPaint.setAntiAlias(true);
		propertyRelationPaint.setStyle(Paint.Style.STROKE);
		propertyRelationPaint.setStrokeWidth(2.5f);
		PathEffect dashEffect2 = new DashPathEffect(new float[] { 20, 20}, 0);
		propertyRelationPaint.setPathEffect(dashEffect2);

		
		//instanziiationRelationPaint
		instanziiationRelationPaint.setColor(getResources().getColor(R.color.inactiveGray));
		instanziiationRelationPaint.setAntiAlias(true);
		instanziiationRelationPaint.setStyle(Paint.Style.STROKE);
		instanziiationRelationPaint.setStrokeWidth(50f);
		
		filledInstanziiationRelationPaint.setColor(getResources().getColor(R.color.inactiveGray));
		filledInstanziiationRelationPaint.setAntiAlias(true);
		filledInstanziiationRelationPaint.setStyle(Paint.Style.FILL);
		filledInstanziiationRelationPaint.setStrokeWidth(2.5f);
		PathEffect roundEffect = new CornerPathEffect(25);
		filledInstanziiationRelationPaint.setPathEffect(roundEffect);
		//subclassRelationPaint
		
		subclassRelationPaint.setColor(getResources().getColor(R.color.black));
		subclassRelationPaint.setAntiAlias(true);
		subclassRelationPaint.setStyle(Paint.Style.STROKE);
		subclassRelationPaint.setStrokeWidth(3f);
		
		// setup recognizer
		recognizer = new Recognizer();
		result = null;
		pointsForRecognizer = new ArrayList<Point>();

		debugPaint.setColor(Color.RED);
		debugPaint.setAntiAlias(true);
		debugPaint.setStyle(Paint.Style.STROKE);
		debugPaint.setStrokeWidth(1.5f);
		debugPaint.setAlpha(100);
		
		debugPaint2.setColor(Color.YELLOW);
		debugPaint2.setAntiAlias(true);
		debugPaint2.setStyle(Paint.Style.STROKE);
		debugPaint2.setStrokeWidth(1.5f);

		drawingObjects = new DrawingComposite(null, null);
		drawingObjects.setIsRoot(true);

		textPaint.setColor(Color.GRAY);
		textPaint.setTextSize(30);
		textPaint.setStrokeWidth(3);

		tempHandwritingPaths = new ArrayList<CustomPath>();
		changeAlternatives = false;

		gestureRecognitionEnabled = true;

		inverse = new Matrix();

		backupInverse = new Matrix();

		drawPath = new CustomPath();

		instanziationStrokeSize = (float) getResources().getIntArray(R.array.sizes)[7];
	}
	
	private CustomPath drawPath = new CustomPath();


	/**
	 * onDraw method that is called after each invalidate() - call
	 */
	protected void onDraw(Canvas canvas) 
	{
		matrix.invert(inverse);

		backupTransformationMatrix.invert(backupInverse);

		canvas.clipRect(0, 0, screenWidth, screenHeight);

		canvas.save();

		if (calculateCanvasSize) 
		{
			float[] scale = new float[] { canvas.getHeight(), 0 };

			inverse.mapPoints(scale);

			canvasHeight = (int) scale[0];

			calculateCanvasSize = false;
		}

		// apply transformations only to highlighted path objects
		if (drawingObjects.getPathList().ContainsHighlightedPath()) 
		{

			// draw highlighted paths

			Log.d("DrawView", "onDraw ==========================================");
			
			for (int i = 0; i < drawingObjects.getPathList().getPaths().size(); i++) 
			{
				drawPath = drawingObjects.getPathList().getPath(i);
				
				DrawingComponent dc = drawingObjects.getObjectByPathId(drawPath.getUid());

				paint.setColor(drawPath.getColor());
				
				if( dc instanceof InstatiationRelation)
				{
					Log.d("DrawView", "IR " + dc.isHighlighted());
				}
				
				instanziationStrokeRect = new RectF(0, 0, instanziationStrokeSize, instanziationStrokeSize);

				// apply the inverse matrix of the canvas upon the path
				if (drawPath.isHighlighted()) 
				{
					
					///////////////////////////////////////////
					///////// draw highlighted paths //////////
					///////////////////////////////////////////
					
					if (dc instanceof CustomObjectRelation)
					{
						Point startp = ((CustomObjectRelation) dc).getStartElementCenterPoint();
						float[] anchor_start = new float[] { startp.x, startp.y};
						
						Point endp = ((CustomObjectRelation) dc).getEndElementCenterPoint();
						float[] anchor_end = new float[] { endp.x, endp.y};
						
						if (((CustomObjectRelation) dc).getStartElement().isHighlighted())
						{
							matrix.mapPoints(anchor_start);
						}
						else
						{
							backupTransformationMatrix.mapPoints(anchor_start);
						}
						
						if (((CustomObjectRelation) dc).getEndElement().isHighlighted())
						{
							matrix.mapPoints(anchor_end);
						}
						else 
						{
							backupTransformationMatrix.mapPoints(anchor_end);
						}
						
						((CustomObjectRelation) dc).setStartPoint(new Point(anchor_start[0], anchor_start[1]));
						((CustomObjectRelation) dc).setEndPoint(new Point(anchor_end[0], anchor_end[1]));
						

						if (!(dc instanceof PropertyRelation))
						{			
							RectF bounds = new RectF();
							dc.getPath().computeBounds(bounds, true);
							
							backupTransformationMatrix.mapRect(bounds);
							
							float radius = bounds.height()/2;
							
							Point newStartp = ((CustomObjectRelation) dc).getStartPoint();
							Point newEndp = ((CustomObjectRelation) dc).getEndPoint(); 
							
							Point newCenterp = getCenterPoint(newStartp, newEndp);

							dc.getPath().reset();
							
							dc.getPath().moveTo(newCenterp.x, newCenterp.y);
							
							if (dc instanceof SubClassRelation)
							{
								dc.getPath().addCircle(newCenterp.x, newCenterp.y, radius, Path.Direction.CW);
							}
							else if (dc instanceof InstatiationRelation)
							{
								dc.getPath().addCircle(newCenterp.x, newCenterp.y, radius, Path.Direction.CW);
							}

							List<Point> vertices = dc.getPath().getVertices();
							
							dc.getPath().transform(backupInverse);

							dc.getPath().setVertices(CustomPath.transformVertices(vertices, backupInverse));
						}
					
						Path p = new Path();
						
						startp = ((CustomObjectRelation) dc).getStartPoint();
						Point centerp = dc.getCenterPoint();
						endp = ((CustomObjectRelation) dc).getEndPoint();
						
						float[] anchor_old = new float[] { startp.x, startp.y, endp.x, endp.y };
						float[] anchor_current = new float[] { centerp.x, centerp.y};
						
						matrix.mapPoints(anchor_current);
						
						if (dc instanceof PropertyRelation)
						{
							p.moveTo(anchor_old[0], anchor_old[1]);
							
							p.lineTo(anchor_current[0], anchor_current[1]);
							p.lineTo(anchor_old[2], anchor_old[3]);
							
							propertyRelationPaint.setColor(drawPath.getColor());
							relationPaint = propertyRelationPaint;
						}
						else if (dc instanceof SubClassRelation)
						{
							p.moveTo(anchor_old[0], anchor_old[1]);
							
							p.lineTo(anchor_old[2], anchor_old[3]);
							
							((SubClassRelation) dc).updateArrow(canvas, matrix, getCenterPoint(new Point(anchor_old[0], anchor_old[1]), new Point(anchor_old[2], anchor_old[3])), new Point(anchor_old[2], anchor_old[3]));

							
							relationPaint = subclassRelationPaint;
						}
						else if (dc instanceof InstatiationRelation)
						{
							p.moveTo(anchor_old[0], anchor_old[1]);

							p.lineTo(anchor_old[2], anchor_old[3]);
							
							RectF startBounds = new RectF();
							((CustomObjectRelation) dc).getStartElement().getPath().computeBounds(startBounds, true);
							
							startBounds.bottom = (float) (startBounds.bottom+12);
							startBounds.top = (float) (startBounds.top-12);
							startBounds.left = (float) (startBounds.left-10);
							startBounds.right = (float) (startBounds.right+10);
							
							RectF endBounds = new RectF();
							((CustomObjectRelation) dc).getEndElement().getPath().computeBounds(endBounds, true);
							
							endBounds.bottom = (float) (endBounds.bottom+12);
							endBounds.top = (float) (endBounds.top-12);
							endBounds.left = (float) (endBounds.left-10);
							endBounds.right = (float) (endBounds.right+10);

							backupTransformationMatrix.mapRect(startBounds);
							backupTransformationMatrix.mapRect(endBounds);
							
							filledInstanziiationRelationPaint.setAlpha(dc.getAlpha());
							
							canvas.drawRect(startBounds, filledInstanziiationRelationPaint);
							canvas.drawRect(endBounds, filledInstanziiationRelationPaint);
							
							backupTransformationMatrix.mapRect(instanziationStrokeRect);
							instanziiationRelationPaint.setStrokeWidth(instanziationStrokeRect.height());
							
							relationPaint = instanziiationRelationPaint;
						}

						relationPaint.setAlpha(dc.getAlpha());
						
						canvas.drawPath(p, relationPaint);

					}
					
					if (dc instanceof DrawingPropertyRelation )
					{
						Point startp = ((DrawingPropertyRelation) dc).getStartElementCenterPoint();
						float[] anchor_start = new float[] { startp.x, startp.y};
						
						Point endp = ((DrawingPropertyRelation) dc).getEndElementCenterPoint();
						float[] anchor_end = new float[] { endp.x, endp.y};
						
						if (((DrawingPropertyRelation) dc).getStartElement().isHighlighted())
						{
							matrix.mapPoints(anchor_start);
						}
						else
						{
							backupTransformationMatrix.mapPoints(anchor_start);
						}
						
						if (((DrawingPropertyRelation) dc).getEndElement().isHighlighted())
						{
							matrix.mapPoints(anchor_end);
						}
						else 
						{
							backupTransformationMatrix.mapPoints(anchor_end);
						}
						
						((DrawingPropertyRelation) dc).setStartPoint(new Point(anchor_start[0], anchor_start[1]));
						((DrawingPropertyRelation) dc).setEndPoint(new Point(anchor_end[0], anchor_end[1]));

						Path p = new Path();
						
						startp = ((DrawingPropertyRelation) dc).getStartPoint();
						Point centerp = dc.getCenterPoint();
						endp = ((DrawingPropertyRelation) dc).getEndPoint();
						
						float[] anchor_old = new float[] { startp.x, startp.y, endp.x, endp.y };
						float[] anchor_current = new float[] { centerp.x, centerp.y};

						matrix.mapPoints(anchor_current);
	
						p.moveTo(anchor_old[0], anchor_old[1]);
						
						p.lineTo(anchor_current[0], anchor_current[1]);
						p.lineTo(anchor_old[2], anchor_old[3]);
						
						propertyRelationPaint.setColor(drawPath.getColor());
						relationPaint = propertyRelationPaint;
						
						relationPaint.setAlpha(dc.getAlpha());
						
						canvas.drawPath(p, relationPaint);
						
						if (((DrawingPropertyRelation) dc).formalizedRelationbuttons != null)
						{
							if (((DrawingPropertyRelation) dc).formalizedRelationbuttons.size() > 0)
							{
								int count = 1;
								
								Point p2 = new Point(anchor_current[0], anchor_current[1]);
								
								RectF r = new RectF();
								
								CustomPath cp = new CustomPath();
								cp.set(dc.getPath());
								
								cp.transform(matrix);
								
								cp.computeBounds(r, true);
								float radius = r.height()/2;
								
								for (FormalizedPropertyRelationButton fb : ((DrawingPropertyRelation) dc).formalizedRelationbuttons)
								{
									fb.getPath().reset();
									
									p2.y = (float) (anchor_current[1] + ((radius*2) * count));
									
									fb.getPath().moveTo(p2.x, p2.y);
									fb.getPath().addCircle(p2.x, p2.y, radius, Path.Direction.CW);
									
									List<Point> vertices = fb.getPath().getVertices();
									
									fb.getPath().transform(backupInverse);

									fb.getPath().setVertices(CustomPath.transformVertices(vertices, backupInverse));
									
									count++;
								}
							}
						}
					}
					
					if (dc instanceof PropertyRelation)
					{
						((PropertyRelation) dc).drawPropertyRelation(canvas, matrix);
					} 
					else if (dc instanceof SubClassRelation)
					{
						((SubClassRelation) dc).drawPropertyRelationHelpText(canvas, matrix);
					}
					
					drawPath.transform(matrix);
					
					try
					{
						if (drawPath.isVisible())
						{
							highlightedPaint.setAlpha(dc.getAlpha());
						}
						else
						{
							highlightedPaint.setAlpha(0);
						}
					}
					catch(Exception e){System.out.print(e);}

					if( dc instanceof DrawingIndividual || dc instanceof DrawingConcept)
					{
						// fill the background
						filledPaint.setAlpha(dc.getAlpha());
						filledPaint.setColor(getResources().getColor(R.color.sketchBackground));
						canvas.drawPath(drawPath, filledPaint);
					}

					if (dc instanceof SubClassRelation || dc instanceof PropertyRelation)
					{
						// draw the path
						// fill the background
						filledPaint.setColor(drawPath.getColor());
						filledPaint.setAlpha(dc.getAlpha());
						canvas.drawPath(drawPath, filledPaint);
					}
					
					if (dc instanceof DrawingPropertyRelation)
					{
						filledPaint.setColor(getResources().getColor(R.color.sketchBackground));
						filledPaint.setAlpha(dc.getAlpha());
						canvas.drawPath(drawPath, filledPaint);
					}
					
					// draw the path
					canvas.drawPath(drawPath, highlightedPaint);
					
					if (dc instanceof PropertyRelation)
					{
						((PropertyRelation) dc).updateArrow(canvas, matrix);
					}
					else if (dc instanceof SubClassRelation)
					{
						((SubClassRelation) dc).drawRelationIcon(canvas, matrix);
					}
					else if (dc instanceof DrawingConcept)
					{
						((DrawingConcept) dc).drawDrawingConceptIcon(canvas, matrix);
					}
					else if (dc instanceof DrawingIndividual)
					{
						((DrawingIndividual) dc).drawDrawingIndividualIcon(canvas, matrix);
					}
					else if (dc instanceof DrawingPropertyRelation)
					{
						((DrawingPropertyRelation) dc).updateArrow(canvas, matrix);
					}
					
					
					// restore the initial path object with the original
					// transformation
					// Matrix

					drawPath.transform(inverse);
					
				} 
				else 
				{
					///////////////////////////////////////////
					//////// draw not highlighted paths ///////
					///////////////////////////////////////////
					
					if ((dc instanceof FormalizedPropertyRelationButton) && dc.getPath().isVisible() == false) break;
					
					
					if (dc instanceof CustomObjectRelation && !(dc instanceof FormalizedPropertyRelationButton))
					{
						Point startp = ((CustomObjectRelation) dc).getStartElementCenterPoint();
						float[] anchor_start = new float[] { startp.x, startp.y};
						
						Point endp = ((CustomObjectRelation) dc).getEndElementCenterPoint();
						float[] anchor_end = new float[] { endp.x, endp.y};
						
						if (((CustomObjectRelation) dc).getStartElement().isHighlighted())
						{
							matrix.mapPoints(anchor_start);
						}
						else
						{
							backupTransformationMatrix.mapPoints(anchor_start);
						}
						
						if (((CustomObjectRelation) dc).getEndElement().isHighlighted())
						{
							matrix.mapPoints(anchor_end);
						}
						else 
						{
							backupTransformationMatrix.mapPoints(anchor_end);
						}
						
						((CustomObjectRelation) dc).setStartPoint(new Point(anchor_start[0], anchor_start[1]));
						((CustomObjectRelation) dc).setEndPoint(new Point(anchor_end[0], anchor_end[1]));
						
						if (!(dc instanceof PropertyRelation))
						{

							RectF bounds = new RectF();
							dc.getPath().computeBounds(bounds, true);
							
							backupTransformationMatrix.mapRect(bounds);
							
							float radius = bounds.height()/2;
							
							Point newStartp = ((CustomObjectRelation) dc).getStartPoint();
							Point newEndp = ((CustomObjectRelation) dc).getEndPoint(); 
							
							Point newCenterp = getCenterPoint(newStartp, newEndp);

							dc.getPath().reset();
							
							dc.getPath().moveTo(newCenterp.x, newCenterp.y);
							
							if (dc instanceof SubClassRelation)
							{
								dc.getPath().addCircle(newCenterp.x, newCenterp.y, radius, Path.Direction.CW);
							}
							else if (dc instanceof InstatiationRelation)
							{
								dc.getPath().addCircle(newCenterp.x, newCenterp.y, radius, Path.Direction.CW);
							}

							List<Point> vertices = dc.getPath().getVertices();
							
							dc.getPath().transform(backupInverse);

							dc.getPath().setVertices(CustomPath.transformVertices(vertices, backupInverse));

						}
						
						Path p = new Path();
						
						startp = ((CustomObjectRelation) dc).getStartPoint();
						Point centerp = dc.getCenterPoint();
						endp = ((CustomObjectRelation) dc).getEndPoint();
						
						float[] anchor_old_start = new float[] { startp.x, startp.y};
						float[] anchor_old_end = new float[] { endp.x, endp.y };
						float[] anchor_current = new float[] { centerp.x, centerp.y};
						
						backupTransformationMatrix.mapPoints(anchor_current);

						if (dc instanceof PropertyRelation)
						{
							p.moveTo(anchor_old_start[0], anchor_old_start[1]);
							
							p.lineTo(anchor_current[0], anchor_current[1]);
							p.lineTo(anchor_old_end[0], anchor_old_end[1]);
							
							propertyRelationPaint.setColor(drawPath.getColor());
							relationPaint = propertyRelationPaint;
						}
						else if (dc instanceof SubClassRelation)
						{
							
							p.moveTo(anchor_old_start[0], anchor_old_start[1]);

							p.lineTo(anchor_old_end[0], anchor_old_end[1]);
							
							((SubClassRelation) dc).updateArrow(canvas, backupTransformationMatrix, getCenterPoint(new Point(anchor_old_start[0], anchor_old_start[1]), new Point(anchor_old_end[0], anchor_old_end[1])), new Point(anchor_old_end[0], anchor_old_end[1]));

							relationPaint = subclassRelationPaint;
						}
						else if (dc instanceof InstatiationRelation)
						{
							p.moveTo(anchor_old_start[0], anchor_old_start[1]);
							
							p.lineTo(anchor_old_end[0], anchor_old_end[1]);
							
							RectF startBounds = new RectF();
							((CustomObjectRelation) dc).getStartElement().getPath().computeBounds(startBounds, true);
							
							startBounds.bottom = (float) (startBounds.bottom+12);
							startBounds.top = (float) (startBounds.top-12);
							startBounds.left = (float) (startBounds.left-10);
							startBounds.right = (float) (startBounds.right+10);
							
							RectF endBounds = new RectF();
							((CustomObjectRelation) dc).getEndElement().getPath().computeBounds(endBounds, true);
							
							endBounds.bottom = (float) (endBounds.bottom+12);
							endBounds.top = (float) (endBounds.top-12);
							endBounds.left = (float) (endBounds.left-10);
							endBounds.right = (float) (endBounds.right+10);

							if (((CustomObjectRelation) dc).getStartElement().isHighlighted())
							{
								matrix.mapRect(startBounds);
								backupTransformationMatrix.mapRect(endBounds);
							}
							else if (((CustomObjectRelation) dc).getEndElement().isHighlighted())
							{
								backupTransformationMatrix.mapRect(startBounds);
								matrix.mapRect(endBounds);
							}
							else 
							{
								backupTransformationMatrix.mapRect(startBounds);
								backupTransformationMatrix.mapRect(endBounds);
							}

							filledInstanziiationRelationPaint.setAlpha(dc.getAlpha());
							
							canvas.drawRect(startBounds, filledInstanziiationRelationPaint);
							canvas.drawRect(endBounds, filledInstanziiationRelationPaint);
							
							backupTransformationMatrix.mapRect(instanziationStrokeRect);
							instanziiationRelationPaint.setStrokeWidth(instanziationStrokeRect.height());
							
							relationPaint = instanziiationRelationPaint;
						}

						relationPaint.setAlpha(dc.getAlpha());
						
						canvas.drawPath(p, relationPaint);
					}
					
					if (dc instanceof DrawingPropertyRelation)
					{
						Point startp = ((DrawingPropertyRelation) dc).getStartElementCenterPoint();
						float[] anchor_start = new float[] { startp.x, startp.y};
						
						Point endp = ((DrawingPropertyRelation) dc).getEndElementCenterPoint();
						float[] anchor_end = new float[] { endp.x, endp.y};
						
						if (((DrawingPropertyRelation) dc).getStartElement().isHighlighted())
						{
							matrix.mapPoints(anchor_start);
						}
						else
						{
							backupTransformationMatrix.mapPoints(anchor_start);
						}
						
						if (((DrawingPropertyRelation) dc).getEndElement().isHighlighted())
						{
							matrix.mapPoints(anchor_end);
						}
						else 
						{
							backupTransformationMatrix.mapPoints(anchor_end);
						}
						
						((DrawingPropertyRelation) dc).setStartPoint(new Point(anchor_start[0], anchor_start[1]));
						((DrawingPropertyRelation) dc).setEndPoint(new Point(anchor_end[0], anchor_end[1]));
					
						Path p = new Path();
						
						startp = ((DrawingPropertyRelation) dc).getStartPoint();
						Point centerp = dc.getCenterPoint();
						endp = ((DrawingPropertyRelation) dc).getEndPoint();
						
						float[] anchor_old_start = new float[] { startp.x, startp.y};
						float[] anchor_old_end = new float[] { endp.x, endp.y };
						float[] anchor_current = new float[] { centerp.x, centerp.y};
					
						backupTransformationMatrix.mapPoints(anchor_current);

						p.moveTo(anchor_old_start[0], anchor_old_start[1]);
						
						p.lineTo(anchor_current[0], anchor_current[1]);
						p.lineTo(anchor_old_end[0], anchor_old_end[1]);
						
						propertyRelationPaint.setColor(drawPath.getColor());
						relationPaint = propertyRelationPaint;
				
						relationPaint.setAlpha(dc.getAlpha());
						
						canvas.drawPath(p, relationPaint);

					}
					
					if (dc instanceof PropertyRelation)
					{
						((PropertyRelation) dc).drawPropertyRelation(canvas, backupTransformationMatrix);
					}
					else if (dc instanceof SubClassRelation)
					{
						((SubClassRelation) dc).drawPropertyRelationHelpText(canvas, backupTransformationMatrix);
					}
					
					drawPath.transform(backupTransformationMatrix);
					
					if( dc instanceof DrawingIndividual || dc instanceof DrawingConcept)
					{
						// fill the background
						filledPaint.setColor(getResources().getColor(R.color.sketchBackground));
						filledPaint.setAlpha(dc.getAlpha());
						canvas.drawPath(drawPath, filledPaint);
					}

					if (dc instanceof SubClassRelation || dc instanceof PropertyRelation )
					{
						// draw the path
						// fill the background
						filledPaint.setColor(drawPath.getColor());
						filledPaint.setAlpha(dc.getAlpha());
						canvas.drawPath(drawPath, filledPaint);

					}
					
					if (dc instanceof DrawingPropertyRelation)
					{
						filledPaint.setColor(getResources().getColor(R.color.sketchBackground));
						filledPaint.setAlpha(dc.getAlpha());
						canvas.drawPath(drawPath, filledPaint);
					}
					
					if (drawPath.isVisible())
					{
						paint.setAlpha(dc.getAlpha());
					}
					else
					{
						paint.setAlpha(0);
					}
					
					canvas.drawPath(drawPath, paint);
					
					drawPath.transform(backupInverse);
					
					if (dc instanceof PropertyRelation)
					{
						((PropertyRelation) dc).updateArrow(canvas, backupTransformationMatrix);
					}
					else if (dc instanceof SubClassRelation)
					{
						((SubClassRelation) dc).drawRelationIcon(canvas, backupTransformationMatrix);
					}
					else if (dc instanceof DrawingConcept)
					{
						((DrawingConcept) dc).drawDrawingConceptIcon(canvas, backupTransformationMatrix);
					}
					else if (dc instanceof DrawingIndividual)
					{
						((DrawingIndividual) dc).drawDrawingIndividualIcon(canvas, backupTransformationMatrix);
					}
					else if (dc instanceof DrawingPropertyRelation)
					{
						((DrawingPropertyRelation) dc).updateArrow(canvas, backupTransformationMatrix);
					}
				}
			}
			
			if (drawingObjects.ContainsFormalizedConceptObjects()) 
			{
				drawingObjects.drawFormalizedConceptObjects(canvas, matrix, backupTransformationMatrix, true);
			}
			
			if (drawingObjects.ContainsFormalizedIndividualObjects()) 
			{
				drawingObjects.drawFormalizedIndividualObjects(canvas, matrix, backupTransformationMatrix, true);
			}

		} 
		else 
		{
			///////////////////////////////////////////
			//////////// draw stored paths ////////////
			///////////////////////////////////////////

			Log.d("DrawView",  " size: " + drawingObjects.getPathList().getPaths().size());

			for (int i = 0; i < drawingObjects.getPathList().getPaths().size(); i++) 
			{

				instanziationStrokeRect = new RectF(0, 0, instanziationStrokeSize, instanziationStrokeSize);
				
				drawPath = drawingObjects.getPathList().getPath(i);
				
				DrawingComponent dc = drawingObjects.getObjectByPathId(drawPath.getUid());
				
				paint.setColor(drawPath.getColor());
				
				if (dc != null)
				{
					if (drawPath.isVisible())
					{
						paint.setAlpha(dc.getAlpha());
					}
					else
					{
						paint.setAlpha(0);
					}

				}
				
				if (dc instanceof CustomObjectRelation && !(dc instanceof FormalizedPropertyRelationButton))
				{
					Path p = new Path();
					
					Point startp = ((CustomObjectRelation) dc).getStartElementCenterPoint();
					Point centerp = dc.getCenterPoint();
					Point endp = ((CustomObjectRelation) dc).getEndElementCenterPoint();
					
					
					if (dc instanceof PropertyRelation)
					{
						p.moveTo(startp.x, startp.y);
						
						p.lineTo(centerp.x, centerp.y);
						p.lineTo(endp.x, endp.y);
						
						propertyRelationPaint.setColor(drawPath.getColor());
						relationPaint = propertyRelationPaint;
					}
					else if (dc instanceof SubClassRelation)
					{
						p.moveTo(startp.x, startp.y);

						p.lineTo(endp.x, endp.y);
						
						((SubClassRelation) dc).updateArrow(canvas, matrix);
						
						relationPaint = subclassRelationPaint;
					}
					else if (dc instanceof InstatiationRelation)
					{
						p.moveTo(startp.x, startp.y);
						
						p.lineTo(endp.x, endp.y);

						RectF startBounds = new RectF();
						((CustomObjectRelation) dc).getStartElement().getPath().computeBounds(startBounds, true);
						
						startBounds.bottom = (float) (startBounds.bottom+12);
						startBounds.top = (float) (startBounds.top-12);
						startBounds.left = (float) (startBounds.left-10);
						startBounds.right = (float) (startBounds.right+10);
						
						RectF endBounds = new RectF();
						((CustomObjectRelation) dc).getEndElement().getPath().computeBounds(endBounds, true);
						
						endBounds.bottom = (float) (endBounds.bottom+12);
						endBounds.top = (float) (endBounds.top-12);
						endBounds.left = (float) (endBounds.left-10);
						endBounds.right = (float) (endBounds.right+10);

						matrix.mapRect(startBounds);
						matrix.mapRect(endBounds);
						
						filledInstanziiationRelationPaint.setAlpha(dc.getAlpha());
						
						canvas.drawRect(startBounds, filledInstanziiationRelationPaint);
						canvas.drawRect(endBounds, filledInstanziiationRelationPaint);
						
						matrix.mapRect(instanziationStrokeRect);
						instanziiationRelationPaint.setStrokeWidth(instanziationStrokeRect.height());
						
						relationPaint = instanziiationRelationPaint;
					}

					p.transform(matrix);
					
					relationPaint.setAlpha(dc.getAlpha());
					
					canvas.drawPath(p, relationPaint);
					
				}
				
				if (dc instanceof DrawingPropertyRelation)
				{
					Path p = new Path();
					
					Point startp = ((DrawingPropertyRelation) dc).getStartElementCenterPoint();
					Point centerp = dc.getCenterPoint();
					Point endp = ((DrawingPropertyRelation) dc).getEndElementCenterPoint();

					p.moveTo(startp.x, startp.y);
					
					p.lineTo(centerp.x, centerp.y);
					p.lineTo(endp.x, endp.y);
					
					propertyRelationPaint.setColor(drawPath.getColor());
					relationPaint = propertyRelationPaint;

					p.transform(matrix);
					
					relationPaint.setAlpha(dc.getAlpha());
					
					canvas.drawPath(p, relationPaint);

				}

				// apply the inverse matrix of the canvas upon the path

				if (dc instanceof PropertyRelation)
				{
					((PropertyRelation) dc).drawPropertyRelation(canvas, matrix);
				}
				else if (dc instanceof SubClassRelation)
				{
					((SubClassRelation) dc).drawPropertyRelationHelpText(canvas, matrix);
				}
				
				drawPath.transform(matrix);
				
				if( dc instanceof DrawingIndividual || dc instanceof DrawingConcept)
				{
					// fill the background
					filledPaint.setColor(getResources().getColor(R.color.sketchBackground));
					filledPaint.setAlpha(dc.getAlpha());
					canvas.drawPath(drawPath, filledPaint);
				}

				if (dc instanceof SubClassRelation || dc instanceof PropertyRelation)
				{
					// draw the path
					// fill the background	
					filledPaint.setColor(drawPath.getColor());
					filledPaint.setAlpha(dc.getAlpha());
					if(drawPath.isVisible()) canvas.drawPath(drawPath, filledPaint);

				}
				
				if (dc instanceof DrawingPropertyRelation)
				{
					filledPaint.setColor(getResources().getColor(R.color.sketchBackground));
					filledPaint.setAlpha(dc.getAlpha());
					canvas.drawPath(drawPath, filledPaint);
				}

				if (drawPath.isVisible())
				{
					paint.setAlpha(dc.getAlpha());
				}
				else
				{
					paint.setAlpha(0);
				}
				
				if(drawPath.isVisible()) canvas.drawPath(drawPath, paint);

				// restore the initial path object with the original
				// transformation
				// Matrix

				drawPath.transform(inverse);

				if (dc instanceof PropertyRelation)
				{
					((PropertyRelation) dc).updateArrow(canvas, matrix);
				}
				else if (dc instanceof SubClassRelation)
				{
					((SubClassRelation) dc).drawRelationIcon(canvas, matrix);
				}
				else if (dc instanceof DrawingConcept)
				{
					((DrawingConcept) dc).drawDrawingConceptIcon(canvas, matrix);
				}
				else if (dc instanceof DrawingIndividual)
				{
					((DrawingIndividual) dc).drawDrawingIndividualIcon(canvas, matrix);
				}
				else if (dc instanceof DrawingPropertyRelation)
				{
					((DrawingPropertyRelation) dc).updateArrow(canvas, matrix);
				} 
			}
			
			
			if (drawingObjects.ContainsFormalizedConceptObjects()) 
			{
				drawingObjects.drawFormalizedConceptObjects(canvas, matrix, backupTransformationMatrix, false);
			}
			
			if (drawingObjects.ContainsFormalizedIndividualObjects()) 
			{
				drawingObjects.drawFormalizedIndividualObjects(canvas, matrix, backupTransformationMatrix, false);
			}

			paint.setColor(getResources().getColor(R.color.highlight));
			
			if (!tempPath.isEmpty() && mode == DRAW) 
			{
				canvas.drawPath(tempPath, paint);
			}

			// attach new paths
			for (int j = 0; j < newPaths.size(); j++) 
			{
				paint.setColor(getResources().getColor(R.color.activeSketch));
				
				canvas.drawPath(newPaths.get(j), paint);
			}
			
			canvas.restore();

			// DEBUG ///////////////////////
			// draw bounding boxes of paths for debugging purposes
			 
			if (drawDebugRegions)
			{
				RectF rect = new RectF();
				
				if(drawingObjects.getPathList().getPaths() != null)
				{
					for(int i = 0; i < drawingObjects.getPathList().getPaths().size(); i++)
					{
						drawingObjects.getPathList().getPath(i).computeBounds(rect, true);
						
						CustomPath cp = new CustomPath();

						cp.moveTo(rect.left, rect.top);
						cp.lineTo(rect.left, rect.bottom);
						cp.lineTo(rect.right, rect.top);
						cp.lineTo(rect.right, rect.bottom);
						cp.lineTo(rect.left, rect.top);
						
						cp.moveTo(rect.left, rect.top);
						cp.lineTo(rect.right, rect.top);
						cp.moveTo(rect.left, rect.bottom);
						cp.lineTo(rect.right, rect.bottom);

						cp.transform(matrix);
					
						canvas.drawPath(cp, debugPaint); //rot
						//canvas.drawRect(rect, debugPaint2); //gelb
					}
				}
			}
			
			// DEBUG ///////////////////////
			 
		}

		canvas.restore();

		super.onDraw(canvas);
	}

	

	/**
	 * Handler for all tracked touch events upon the <b>DrawView</b>
	 * 
	 * @param v
	 * @param event
	 * @return true if the method was successfully passed
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View,
	 *      android.view.MotionEvent)
	 */
	public boolean onTouch(View v, MotionEvent event) 
	{
		try {
			

			// store historical motion event coordinates
			historicalX = x;
			historicalY = y;

			x = event.getX(0);
			y = event.getY(0);
			
			float timedifference = -1;
			
			switch (event.getAction() & MotionEvent.ACTION_MASK) 
			{
				// invoked as soon as a touch input is registered
				case MotionEvent.ACTION_DOWN:
	
					historicalX = -1;
					historicalY = -1;
	
					savedMatrix.set(matrix);
	
					start.set(x, y);
	
					mode = DRAW;
	
					// test if path objects are in the input area, if the pan mode
					// is
					// enabled and a double tap was performed

					if (historicalEventTime > 0)
					{
						timedifference = event.getEventTime() - historicalEventTime;
						
						//Log.d("DrawView", "onTouch: ACTION_DOWN");
					}
						
					
	
					if (timedifference < 300 && timedifference > -1) 
					{
						//Log.d("DrawView", "onTouch: ACTION_DOWN doubleTap");
						
						doubleTap = true;
						matrix.set(savedMatrix);
						RetrievePossiblePathMatch(event, drawingObjects.getPathList());
						historicalEventTime = -1;
					}

					historicalEventTime = event.getEventTime();

					editText.setVisibility(GONE);
					editText.setText("");
					confirmButton.setVisibility(GONE);
					cancelButton.setVisibility(GONE);
					
					break;
	
				// invoked as soon as a second touch input is recognized
				// simultaneously
				// to the first one
				case MotionEvent.ACTION_POINTER_DOWN:
	
					//Log.d("DrawView", "onTouch: ACTION_POINTER_DOWN");
					doubleTap = false;
	
					oldDist = spacing(event);
	
					if (oldDist > 0f) 
					{
						savedMatrix.set(matrix);
						midPoint(mid, event);
						mode = PANZOOM;
					}
					
					
					editText.setVisibility(GONE);
					editText.setText("");
					confirmButton.setVisibility(GONE);
					cancelButton.setVisibility(GONE);
					
					break;
	
				// invoked as soon as the second touch input disappears
				case MotionEvent.ACTION_POINTER_UP:
	
					//Log.d("DrawView", "onTouch: ACTION_POINTER_UP");
					
					scaleView = false;
					
					
	
					doubleTap = false;
	
					// stay in drag - mode if the flag is set
					if (mode == PANZOOM) 
					{
						savedMatrix.set(matrix);
	
					}
					// switch back to standard drawing mode
					else
					{
						mode = DRAW;
					}
	
					pointsForRecognizer.clear();
					firstPointOfPath = true;
					tempPath = new CustomPath();
	
					calculateCanvasSize = true;
					break;
	
				// invoked in case the touch input is moving
				case MotionEvent.ACTION_MOVE:
					doubleTap = false;

					if (mode == PANZOOM) 
					{
						float newDist = spacing(event);
	
						if (newDist > 0f) 
						{
	
							matrix.set(savedMatrix);

							// apply a threshold that enables a distinction between
							// zoom and pan
							if (!scaleView && Math.abs(newDist - oldDist) > 75) 
							{
								scaleView = true;
								oldDist = newDist;
							}
	
							scale = Math.abs(newDist / oldDist);
	
							if (scaleView) 
							{
								matrix.set(savedMatrix);
								
								// scale canvas according to detected scale
								// operation
								if (allowScale)
								{
									matrix.postScale(scale, scale, mid.x, mid.y);
								}
	
								// translate canvas according to the detected pan
								// operation
								if (allowTranslate) matrix.postTranslate(x - start.x, y - start.y);

							} 
							else 
							{
								matrix.set(savedMatrix);
								if (allowTranslate)matrix.postTranslate(x - start.x, y - start.y);
							}
	
						}
	
						ma.setUnsaved(false);
						
					}
	
					editText.setVisibility(GONE);
					editText.setText("");
					confirmButton.setVisibility(GONE);
					cancelButton.setVisibility(GONE);
					
					break;
	
				case MotionEvent.ACTION_UP:
					
					Log.d("DrawView", "onTouch: ACTION_UP");
					
					timedifference = event.getEventTime() - historicalEventTime;
					
					if (timedifference < 300 && timedifference > -1) 
					{
						DrawingComponent dc = null;
						
						dc = getElementUnderPoint(new Point(x,y));
						
						if (dc != null)
						{
							Log.d("DrawView", "ontRes: " + dc.getOntResource());
							
							if (dc.isOpen())
							{
								dc.setOpen(false);
								
								if (dc instanceof DrawingPropertyRelation)
								{
									Log.d("DrawView", "onTouch: ACTION_UP close DrawingPropertyRelation");
									
									for (FormalizedPropertyRelationButton fb : ((DrawingPropertyRelation)dc).getFormalizedRelationbuttons())
									{
										fb.getPath().setVisible(false);
									}
								}
								else if (dc instanceof FormalizedPropertyRelationButton)
								{
									if (((FormalizedPropertyRelationButton)dc).getPath().isVisible() == true) 
									{
										replaceDrawingPropertyRelation(((FormalizedPropertyRelationButton)dc));
										drawingObjects.removeComponent(((FormalizedPropertyRelationButton)dc));
									}
								}
							}
							else
							{
								dc.setOpen(true);
								
								if (dc instanceof DrawingPropertyRelation)
								{
									Log.d("DrawView", "onTouch: ACTION_UP open DrawingPropertyRelation");
									
									for (FormalizedPropertyRelationButton fb : ((DrawingPropertyRelation)dc).getFormalizedRelationbuttons())
									{
										fb.getPath().setVisible(true);
									}
								}
							}
						}

					}
	
					if (!doubleTap && mode == DRAW) 
					{
						paint.setColor(getResources().getColor(R.color.activeGray));
	
						CustomPath path = new CustomPath();
						boolean first = true;
						
						boolean startsOnElement= false;
						boolean endsOnElement = false;
						
						DrawingComponent startElement = null;
						DrawingComponent endElement = null;
	
						// recognize gestures that consist of at least 10 Points
						if (pointsForRecognizer.size() > MINSTROKELENGTH) 
						{
							Point lastPoint = null;
							
							for (Point point : pointsForRecognizer) 
							{
								if (first) 
								{
									path.moveTo(point.x, point.y);
									
									first = false;
									
									CustomPath tpath = new CustomPath();

									tpath.moveTo(point.x, point.y);
									
									tpath.transform(matrix);
									
									startElement = getRelationTargetElementUnderPoint(point);
		
									if(startElement != null)
									{
										startsOnElement = true;
									}
								}
	
								path.lineTo(point.x, point.y);
								
								lastPoint = point;
							}
							
							endElement = getRelationTargetElementUnderPoint(lastPoint);
							
							if(endElement != null)
							{
								endsOnElement = true;
							}
	
							newPaths.add(path);
	
							CustomPath[] pathArray = new CustomPath[] { path };
							
							if((startsOnElement && endsOnElement) && (startElement != endElement))
							{
								if ((startElement instanceof DrawingConcept && endElement instanceof DrawingConcept) 
										|| (startElement instanceof FormalizedConcept && endElement instanceof DrawingConcept))
								{
									if(!(endElement.hasSuperClass())) addSubConceptRelation(startElement, endElement, false);
									else 
									{
										Toast toast = Toast.makeText(getContext(), "This concept does already have a super concept!", Toast.LENGTH_LONG);
										
										toast.show();
									}
								}
								else if ((startElement instanceof DrawingIndividual || startElement instanceof FormalizedIndividual) 
										&& (endElement  instanceof DrawingIndividual || endElement instanceof FormalizedIndividual))
								{
									//TODO: namespace
									
									Log.d("test", startElement.getItemText() + " nach " + endElement.getItemText());
									
									addPropertyRelation(startElement, endElement, getResources().getColor(R.color.sketch), "http//placeholder.com/", false, false, "");
								}
								else if ((startElement instanceof DrawingIndividual && endElement instanceof DrawingConcept) 
										|| (startElement instanceof DrawingConcept && endElement  instanceof DrawingIndividual)
										|| (startElement instanceof DrawingIndividual && endElement  instanceof FormalizedConcept)
										|| (startElement instanceof FormalizedConcept && endElement  instanceof DrawingIndividual))

								{
									if (startElement instanceof DrawingIndividual)
									{
										if(!(startElement.hasInstanziation())) addInstantiation(endElement, startElement, false);
										else
										{
											Toast toast = Toast.makeText(getContext(), "This individual is already instantiated!", Toast.LENGTH_LONG);
											
											toast.show();
										}
									}
									else
									{
										if(!(endElement.hasInstanziation())) addInstantiation(startElement, endElement, false);
										else
										{
											Toast toast = Toast.makeText(getContext(), "This individual is already instantiated!", Toast.LENGTH_LONG);
											
											toast.show();
										}
									}
								}
								else 
								{
									String startele = "";
									String endele = "";
									
									if (startElement instanceof DrawingIndividual || startElement instanceof FormalizedIndividual) startele = getResources().getString(R.string.tabname_individuals);
									else if (startElement instanceof DrawingConcept || startElement instanceof FormalizedConcept) startele = getResources().getString(R.string.tabname_classes);
									
									if (endElement instanceof DrawingIndividual || endElement instanceof FormalizedIndividual) endele = getResources().getString(R.string.tabname_individuals);
									else if (endElement instanceof DrawingConcept || endElement instanceof FormalizedConcept) endele = getResources().getString(R.string.tabname_classes);
									
									Toast toast = Toast.makeText(getContext(), getResources().getString(R.string.wrong_relation_1) + " " + startele + " and " + endele + " " + getResources().getString(R.string.wrong_relation_2), Toast.LENGTH_LONG);
									toast.show();
								}
								
								pointsForRecognizer.clear();
								newPaths.clear();
								firstPointOfPath = true;
								tempPath.reset();			
							}
							else
							{
								asyncRecognizeGestues(pathArray);
								
								recognizeHandwriting(pointsForRecognizer, path);
							}
						}
	
						tempPath = new CustomPath();
	
						firstPointOfPath = true;
	
						pointsForRecognizer.clear();
	
					}
	
					mode = DEFAULT;
					break;
			}

			if (!doubleTap)
			{
				if (mode == DRAW && !drawingObjects.getPathList().ContainsHighlightedPath())
				{
					// draw path but ensure that the user doesn't zoom
					if (event.getAction() == MotionEvent.ACTION_MOVE && mode != PANZOOM) 
					{
						paint.setColor(getResources().getColor(R.color.highlight));

						Point point = new Point();

						point.x = (int) x;

						point.y = (int) y;

						if (((int) historicalX) != point.x && ((int) historicalY) != point.y) 
						{

							pointsForRecognizer.add(point);

							if (firstPointOfPath) 
							{
								tempPath.moveTo(point.x, point.y);
								firstPointOfPath = false;
							} 
							else 
							{
								tempPath.lineTo(point.x, point.y);
							}
						}
					}
				}

			}

			// delete path if canvas was zoomed
			if (mode == PANZOOM) 
			{
				pointsForRecognizer.clear();
				newPaths.clear();
				firstPointOfPath = true;
			}

		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		invalidate();

		return true;

	}

	public void resetView()
	{
		if (pointsForRecognizer != null) pointsForRecognizer.clear();
		if (newPaths != null) newPaths.clear();
		firstPointOfPath = true;
		if (tempPath != null) tempPath.reset();
		if (tempHandwritingPaths != null) tempHandwritingPaths.clear();
		
		hideSuggestionButtons();
	}
	
	/**
	 * Returns a DrawigComponent for adding a relation of the type DrawingIndividual, DrawingConcept or FormalizedObject if the point is contained by the returned object.
	 * @param point to check if their is any object under
	 * @return DrawigComponent of the type DrawingIndividual, DrawingConcept or FormalizedObject or null
	 */
	private DrawingComponent getRelationTargetElementUnderPoint(Point point) 
	{
		RectF rect = new RectF();
		
		DrawingComponent dc = null;
		
		if(drawingObjects.getPathList().getPaths() != null)
		{
			for(int i = 0; i < drawingObjects.getPathList().getPaths().size(); i++)
			{
				CustomPath cp = new CustomPath();
				
				cp.addPath(drawingObjects.getPathList().getPath(i));
				
				cp.transform(matrix);
				
				cp.computeBounds(rect, true);
				
				dc = (DrawingComponent) (drawingObjects.getObjectByPathId(drawingObjects.getPathList().getPath(i).getUid()));

				if (dc != null)
				{
					if(rect.contains(point.x, point.y) 
							&& (dc.getAlpha() != 0)
							&& (dc instanceof DrawingIndividual || dc instanceof DrawingConcept || dc instanceof FormalizedObject ))
						return dc;
				}

			}
		}

		return null;
	}
	
	/**
	 * Returns a DrawigComponent for simulation a tap on an element of the type DrawingIndividual, DrawingConcept, FormalizedObject, CustomObjectRelation, DrawingPropertyRelation or FormalizedPropertyRelationButton  if the point is contained by the returned object.
	 * @param point to check if their is any object under
	 * @return DrawigComponent of the type DrawingIndividual, DrawingConcept, FormalizedObject, CustomObjectRelation, DrawingPropertyRelation, FormalizedPropertyRelationButton or null
	 */
	private DrawingComponent getElementUnderPoint(Point point) 
	{
		RectF rect = new RectF();
		
		DrawingComponent dc = null;
		
		if(drawingObjects.getPathList().getPaths() != null)
		{
			for(int i = 0; i < drawingObjects.getPathList().getPaths().size(); i++)
			{
				CustomPath cp = new CustomPath();
				
				cp.addPath(drawingObjects.getPathList().getPath(i));
				
				cp.transform(matrix);
				
				cp.computeBounds(rect, true);
				
				dc = (DrawingComponent) (drawingObjects.getObjectByPathId(drawingObjects.getPathList().getPath(i).getUid()));

				if (dc != null)
				{
					if(rect.contains(point.x, point.y) 
							&& (dc.getAlpha() != 0)
							&& (dc instanceof DrawingIndividual || dc instanceof DrawingConcept 
									|| dc instanceof FormalizedObject || dc instanceof CustomObjectRelation 
									|| dc instanceof DrawingPropertyRelation || dc instanceof FormalizedPropertyRelationButton))
						return dc;
				}

			}
		}

		return null;
	}

	/**
	 * Submit the point data to the RecognizerService in order to recognize the
	 * letters that were drawn out of the respective list of points
	 * 
	 * @param pointsForRecognizer2
	 *            The list of points that deliver the basis for the recognition
	 * @param path
	 *            The path object that was created out of the respective list of
	 *            points
	 */
	private void recognizeHandwriting(List<Point> pointsForRecognizer2, CustomPath path) 
	{
		// handwriting recognition

		if (pointsForRecognizer2.size() > MINSTROKELENGTH) 
		{



			tempHandwritingPaths.add(path);

			// notify recognizer thread about data availability
			ma.mBoundService.dataNotify(nStrokeCnt);
		}
	}

	/**
	 * Asynchronously identify selected path objects
	 * 
	 * @param event
	 *            touch input event
	 * @param scaledPathArray2
	 *            ScaledPathArray object that contains all paths that are drawn
	 *            on the canvas
	 */
	private void RetrievePossiblePathMatch(MotionEvent event, ScaledPathArray scaledPathArray2) 
	{
	
//		Log.d("retreiver",""+ scaledPathArray2 + " " + scaledPathArray2.getPaths().size());
		
		if (scaledPathArray2.getPaths().size() > 0)
		{
			PathRetrieverTask retriever = 	new PathRetrieverTask(this);
			
			retriever.execute(scaledPathArray2.getPathsArray());
		}

	}

	boolean firstBackup = true;

	/**
	 * Translate the transformations of highlighted path objects back to the
	 * canvas matrix
	 * 
	 * @param minDistanceIndex
	 *            Index of the detected selected path
	 */
	protected void updatePath(CustomPath path) 
	{
		
		Log.d("DrawView", "::::::::::::::::::::::: updatePath starts :::::::::::::::::::::::");

		if (path != null) 
		{

			DrawingComponent component = null;
			component = drawingObjects.getObjectByPathId(path.getUid());
			
			//Log.d("DrawView", "updatePath " + component);

			if (component != null && (component instanceof FormalizedPropertyRelationButton))
				return;
			
			if(component != null)
			if (!path.isHighlighted()) 
			{
				Log.d("DrawView", "update_NOT_HighlightedPath " + component);

				component.updatePath(matrix, backupTransformationMatrix, true);
				
				if (!(component instanceof InstatiationRelation) 
						&& !(component instanceof SubClassRelation)
						&& !(component instanceof PropertyRelation)
						&& !(component instanceof DrawingPropertyRelation)) 
				{

					Log.d("DrawView", "setPathOnTop " + component);

					ArrayList<CustomPath> acp =  component.setPathOnTop(true);
					
					for (CustomPath cp : acp)
					{
						drawingObjects.getPathList().setOnTop(cp);
						Log.d("DrawView", "setOnTop " + cp);
					}
				}
			} 
			else 
			{
				Log.d("DrawView", "updateHighlightedPath " + component);
	
				// update object dependencies
	
				if (!component.isGrouped())
				{
					if (!(component instanceof DrawingWordLetter)) 
					{
						drawingObjects.removeComponent(component);
	
						component.updatePath(matrix, backupTransformationMatrix, false);
	
						if (component.isComposite) 
						{
							((DrawingComposite) component).deleteChildren();
						}
	
						drawingObjects.addComponent(component);
					}
					else 
					{
						component = ((DrawingWordLetter) component).getParent();
	
						drawingObjects.removeCompositeComponent(component);
	
						Log.d("DrawView","update drawingwordcomposite");
						component.updatePath(matrix, backupTransformationMatrix, false);
	
						drawingObjects.addComponent(component);
					}
	
				} 
				else 
				{
	
					if (component instanceof DrawingWordLetter) 
					{
						component = component.getParent();
					}
	
					component = component.getParent();
	
					drawingObjects.removeCompositeComponent(component);
	
					component.updatePath(matrix, backupTransformationMatrix,false);
	
					drawingObjects.addComponent(component);
	
				}
	
			}

		}

		if (firstBackup)
		{
			if (drawingObjects.getPathList().ContainsHighlightedPath()) 
			{
				Log.d("DrawView", "::backupCurrentTransformationMatrix::");
				
				backupCurrentTransformationMatrix();
				firstBackup = false;
				
				 updateAllowScale(path);
				 
				 ma.activateScrollBarPanel(false);
			}
		}
		else 
		{
			updateAllowScale(path);
		}
		
		if (!firstBackup && !drawingObjects.getPathList().ContainsHighlightedPath()) 
		{
			Log.d("DrawView", "::restoreBackupTransformationMatrix::");
			
			restoreBackupTransformationMatrix();
			firstBackup = true;
			
			allowScale = true;
			allowTranslate = true;
			
			firstCheck = true;
			
			mode = DEFAULT;
			
			ma.activateScrollBarPanel(true);
		}

		Log.d("DrawView", "::::::::::::::::::::::::: end of updatePath :::::::::::::::::::::::::::::::::::");
		
		invalidate();

	}
	
	boolean firstCheck = true;
	
	private void updateAllowScale(CustomPath path)
	{
		boolean scale = true;
		boolean translate = true;
		
		switch(path.getOntoType())
		{
		case DRAWNCONCEPT:
			scale = true;
			translate = true;
			break;
		case DRAWNINDIVIDUAL:
			scale = true;
			translate = true;
			break;
		case DRAWNINSTANTIATION:
			scale = false;
			translate = false;
			break;
		case DRAWNPROPERTYRELATION:
			scale = true;
			translate = true;
			break;
		case DRAWNSUBCONCEPTRELATION:
			scale = true;
			translate = false;
			break;
		case FORMALIZEDCONCEPT:
			scale = true;
			translate = true;
			break;
		case FORMALIZEDINDIVIDUAL:
			scale = true;
			translate = true;
			break;
		case FORMALIZEDINSTANTIATION:
			scale = false;
			translate = false;
			break;
		case FORMALIZEDPROPERTYRELATION:
			scale = true;
			translate = true;
			break;
		case FORMALIZEDSUBCONCEPTRELATION:
			scale = false;
			translate = false;
			break;
		case NONE:
			scale = true;
			translate = true;
			break;
		default:
			break;
		}
		
		if (firstCheck)
		{
			allowScale = scale;
			allowTranslate = translate;
		}
		else
		{
			if (allowScale)
			{
				if (!scale) allowScale = false;
			} 
			
			if (allowTranslate)
			{
				if (!translate) allowTranslate = false;
			}
		}

	}
	
	
	/**
	 * Adds a formalized object (leaf) to the sketchBoard at passed point with the OntResource
	 * @param leaf
	 * @param point
	 * @param ontresource
	 */
	public void addFormalizedObject(FormalizedObject leaf, android.graphics.Point point, OntResource ontresource)
	{
		int width = 100;
		int height = 100;
		
		leaf.setParent(drawingObjects);
		
		if (leaf instanceof FormalizedConcept)
		{
			width = getResources().getIntArray(R.array.formalizedConceptDimension)[0];
			height = getResources().getIntArray(R.array.formalizedConceptDimension)[1];
			
			leaf.setNormalColor(getResources().getColor(R.color.concept));
			leaf.setHighlightColor(getResources().getColor(R.color.highlight));
			
			leaf.setDisplayState(DisplayObjectState.CONCEPT);
			leaf.getPath().setOntoType(OntologyObjectTypes.FORMALIZEDCONCEPT);
			
			leaf.setUri(ontresource.getURI());

			OntClass oClass = ontresource.asClass();
			
			OntClass sClass = oClass.getSuperClass();
			
			String helpText = "";
			
			if (sClass != null) helpText = getResources().getString(R.string.relation_subconcept) + " " + sClass.getLocalName();

			leaf.setHelpText(helpText);
			leaf.setItemText(leaf.getName());
			leaf.setOntResource(oClass);
		} 
		else if (leaf instanceof FormalizedIndividual)
		{
			width = getResources().getIntArray(R.array.formalizedIndividualDimension)[0];
			height = getResources().getIntArray(R.array.formalizedIndividualDimension)[1];
			
			leaf.setNormalColor(getResources().getColor(R.color.individual));
			leaf.setHighlightColor(getResources().getColor(R.color.highlight));
			
			leaf.setDisplayState(DisplayObjectState.INDIVIDUAL);
			leaf.getPath().setOntoType(OntologyObjectTypes.FORMALIZEDINDIVIDUAL);
			
			leaf.setUri(ontresource.getURI());

			Individual individual = ontresource.asIndividual();

			OntClass sClass = individual.getOntClass();
			
			String helpText = "";
			
			if (sClass != null) helpText = getResources().getString(R.string.relation_instanziation) + " " + sClass.getLocalName();

			leaf.setHelpText(helpText);
			leaf.setItemText(leaf.getName());
			leaf.setOntResource(individual);

		}
		
		leaf.setPosition(point.x, point.y, width, height);
		leaf.getPath().setVisible(false);
		leaf.setBackgroundColor(getResources().getColor(R.color.formalizedBackground));
		leaf.setButtonColorActive(getResources().getColor(R.color.sketchBackground));
		
		this.drawingObjects.addComponent(leaf);
		
		if (leaf instanceof FormalizedConcept)
		{
			OntClass oClass = ontresource.asClass();
			
			if (oClass.hasSubClass())
			{
				for (OntClass oc : oClass.listSubClasses(true).toList())
				{
					if (this.containsOntResource(oc.getURI()))
					{
						Matrix examinedInverseTransformationMatrix = new Matrix();
						
						matrix.invert(examinedInverseTransformationMatrix);
						
						CustomPath cp = new CustomPath();
						
						Point p = getCenterPoint(leaf.getCenterPoint(), this.getDrawingComponent(oc.getURI()).getCenterPoint());

						SubClassRelation relation = null;

						cp.moveTo(p.x, p.y);
						cp.addCircle(p.x, p.y, (float) getResources().getIntArray(R.array.sizes)[0] , Path.Direction.CW);
						
						cp.setColor(getResources().getColor(R.color.concept));
						cp.setOntoType(OntologyObjectTypes.FORMALIZEDSUBCONCEPTRELATION);
						
						DrawingComponent endComponent = this.getDrawingComponent(oc.getURI());
						
						relation = new SubClassRelation(cp, examinedInverseTransformationMatrix, leaf, this.getDrawingComponent(oc.getURI()));
						
						relation.setBackgroundColor(getResources().getColor(R.color.sketchBackground));
						relation.setHelpText(endComponent.getItemText() + " " + getResources().getString(R.string.relation_subconcept) + " " + leaf.getItemText());
						relation.setDisplayState(DisplayObjectState.CONCEPTSUBCLASSRELATION);
						
						drawingObjects.addComponent(relation);

					}
				}
			}

			if (oClass.hasSuperClass())
			{
				for (OntClass oc : oClass.listSuperClasses(true).toList())
				{
					if (this.containsOntResource(oc.getURI()))
					{
						Matrix examinedInverseTransformationMatrix = new Matrix();
						
						matrix.invert(examinedInverseTransformationMatrix);
						
						CustomPath cp = new CustomPath();
						
						Point p = getCenterPoint(this.getDrawingComponent(oc.getURI()).getCenterPoint(), leaf.getCenterPoint());

						SubClassRelation relation = null;
	
						cp.moveTo(p.x, p.y);
						cp.addCircle(p.x, p.y, (float) getResources().getIntArray(R.array.sizes)[0] , Path.Direction.CW);
						
						cp.setColor(getResources().getColor(R.color.concept));
						cp.setOntoType(OntologyObjectTypes.FORMALIZEDSUBCONCEPTRELATION);
						
						DrawingComponent startComponent = this.getDrawingComponent(oc.getURI());
						
						relation = new SubClassRelation(cp, examinedInverseTransformationMatrix, startComponent, leaf);
						
						relation.setBackgroundColor(getResources().getColor(R.color.sketchBackground));
						relation.setHelpText(leaf.getItemText() + " " + getResources().getString(R.string.relation_subconcept) + " " + startComponent.getItemText());
						relation.setDisplayState(DisplayObjectState.CONCEPTSUBCLASSRELATION);
						
						this.drawingObjects.addComponent(relation);
					}
				}
			}
			
			for (Individual i : ma.getIndividuals())
			{
				if (i.hasOntClass(oClass, true))
				{
					if (containsOntResource(i.getURI()))
					{
						//add Relation to Individual
						this.addInstantiation(leaf, this.getDrawingComponent(i.getURI()), true);
						
						ma.scrollBar.setProgress(50);
					}
				}
			}

			for (OntProperty op : MainActivity.getProperties())
			{
				OntClass range = null;
				if (op.getRange() != null) range = op.getRange().asClass();
				
				OntClass domain = null;
				if (op.getDomain() != null) domain= op.getDomain().asClass();
				
				if (range != null && domain != null)
				{
					if (oClass.equals(domain) || oClass.equals(range))
					{
						
						DrawingComponent drawingDomain = getDrawingComponent(domain.getURI());
						DrawingComponent drawingRage = getDrawingComponent(range.getURI());
						
						if (drawingDomain != null && drawingRage != null)
						{
							ma.setOntopanelPropertyListItemActive(op.getURI(), true);

							this.addPropertyRelation(drawingDomain, drawingRage, ma.getPropertyRelationColor(op.getURI()), op.getURI(), true, true, op.getLocalName());
						}
					}
				}	
			}
		}
		else if (leaf instanceof FormalizedIndividual)
		{
			Individual individual = ontresource.asIndividual();

			if (this.containsOntResource(individual.getOntClass().getURI()))
			{
				//add Relation to Concept
				addInstantiation(this.getDrawingComponent(individual.getOntClass().getURI()), leaf, true);
				
				ma.scrollBar.setProgress(50);
			}
			
			StmtIterator si = individual.listProperties();
			
			while(si.hasNext())
			{
				Statement s = si.next();
				
				Triple t = s.asTriple();

				if (individual.hasProperty(ma.getProperty(t.getPredicate().getURI())))
				{
					try
					{
						DrawingComponent endComponent = getDrawingComponent(t.getObject().getURI());
						
						if (endComponent != null)
						{
							String uri = t.getPredicate().getURI();
							
							int color = ma.getPropertyRelationColor(uri);
							
							if (color != 0) ma.setOntopanelPropertyListItemActive(uri, true);

							if (ma.isPropertyListItemActive(uri))
							{
								addPropertyRelation(leaf, endComponent, color, uri, false, true, t.getPredicate().getLocalName());
							}
						}
					}
					catch (Exception e)
					{
						System.out.print(e.toString());
					}
				}
			}
			
			ma.findObjectsPropertyRelations(leaf.getUri(), leaf);
		}
	}
	
	/**
	 * Adds a visual instantiation between two DrawingComponents
	 * @param startComponent
	 * @param endComponent
	 * @param formalized false if sketched, true if formalized
	 */
	private void addInstantiation(DrawingComponent startComponent, DrawingComponent endComponent, boolean formalized)
	{
		Matrix examinedInverseTransformationMatrix = new Matrix();
		
		matrix.invert(examinedInverseTransformationMatrix);
		
		CustomPath cp = new CustomPath();

		Point startp = startComponent.getCenterPoint();
		Point endp = endComponent.getCenterPoint();
		
		float[] anchor_old_start = new float[] { startp.x, startp.y};
		float[] anchor_old_end = new float[] { endp.x, endp.y };
		
		float radius = (float) (float) getResources().getIntArray(R.array.sizes)[2];
		
		RectF r = new RectF(0, 0, radius, radius);
		
		matrix.mapRect(r);
		
		radius = r.height();
		
		matrix.mapPoints(anchor_old_start);
		matrix.mapPoints(anchor_old_end);
		
		Point p = getCenterPoint(new Point(anchor_old_start[0],anchor_old_start[1]), new Point(anchor_old_end[0], anchor_old_end[1]));

		cp.moveTo(p.x, p.y);
		cp.addCircle(p.x, p.y, radius, Path.Direction.CW);
		
		cp.setColor(getResources().getColor(R.color.formalizedBackground));
		
		cp.transform(examinedInverseTransformationMatrix);
		
		if (formalized) cp.setOntoType(OntologyObjectTypes.FORMALIZEDINSTANTIATION);
		else cp.setOntoType(OntologyObjectTypes.DRAWNINSTANTIATION);
		
		InstatiationRelation relation = new InstatiationRelation(cp, examinedInverseTransformationMatrix, startComponent, endComponent);
		relation.setParent(drawingObjects);
		relation.setDisplayState(DisplayObjectState.INSTANZIATION);
		
		drawingObjects.addComponent(relation);
		
		invalidate();
		
		if(!formalized)
		{
			Individual individual = null;
			OntClass oClass = null;

			if (startComponent instanceof FormalizedConcept || startComponent instanceof DrawingConcept)
			{
				if (startComponent instanceof FormalizedConcept)
				{
					oClass = ma.getOntoModel().getOntClass(startComponent.getUri());
				}
				else
				{
					oClass = ma.getOntoModel().getOntClass(getResources().getString(R.string.onto_namespace) + startComponent.getPath().getUid());
				}

				individual = ((DrawingIndividual) endComponent).getOntResource().asIndividual();
				individual.setOntClass(oClass);
				((DrawingIndividual) endComponent).setOntResource(individual);
				
				if (!startComponent.getItemText().equalsIgnoreCase("")) endComponent.setHelpText(getResources().getString(R.string.relation_instanziation) + " " + startComponent.getItemText());
				else endComponent.setHelpText(getResources().getString(R.string.relation_instanziation) + " EMPTY CONCEPT");
		}
		else
		{
			if (endComponent instanceof FormalizedConcept)
			{
				oClass = ma.getOntoModel().getOntClass(endComponent.getUri());
			}
			else
			{
				oClass = ma.getOntoModel().getOntClass(getResources().getString(R.string.onto_namespace) + endComponent.getPath().getUid());
			}
		
			individual = ((DrawingIndividual) startComponent).getOntResource().asIndividual();
			individual.setOntClass(oClass);
			
			((DrawingIndividual) startComponent).setOntResource(individual);
			
			if (!endComponent.getItemText().equalsIgnoreCase("")) 
			{
				startComponent.setHelpText(getResources().getString(R.string.relation_instanziation) + " " + endComponent.getItemText());
			}
			else 
				startComponent.setHelpText(getResources().getString(R.string.relation_instanziation) + " EMPTY CONCEPT");
		}
		
			ma.scrollBar.setProgress(50);
			
			Toast toast = Toast.makeText(ma, "The real element was successfully instantiated.", Toast.LENGTH_LONG);
			
			toast.show();
		}

	}
	
	/**
	 * Adds a visual addSubConceptRelation between two DrawingComponents
	 * @param startComponent
	 * @param endComponent
	 * @param formalized false if sketched, true if formalized
	 */
	public void addSubConceptRelation(DrawingComponent startComponent, DrawingComponent endComponent, boolean formalized)
	{
		Matrix examinedInverseTransformationMatrix = new Matrix();
		
		matrix.invert(examinedInverseTransformationMatrix);
		
		CustomPath cp = new CustomPath();

		Point startp = startComponent.getCenterPoint();
		Point endp = endComponent.getCenterPoint();
		
		float[] anchor_old_start = new float[] { startp.x, startp.y};
		float[] anchor_old_end = new float[] { endp.x, endp.y };
		
		matrix.mapPoints(anchor_old_start);
		matrix.mapPoints(anchor_old_end);
		
		Point p = getCenterPoint(new Point(anchor_old_start[0],anchor_old_start[1]), new Point(anchor_old_end[0], anchor_old_end[1]));

		float radius = (float) getResources().getIntArray(R.array.sizes)[0];
		RectF r = new RectF(0, 0, radius, radius);
		matrix.mapRect(r);
		radius = r.height();
		
		cp.moveTo(p.x, p.y);
		cp.addCircle(p.x, p.y, radius, Path.Direction.CW);
		
		cp.setColor(getResources().getColor(R.color.concept));
		
		cp.transform(examinedInverseTransformationMatrix);
		
		if (formalized) cp.setOntoType(OntologyObjectTypes.FORMALIZEDSUBCONCEPTRELATION);
		else  cp.setOntoType(OntologyObjectTypes.DRAWNSUBCONCEPTRELATION);
		
		SubClassRelation relation = new SubClassRelation(cp, examinedInverseTransformationMatrix, startComponent, endComponent);
		
		relation.setParent(drawingObjects);
		relation.setDisplayState(DisplayObjectState.CONCEPTSUBCLASSRELATION);
		relation.setBackgroundColor(getResources().getColor(R.color.sketchBackground));
		
		relation.updateHelpText();
		
		drawingObjects.addComponent(relation);
		
		invalidate();
		
		if (ma.scrollBar.getProgress() > 50) ma.scrollBar.setProgress(50);
		
		if(!formalized)
		{
			OntClass startClass = null;
			OntClass endClass = null;
			
			if (startComponent instanceof FormalizedConcept && endComponent instanceof DrawingConcept)
			{
					startClass = ma.getOntoModel().getOntClass(startComponent.getUri());

					endClass = endComponent.getOntResource().asClass();
			}
			else
			{
				startClass = startComponent.getOntResource().asClass();
			
				endClass = endComponent.getOntResource().asClass();
			}
			
			Log.d("DrawView", "addSubClasses: " + startClass + "  " + endClass);
			
			try
			{
				endClass.addSuperClass(startClass);
			}
			catch(Exception e){}

			Toast toast = Toast.makeText(ma, "The sub concept relation was successfully added.", Toast.LENGTH_LONG);
			
			toast.show();
		}
	}
	
	/**
	 * Replaces a DrawingPropertyRelation by aFormalizedPropertyRelationButton 
	 * @param fbutton
	 */
	private void replaceDrawingPropertyRelation(FormalizedPropertyRelationButton fbutton) 
	{
		DrawingPropertyRelation parent = fbutton.getParentRelation();

		DrawingComponent startComponent = parent.getStartElement();
		DrawingComponent endComponent = parent.getEndElement();

		if (startComponent.containsRelation(fbutton.getUri(), endComponent))
		{
			parent.removeRelationButton(fbutton);
			drawingObjects.children.remove(fbutton);
			return;
		}
		
		ArrayList<FormalizedPropertyRelationButton> toDelete = new ArrayList<FormalizedPropertyRelationButton>();
		
		toDelete = parent.getFormalizedRelationbuttons();
		
		Matrix examinedInverseTransformationMatrix = new Matrix();
		matrix.invert(examinedInverseTransformationMatrix);
		
		CustomPath cp = new CustomPath();
		
		Point p = new Point();
		
		RectF bounds = new RectF();
		
		parent.getPath().computeBounds(bounds, true);
		
		float[] anchor_old_start = new float[] {(float) (bounds.left + 0.5*bounds.height()),(float) (bounds.top + 0.5*bounds.height())};
		
		matrix.mapPoints(anchor_old_start);
		
		p.x = (float) anchor_old_start[0];
		p.y = (float) anchor_old_start[1];

		float radius = bounds.height()/2;
		
		cp.moveTo(p.x, p.y);
		cp.addCircle(p.x, p.y, radius, Path.Direction.CW);
		
		cp.setColor(fbutton.getPath().color);
		
		cp.transform(examinedInverseTransformationMatrix);
		
		cp.setOntoType(OntologyObjectTypes.FORMALIZEDPROPERTYRELATION);
		
		FormalizedPropertyRelation relation = new FormalizedPropertyRelation(cp, examinedInverseTransformationMatrix, startComponent, endComponent);
		
		relation.setHelpText(fbutton.getItemText());
		relation.setBackgroundColor((getResources().getColor(R.color.sketchBackground)));
		relation.setUri(fbutton.getUri());
		relation.setParent(drawingObjects);
		relation.setDisplayState(DisplayObjectState.INDIVIDUALRELATION);
		
		relation.setOntResource(fbutton.getOntResource());
		
		int index = drawingObjects.getLastRelationIndex();
		
		drawingObjects.children.add(index,relation);
		
		parent.getPath().reset();
		
		parent.removeReferences();
		
		for (FormalizedPropertyRelationButton fb : toDelete)
		{
			if(drawingObjects.children.contains(fb)) drawingObjects.children.remove(fb);
		}

		if(drawingObjects.children.contains(fbutton.getParentRelation())) drawingObjects.children.remove(fbutton.getParentRelation());

		parent.removeRelationButtons();
		
		parent = null;

		invalidate();
	}


	/**
	 * Adds a visual PropertyRelation between two DrawingComponents
	 * @param startComponent
	 * @param endComponent
	 * @param color
	 * @param uri
	 * @param conceptRelation false if individualRelation, true if conceptRelation
	 * @param formalized false if sketched, true if formalized
	 * @param localName
	 */
	public void addPropertyRelation(DrawingComponent startComponent, DrawingComponent endComponent, int color, String uri, boolean conceptRelation, boolean formalized, String localName)
	{
		Matrix examinedInverseTransformationMatrix = new Matrix();
		
		matrix.invert(examinedInverseTransformationMatrix);
		
		CustomPath cp = new CustomPath();

		Point startp = startComponent.getCenterPoint();
		Point endp = endComponent.getCenterPoint();

		float[] anchor_old_start = new float[] { startp.x, startp.y};
		float[] anchor_old_end = new float[] { endp.x, endp.y };
		
		matrix.mapPoints(anchor_old_start);
		matrix.mapPoints(anchor_old_end);
		
		float radius = (float) getResources().getIntArray(R.array.sizes)[1];
		RectF r = new RectF(0, 0, radius, radius);
		matrix.mapRect(r);
		radius = r.height();
		
		Point p = new Point();
		
		if (startComponent == endComponent)
		{
			Point centerp = startComponent.getCenterPoint();
			
			centerp.y = (float) (centerp.y - 2*startComponent.getBounds().height());
			
			if (startComponent.getSelfRelationCount() >= 0 )
			{
				int count = startComponent.getSelfRelationCount();
				
				if (count >= 0)
				{
					int padding = (int) (((double)count/2.0) + 0.5d);

					if (count%2 == 0) centerp.x = (float) (centerp.x + 2.5*radius * -padding);
					else centerp.x = (float) (centerp.x + 2.5*radius * padding);
				}
			}
			
			float[] anchor_center = new float[] { centerp.x, centerp.y };
			
			matrix.mapPoints(anchor_center);
			
			p = new Point(anchor_center[0] ,anchor_center[1]);
		}
		else
		{
			int count = startComponent.getPairRelationCount(endComponent);
			
			p = getCenterPoint(new Point(anchor_old_start[0],anchor_old_start[1]), new Point(anchor_old_end[0], anchor_old_end[1]));

			if (count >= 0)
			{
				int padding = (int) (((double)count/2.0) + 0.5d);

				if (count%2 == 0) p.x = (float) (p.x + 2.5*radius * -padding);
				else p.x = (float) (p.x + 2.5*radius * padding);
			}
		}

		DrawingComponent relation = null;
		
		if (formalized)
		{
			cp.moveTo(p.x, p.y);
			cp.addCircle(p.x, p.y, radius, Path.Direction.CW);
			
			cp.setColor(color);
			
			cp.transform(examinedInverseTransformationMatrix);
			
			cp.setOntoType(OntologyObjectTypes.FORMALIZEDPROPERTYRELATION);
			
			relation = new FormalizedPropertyRelation(cp, examinedInverseTransformationMatrix, startComponent, endComponent);
			
			relation.setHelpText(localName);
			((FormalizedPropertyRelation) relation).setBackgroundColor((getResources().getColor(R.color.sketchBackground)));
		}
		else 
		{
			Point pathPoint = new Point(p.x, p.y-radius); 
			
			cp.moveTo(p.x, p.y);
			cp.addCircle(p.x, p.y, radius, Path.Direction.CW);
			
			cp.moveTo((float)(pathPoint.x-(0.2*radius)), pathPoint.y);
			
			cp.lineTo(pathPoint.x+1*radius, pathPoint.y);
			cp.lineTo(pathPoint.x+2*radius, pathPoint.y);
			cp.lineTo(pathPoint.x+3*radius, pathPoint.y);
			cp.lineTo(pathPoint.x+4*radius, pathPoint.y);
			cp.lineTo(pathPoint.x+5*radius, pathPoint.y);
			cp.lineTo(pathPoint.x+6*radius, pathPoint.y);
			cp.lineTo(pathPoint.x+7*radius, pathPoint.y);
			
			cp.lineTo(pathPoint.x+7*radius, pathPoint.y+2*radius);
			
			cp.lineTo(pathPoint.x+6*radius, pathPoint.y+2*radius);
			cp.lineTo(pathPoint.x+5*radius, pathPoint.y+2*radius);
			cp.lineTo(pathPoint.x+4*radius, pathPoint.y+2*radius);
			cp.lineTo(pathPoint.x+3*radius, pathPoint.y+2*radius);
			cp.lineTo(pathPoint.x+2*radius, pathPoint.y+2*radius);
			cp.lineTo(pathPoint.x+1*radius, pathPoint.y+2*radius);
			cp.lineTo((float)(pathPoint.x-(0.2*radius)), pathPoint.y+2*radius);

			cp.close();
			
			cp.setColor(color);
			
			cp.transform(examinedInverseTransformationMatrix);
			
			cp.setOntoType(OntologyObjectTypes.DRAWNPROPERTYRELATION);
			
			relation = new DrawingPropertyRelation(cp, examinedInverseTransformationMatrix, startComponent, endComponent);
			
			((DrawingPropertyRelation) relation).setBackgroundColor((getResources().getColor(R.color.sketchBackground)));
			
			((DrawingPropertyRelation) relation).updateFormalizedRelations();
		}
		
		relation.setUri(uri);
		relation.setParent(drawingObjects);
		relation.setHelpText(localName);
		
		if (!formalized) 
		{
			ObjectProperty op = ma.getOntoModel().createObjectProperty(getResources().getString(R.string.onto_namespace) + relation.getPath().getUid());

			relation.setOntResource(op);
			
			relation.setUri(op.getURI());

		}
		
		if (conceptRelation) relation.setDisplayState(DisplayObjectState.CONCEPTRELATION);
		else  relation.setDisplayState(DisplayObjectState.INDIVIDUALRELATION);
		
		if (startComponent.containsRelation(relation) <= 1)
		{
			drawingObjects.addComponent(relation);		
		}
		else 
		{
			((CustomObjectRelation)relation).removeReferences();
			relation = null;
		}

		if (!formalized && ((DrawingPropertyRelation)relation).hasFormalizedRelations())
		{
			int count = 1;
			
			for (OntProperty op : ((DrawingPropertyRelation)relation).getOntProperties())
			{
				
				if (!(startComponent.containsRelation(op.getURI(), endComponent)))
				{
					CustomPath cp2 = new CustomPath();

					Point p2 = new Point(p.x, p.y);
					
					p2.y = (float) (p.y + ((radius*2) * count));
					
					cp2.moveTo(p2.x, p2.y);
					cp2.addCircle(p2.x, p2.y, radius, Path.Direction.CW);
					
					cp2.setColor(ma.getPropertyRelationColor(op.getURI()));
					
					cp2.transform(examinedInverseTransformationMatrix);
					
					cp2.setOntoType(OntologyObjectTypes.FORMALIZEDPROPERTYRELATION);
					
					FormalizedPropertyRelationButton fprb = new FormalizedPropertyRelationButton(cp2, examinedInverseTransformationMatrix, relation, relation);
					
					fprb.setUri(op.getURI());
					fprb.setParent(drawingObjects);
					fprb.setItemText(op.getLocalName());
					fprb.setHelpText(op.getLocalName());
					fprb.setDisplayState(DisplayObjectState.INDIVIDUALRELATION);
					fprb.setBackgroundColor((getResources().getColor(R.color.sketchBackground)));
					fprb.setParentRelation((DrawingPropertyRelation)relation);
					fprb.setOntResource(MainActivity.getOntologyResource(op.getURI()));
					
					((DrawingPropertyRelation)relation).addRelationButton(fprb);
					
					drawingObjects.children.add(fprb);
					
					count++;
				}
			}
		}
		
		invalidate();
		
		if (conceptRelation)
		{
			ma.scrollBar.setProgress(0);
		}
		else 
		{
			ma.scrollBar.setProgress(100);
		}
	}

	/**
	 * Returns a DrawingComponent by a given uri
	 * @param uri
	 * @return DrawingComponent or null
	 */
	public DrawingComponent getDrawingComponent(String uri)
	{
		for (DrawingComponent dc : drawingObjects.children)
		{
			if (dc.getUri().equalsIgnoreCase(uri)) return dc;
		}
		
		return null;
	}
	
	
	/**
	 * Returns the center of two given Points
	 * @param p1
	 * @param p2
	 * @return
	 */
	public Point getCenterPoint(Point p1, Point p2)
	{
		RectF r = new RectF(p1.x, p1.y, p2.x, p2.y);

		Point point = new Point(r.centerX(), r.centerY());

		return point;
	}

	
	/**
	 * Checks if a DrawingObject with the given uri exists
	 * @param uri
	 * @return
	 */
	private boolean containsOntResource(String uri) 
	{

		for (DrawingComponent dc : drawingObjects.children)
		{
			if (dc.getUri().equalsIgnoreCase(uri)) return true;
		}

		return false;
	}

	/**
	 * Asynchronous gesture recognizer
	 * 
	 * @author Christian Brändel TU Dresden / SAP Research Dresden
	 * @see android.os.AsyncTask
	 * 
	 */
	// private class RecognizerTask extends AsyncTask<Point, Integer, Result> {
	private class RecognizerTask extends AsyncTask<CustomPath, Integer, Result> 
	{
		boolean asyncGestureRecognition = gestureRecognitionEnabled;

		CustomPath examinedPath = new CustomPath();
		Matrix examinedInverseTransformationMatrix = new Matrix();
		
		int topicSizeThreshold = 80;//120;

		protected void onPreExecute() 
		{
			matrix.invert(examinedInverseTransformationMatrix);
		}

		protected Result doInBackground(CustomPath... paths) 
		{
			examinedPath = paths[0];

			List<Point> params = examinedPath.getVertices();

			//TODO: Neukonfigurieren der Grenzen mit anschließenden Tests
			
			// Decide whether to reset word recognition and enable gesture
			// recognition

			if (examinedPath.getVertices().size() > 25)
			{
				if (tempHandwritingPaths.size() > 1)
				{

					CustomPath referencePath = tempHandwritingPaths.get(0);

					float[] extrema = new float[] { referencePath.minX,
							referencePath.minY, referencePath.maxX,
							referencePath.maxY };

					matrix.mapPoints(extrema);

					float tolerance = (extrema[3] - extrema[1]) * 1.5f;
					
					if (tolerance > 80) 
					{
						tolerance = 100;
					}

					if ((examinedPath.minY < extrema[1] - tolerance || (examinedPath.maxY > extrema[3]+ tolerance))
							&&
							// examined Path exceeds upper or lower tolerance
							// border
							// of previously drawn elements
							!((examinedPath.minY >= extrema[1]) && (examinedPath.maxY <= extrema[3])))
					// examined path is not located between the tolerance
					// borders
					{
						asyncGestureRecognition = true;
					} 
					else 
					{
						asyncGestureRecognition = false;
					}

				} 
				else 
				{
					asyncGestureRecognition = true;
				}
			} 
			else 
			{
				asyncGestureRecognition = false;
			}

			// only initiate recognition if no word is written at this very
			// moment
			if (params.size() > MINRECOGNITIONLENGTH && asyncGestureRecognition) 
			{
				return recognizer.Recognize(params);
			} 
			else
			{
				return new Result("- none - ", 0.0f, 1.0f, GestureTypes.NOGESTURE);
			}
				
		}

		protected void onPostExecute(Result result) 
		{
			DrawingComponent object = null;
			
			int x,y = 0;

			switch (result.getType()) 
			{
				case NOGESTURE:
	
					examinedPath.transform(examinedInverseTransformationMatrix);
					examinedPath.setColor(getResources().getColor(R.color.activeSketch));
					examinedPath.setType(GestureTypes.NOGESTURE);
					object = new DrawingLeaf(examinedPath,
							examinedInverseTransformationMatrix);
	
					break;

				case INDIVIDUAL:
	
					// NOTE: stroke can't be excluded from handwriting recognition,
					// because it could be
					// that one of the letters "o", "O" or "0" was written -> has to
					// be investigated
					// within the respective context
					// for this reason a minimum size for Topics is introduced
					
					x = 0;
					y = 0;
					
					x= (int) (examinedPath.maxX - examinedPath.minX);
					y = (int) (examinedPath.maxY - examinedPath.minY);
	
					if((x)>topicSizeThreshold && (y)>topicSizeThreshold)
					{
						Log.d("reco", "INDIVIDUAL");
						
						examinedPath.setColor(getResources().getColor(R.color.individual));
					
						examinedPath.transform(examinedInverseTransformationMatrix);
						examinedPath.setType(GestureTypes.INDIVIDUAL);
						examinedPath.setOntoType(OntologyObjectTypes.DRAWNINDIVIDUAL);
						object = new DrawingIndividual(examinedPath, examinedInverseTransformationMatrix);
						object.setDisplayState(DisplayObjectState.INDIVIDUAL);
						object.getPath().setType(GestureTypes.INDIVIDUAL);
						object.getPath().close();
						
						OntClass i = ma.getOntoModel().getOntClass(getResources().getString(R.string.onto_namespace) + "PlainIndividual");
						
						String uri = getResources().getString(R.string.onto_namespace)+object.getPath().getUid();
						
						Individual individual = ma.getOntoModel().createIndividual(uri, i);
						
						((DrawingIndividual)object).setOntResource(individual);
						
						object.setUri(uri);
						
						hideSuggestionButtons();
						
						ma.updateScrollBar(GestureTypes.INDIVIDUAL);
					
					}
					else
					{
						Log.d("reco", "INDIVIDUAL - NOGESTURE");
						
						examinedPath.transform(examinedInverseTransformationMatrix);
						examinedPath.setType(GestureTypes.NOGESTURE);
						object = new DrawingLeaf(examinedPath,
								examinedInverseTransformationMatrix);
						result.setName("- none - ");
						result.setType(GestureTypes.NOGESTURE);
					}

					break;
					
				case CONCEPT:

					// NOTE: stroke can't be excluded from handwriting recognition,
					// because it could be
					// that one of the letters "o", "O" or "0" was written -> has to
					// be investigated
					// within the respective context
					// for this reason a minimum size for Topics is introduced
					
					x = 0;
					y = 0;
					
					x= (int) (examinedPath.maxX - examinedPath.minX);
					y = (int) (examinedPath.maxY - examinedPath.minY);
	
					if((x)>topicSizeThreshold && (y)>topicSizeThreshold)
					{
						Log.d("reco", "CONCEPT");
						
						examinedPath.setColor(getResources().getColor(R.color.concept));
					
						examinedPath.transform(examinedInverseTransformationMatrix);
						examinedPath.setType(GestureTypes.CONCEPT);
						examinedPath.setOntoType(OntologyObjectTypes.DRAWNCONCEPT);
						object = new DrawingConcept(examinedPath, examinedInverseTransformationMatrix);
						object.setDisplayState(DisplayObjectState.CONCEPT);
						//object.getPath().close();
						
						object.getPath().setType(GestureTypes.CONCEPT);
						
						String uri = getResources().getString(R.string.onto_namespace)+object.getPath().getUid();
						
						OntClass oClass = ma.getOntoModel().createClass(uri);
						
						object.setUri(uri);
						
						((DrawingConcept)object).setOntResource(oClass);
						
						hideSuggestionButtons();
						
						ma.updateScrollBar(GestureTypes.CONCEPT);
					
					}
					else
					{
						Log.d("reco", "CONCEPT - NOGESTURE");
						
						examinedPath.transform(examinedInverseTransformationMatrix);
						examinedPath.setType(GestureTypes.NOGESTURE);
						object = new DrawingLeaf(examinedPath, examinedInverseTransformationMatrix);
						result.setName("- none - ");
						result.setType(GestureTypes.NOGESTURE);
					}
	
					break;
				}

			if (object != null)
				drawingObjects.addComponent(object);

			newPaths.remove(examinedPath);

			// clean View if the gesture Recognition is enabled, because word
			// suggestions would still be displayed
			if ((asyncGestureRecognition && result.getType() != GestureTypes.NOGESTURE)) 
			{
				cleanView();
			} 
			else 
			{
				// manage undo button
				if (tempHandwritingPaths.size() > 0) 
				{
					ma.setUndoButtonActive(true);
				} 
				else 
				{
					ma.setUndoButtonActive(false);
				}
			}

			invalidate();

			if (result.getName().contains("rectangle") || result.getName().contains("circle")) 
			{
				
				String s = getResources().getString(R.string.concept);
				
				if (result.getName().contains("rectangle")) s = getResources().getString(R.string.individual);

				Toast toast = Toast.makeText(getContext(), s + " created!", Toast.LENGTH_LONG);
				toast.show();
				
				hideSuggestionButtons();
			}else{
				Toast.makeText(context, "handwriting", Toast.LENGTH_LONG).show();
				Message message = mHandler.obtainMessage();
				Bundle bundle = new Bundle();
				bundle.putString("result", "result");
				message.setData(bundle);
				mHandler.sendMessage(message);
			}
		}

	}

	/**
	 * Start the asynchronous gesture recognition
	 * 
	 * @param pointsForRecognizer2
	 *            The <b>Point</b> array
	 */
	private void asyncRecognizeGestues(CustomPath[] paths)
	{
		new RecognizerTask().execute(paths);
	}


	/**
	 * clear the canvas or delete highlighted elements selectively
	 */
	public void deleteOrClear() 
	{
		ma.setUnsaved(false);
		
		allowScale = true;
		allowTranslate = true;
		
		mode = DEFAULT;
		
		ma.activateScrollBarPanel(true);


		CustomPath path;

		DrawingComponent component;

		while (drawingObjects.getPathList().ContainsHighlightedPath()) 
		{

			ArrayList<DrawingComponent> propArray = new ArrayList<DrawingComponent>();
			
			for (int i = 0; i < drawingObjects.getPathList().getPaths().size(); i++) 
			{

				path = drawingObjects.getPathList().getPaths().get(i);

				if (path.isHighlighted()) 
				{

					component = drawingObjects.getObjectByPathId(path.getUid());

					// delete object if it is part of a written but not
					// confirmed word or sketch

					if (nStrokeCnt > 0 && ((component instanceof DrawingLeaf))) 
					{

						// remove stroke from the set that was delivered for
						// handwriting recognition



					}
					
					if (component instanceof FormalizedConcept)
					{
						ma.setOntopanelConceptListItemActive(component.getUri());

						for (DrawingComponent dc : ((FormalizedConcept) component).getRelations())
						{
							if (dc instanceof FormalizedPropertyRelation)
							{
								propArray.add(dc);
							}
						}
						
						
					} 
					else if (component instanceof FormalizedIndividual)
					{
						ma.setOntopanelIndividualListItemActive(component.getUri());
					}
					else if (component instanceof InstatiationRelation)
					{
						((InstatiationRelation) component).resetHelpText();		
						
						OntClass o = ma.getOntoModel().getOntClass(getResources().getString(R.string.onto_namespace) + "PlainIndividual");

						Individual individual = MainActivity.loadedOntoModel.createIndividual(((InstatiationRelation) component).getEndElement().getUri(), o);
					
						((InstatiationRelation) component).getEndElement().setOntResource(individual);
						
						ArrayList<DrawingComponent> tempArray = new ArrayList<DrawingComponent>();
						
						for (DrawingComponent dc : ((InstatiationRelation) component).getEndElement().getRelations())
						{
							if (dc instanceof FormalizedPropertyRelation)
							{
								((FormalizedPropertyRelation) dc).removeReferences();
								
								tempArray.add(dc);
							}
						}
						
						for (DrawingComponent dc : tempArray)
						{
							drawingObjects.removeComponent(dc);
						}
					}
					
					if (component instanceof CustomObjectRelation)
					{
						if (component instanceof SubClassRelation)
						{
							((SubClassRelation) component).resetHelpText();
						}
						
						((CustomObjectRelation) component).removeReferences();
					}
					else if (component instanceof DrawingPropertyRelation)
					{
						((DrawingPropertyRelation) component).removeReferences();
					}
					
					if (component.hasRelations())
					{
						List<DrawingComponent> temp = component.getRelations();
						
						int p = temp.size();
						
						for (int n = 0; n < p; n++)
						{
							DrawingComponent r = temp.get(0);
							
							if (r instanceof CustomObjectRelation)
							{
								((CustomObjectRelation) r).removeReferences();
							}
							else if (r instanceof DrawingPropertyRelation)
							{
								((DrawingPropertyRelation) r).removeReferences();
							}
							
							drawingObjects.removeComponent(r);
						}
						
						temp = null;
						
					}

					if (component.isGrouped()) 
					{
						component = component.getParent();
						drawingObjects.removeCompositeComponent(component);
						
					} 
					else if (component instanceof DrawingWordLetter) 
					{
						component = component.getParent();

						drawingObjects.removeCompositeComponent(component);
						
					} 
					else 
					{
//						if (component instanceof PersonObject)
//							((PersonObject) component).getContactPhoto().recycle();

						drawingObjects.removeComponent(component);
					}

				}
			}
		}

		if (nStrokeCnt == 0)
			cleanView();
		else {
			// notify recognizer thread about data availability
			ma.mBoundService.dataNotify(nStrokeCnt);
		}
		
		restoreBackupTransformationMatrix();

		configureButtonActivation();

		invalidate();

	}

	/**
	 * Delete the last attached <b>CustomPath</b> object from the respective
	 * <b>ScaledPathArray</b> object
	 * 
	 * @see com.drawing.datastructure.ScaledPathArray
	 * @see com.drawing.datastructure.CustomPath
	 */
	public void undo() 
	{
		// TODO: implement real history (maybe via an Array in
		// TODO: delete whole word if the last action was to create one
		// the DrawingComponent root)

		if (drawingObjects.getPathList().getPaths().size() > 0) 
		{
			
			allowScale = true;

			int lastPathIndex = -1;

			if (tempHandwritingPaths.size() > 0) 
			{

				lastPathIndex = tempHandwritingPaths.size() - 1;

				CustomPath undoPath = tempHandwritingPaths.get(lastPathIndex);

				DrawingComponent component = drawingObjects.getObjectByPathId(undoPath.getUid());

				Point point = new Point(0.0f, 0.0f);

				if (component != null)
					point = component.getPath().getOriginalVertices()[0];


			}

			// notify recognizer thread about data availability
			ma.mBoundService.dataNotify(nStrokeCnt);

			if (nStrokeCnt > 0) 
			{
				configureButtonActivation();
				invalidate();
			} 
			else
				cleanView();

		} 
		else 
		{

			// notify recognizer thread about data availability
			ma.mBoundService.dataNotify(nStrokeCnt);

			cleanView();

		}
	}

	private Set<String> mCurrMessage;

	// begin region hwr

	private int nStrokeCnt;


	/**
	 * Reset the handwriting recognizer and respective help structures
	 * 
	 * @see com.drawing.application.MainActivity.OnInkViewListener#cleanView()
	 */
	public void cleanView() 
	{
		allowScale = true;
		
		ma.setUnsaved(false);
		
		tempHandwritingPaths.clear();
		mCurrMessage = null;

		gestureRecognitionEnabled = true;

		hideSuggestionButtons();

		configureButtonActivation();

		invalidate();

	}

	/**
	 * Toggle button activation according to possible actions
	 */
	protected void configureButtonActivation() 
	{
		int highlightedComponents = ((DrawingComposite) drawingObjects).getHighlightedComponents().size();

		// manage group button

		// attach the respective functionality and the suiting icon to the
		// button
		// depending on whether the selected objects can be grouped or ungrouped

		List<CustomPath> highlightedPaths = drawingObjects.getHighlightedComponents();

		DrawingComponent component;

		boolean switchGroupButton = false;
		
		boolean allowDelete = true;

		int groupCount = 0;

		for (CustomPath path : drawingObjects.getHighlightedComponents()) 
		{
			component = drawingObjects.getObjectByPathId(path.getUid());
			
			switch (path.getOntoType())
			{
				case FORMALIZEDINSTANTIATION:
					allowDelete = false;
					break;
				case FORMALIZEDPROPERTYRELATION:
					allowDelete = false;
					break;
				case FORMALIZEDSUBCONCEPTRELATION:
					allowDelete = false;
					break;
				case DRAWNCONCEPT:
					allowDelete = true;
					break;
				case DRAWNINDIVIDUAL:
					allowDelete = true;
					break;
				case DRAWNINSTANTIATION:
					allowDelete = true;
					break;
				case DRAWNPROPERTYRELATION:
					allowDelete = true;
					break;
				case DRAWNSUBCONCEPTRELATION:
					allowDelete = true;
					break;
				case FORMALIZEDCONCEPT:
					allowDelete = true;
					break;
				case FORMALIZEDINDIVIDUAL:
					allowDelete = true;
					break;
				case NONE:
					allowDelete = true;
					break;
				default:
					allowDelete = true;
					break;
			}
			
			if (component instanceof DrawingGroup) 
			{
				groupCount++;

				for (DrawingComponent child : ((DrawingGroup) component).children) 
				{
					if (!(child instanceof DrawingCompositeWord))
					{
						highlightedPaths.remove(child.getPath());
					}
					else
					{
						try 
						{
							highlightedPaths.remove(((DrawingCompositeWord) child).children.get(0).getPath());
						} 
						catch (IndexOutOfBoundsException indEx) 
						{
							indEx.printStackTrace();
						}
					}

				}

				highlightedPaths.remove(component.getPath());

				if (highlightedPaths.size() == 0 && !(groupCount > 1)) 
				{
					switchGroupButton = true;
					break;
				}
			}

		}
		
		/* FS

		if (switchGroupButton) {

			ma.findViewById(R.id.groupButton).setVisibility(Button.INVISIBLE);

			ma.findViewById(R.id.ungroupButton).setVisibility(Button.VISIBLE);

		} else

		{
			ma.findViewById(R.id.ungroupButton)
					.setVisibility(Button.INVISIBLE);

			ma.findViewById(R.id.groupButton).setVisibility(Button.VISIBLE);
		}

		if (highlightedComponents > 1) {
			ma.findViewById(R.id.groupButton).setEnabled(true);
		} else {
			ma.findViewById(R.id.groupButton).setEnabled(false);
		}

		 */

		// manage delete button
		if (highlightedComponents >= 1) 
		{
			if (allowDelete) ma.setDeleteButtonActive(true);
		} 
		else 
		{
			ma.setDeleteButtonActive(false);
		}

		// manage undo button
		if (tempHandwritingPaths.size() > 0) 
		{
			ma.setUndoButtonActive(true);
		} 
		else 
		{
			ma.setUndoButtonActive(false);
		}
		
		

	}

	// Define the Handler that receives messages from the thread and update the
	// progress
	private final Handler mHandler = new Handler() 
	{

		/**
		 * Handle the message including the recognition result from the
		 * <b>RecognizerService</b>
		 */
		public void handleMessage(Message msg) 
		{
			if (!msg.getData().isEmpty()) 
			{
				mCurrMessage = msg.getData().keySet();

				changeAlternatives = true;
				View editText = alternativeWordListView.findViewById(R.id.editText);
				editText.setVisibility(VISIBLE);
				alternativeWordListView.findViewById(R.id.buttonConfirm).setVisibility(VISIBLE);
				alternativeWordListView.findViewById(R.id.buttonCancel).setVisibility(VISIBLE);
			}

			invalidate();
		}

	};

	private EditText editText;

	private Button confirmButton;

	private Button cancelButton;

	/**
	 * @see android.view.View#getHandler()
	 */
	public Handler getHandler() 
	{
		return mHandler;
	}

	// end region hwr

	/**
	 * Set the panel for alternative words and initialize the respective button
	 * listeners for handling confirmation requests
	 * 
	 * @param alternativeWordListView
	 *            The LinearLayout object holding the buttons with alternative
	 *            word suggestions
	 */
	public void initializeAlternativeWordListView(LinearLayout alternativeWordListView)
	{
		DrawView.alternativeWordListView = alternativeWordListView;
		editText = (EditText) alternativeWordListView.findViewById(R.id.editText);
		confirmButton = (Button) alternativeWordListView.findViewById(R.id.buttonConfirm);
		cancelButton = (Button) alternativeWordListView.findViewById(R.id.buttonCancel);
		editText.setOnEditorActionListener(new OnEditorActionListener() {
			
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				confirmWord(editText.getText().toString());
				editText.setVisibility(GONE);
				editText.setText("");
				confirmButton.setVisibility(GONE);
				cancelButton.setVisibility(GONE);
				InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
				return true;
			}
		});
		
		OnClickListener listener = new View.OnClickListener() 
		{
			public void onClick(View v)
			{
				confirmWord(editText.getText().toString());
				editText.setVisibility(GONE);
				editText.setText("");
				confirmButton.setVisibility(GONE);
				cancelButton.setVisibility(GONE);
				InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
			}
		};
		
		cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				editText.setVisibility(GONE);
				editText.setText("");
				confirmButton.setVisibility(GONE);
				cancelButton.setVisibility(GONE);
				InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
			}
		});
		
		confirmButton.setOnClickListener(listener);
		
//		OnClickListener alternativeWordButtonListener2 = new View.OnClickListener() 
//		{
//			public void onClick(View v) 
//			{
//				confirmPlaceholder(getResources().getString(R.string.placeholder));
//			}
//		};
//		
//		int childCount = alternativeWordListView.getChildCount();
//		
//		for (int i = 0; i < childCount-1; i++) 
//		{
//			alternativeWordListView.getChildAt(i).setOnClickListener(alternativeWordButtonListener);
//		}
//		
//		alternativeWordListView.getChildAt(childCount-1).setOnClickListener(alternativeWordButtonListener2);

	}

	/**
	 * Hide all buttons that display a suggestion for the handwriting
	 * recognition
	 */
	private void hideSuggestionButtons() 
	{
		for (int i = 0; i < alternativeWordListView.getChildCount(); i++) 
		{
			alternativeWordListView.getChildAt(i).setVisibility(Button.INVISIBLE);

		}

	}

	/**
	 * Add the recognized word to the data structure after it was confirmed by
	 * the user
	 * 
	 * @param word
	 *            The recognized String that will be attached to the respective
	 *            strokes
	 */
	public void confirmWord(String word) 
	{
		if (tempHandwritingPaths.size() > 0) 
		{
			ArrayList<DrawingComponent> wordChildren = new ArrayList<DrawingComponent>();

			// remove strokes from temporary drawing objects and attach them to
			// a composite word
			for (CustomPath path : tempHandwritingPaths) 
			{
				path.setColor(getResources().getColor(R.color.sketch));
				
				drawingObjects.removeComponent(drawingObjects.getObjectByPathId(path.getUid()));
				DrawingWordLetter wordLetter = new DrawingWordLetter(path, new Matrix());
				wordChildren.add(wordLetter);
			}

			DrawingCompositeWord compositeWord = new DrawingCompositeWord(wordChildren, word.replace(" ", ""));

			drawingObjects.addComponent(compositeWord);

			Toast toast = Toast.makeText(getContext(), "The word '" + word + "' was recognized!", Toast.LENGTH_LONG);
			toast.show();

			cleanView();

		}

	}
	
	/**
	 * Add the recognized word to the data structure after it was confirmed by
	 * the user
	 * 
	 * @param word
	 *            The recognized String that will be attached to the respective
	 *            strokes
	 */
	public void confirmPlaceholder(String word) 
	{
		if (tempHandwritingPaths.size() > 0) 
		{
			ArrayList<DrawingComponent> wordChildren = new ArrayList<DrawingComponent>();

			// remove strokes from temporary drawing objects and attach them to
			// a composite word
			for (CustomPath path : tempHandwritingPaths) 
			{
				path.setColor(getResources().getColor(R.color.sketch));
				
				drawingObjects.removeComponent(drawingObjects.getObjectByPathId(path.getUid()));
				DrawingWordLetter wordLetter = new DrawingWordLetter(path, new Matrix());
				wordChildren.add(wordLetter);
			}

			DrawingCompositeWord compositeWord = new DrawingCompositeWord(wordChildren, word);

			drawingObjects.addComponent(compositeWord);

			Toast toast = Toast.makeText(getContext(), "A placeholder was recognized!", Toast.LENGTH_LONG);
			toast.show();

			cleanView();

		}

	}

	/**
	 * Getter for the <b>DrawingComponent</b> of the <b>View</b>
	 * 
	 * @return The components that represent the created data structure
	 */
	public DrawingComposite getDrawingObjects() 
	{
		return drawingObjects;
	}

	/**
	 * Getter for the <b>DrawingComponent</b> of the <b>View</b>
	 * 
	 * @param drawingObjects
	 *            The components that represent the data structure that should
	 *            be attached
	 */
	public void setDrawingObjects(DrawingComposite drawingObjects) 
	{
		// All paths need to be redrawn due to an deserialization bug of the
		// original Path object
		// of Android, because otherwise nothing would be drawn on the canvas

		drawingObjects.redrawPathsafterDeserialization(ma);

		this.drawingObjects = drawingObjects;

		invalidate();
	}

	/**
	 * This method adjusts the view port of the canvas so that every stroke that
	 * has been drawn is contained
	 */
	public void adjustViewPort() 
	{
		Log.d("DrawView","adjustViewPort ");

		if (drawingObjects.getChild().size() > 0) {

			float[] extrema = { Float.POSITIVE_INFINITY, -1,
					Float.POSITIVE_INFINITY, -1 };

			Matrix adjustmentMatrix = new Matrix();

			extrema = drawingObjects.determineDrawnExtrema(extrema);

			Point viewCenter = new Point(getWidth() / 2, getHeight() / 2);

			float heightScale = 1.0f;

			float widthScale = 1.0f;

			float appliedScale = 1.0f;

			int offset = 100;

			float extremaWidth = (float) Math.abs(extrema[1] - extrema[0]);

			float extremaHeight = (float) Math.abs(extrema[3] - extrema[2]);

			Point extremaCenter = new Point((extrema[0] + extrema[1]) / 2,
					(extrema[2] + extrema[3]) / 2);

			widthScale = ((getWidth() - offset) / extremaWidth);

			heightScale = ((getHeight() - offset) / extremaHeight);

			if (widthScale > 1.0) {

				if (heightScale > 1.0) {
					if (widthScale > heightScale) {
						appliedScale = heightScale;
					} else {
						appliedScale = widthScale;
					}
				} else {
					appliedScale = heightScale;
				}

			} else {
				if (heightScale > 1.0) {
					appliedScale = widthScale;
				} else {
					if (widthScale < heightScale) {
						appliedScale = widthScale;
					} else {
						appliedScale = heightScale;
					}
				}

			}

			adjustmentMatrix.postScale(appliedScale, appliedScale);
			extremaCenter.x = extremaCenter.x * appliedScale;
			extremaCenter.y = extremaCenter.y * appliedScale;

			adjustmentMatrix.postTranslate(viewCenter.x - extremaCenter.x,
					viewCenter.y - extremaCenter.y);

			savedMatrix.set(matrix);

			matrix.set(adjustmentMatrix);

		}
		else 
		{
			matrix.set(new Matrix());
		}

	}


	/**
	 * Group highlighted objects together so that they can be treated uniformly
	 * regardless of their physical location
	 */
	public void groupHighlightedObjects() {

		ArrayList<DrawingComponent> componentsToAdd = new ArrayList<DrawingComponent>();

		if (drawingObjects.getHighlightedComponents().size() > 1) {

			ArrayList<DrawingComponent> componentList = new ArrayList<DrawingComponent>();

			DrawingComponent component;

			for (CustomPath path : drawingObjects.getHighlightedComponents()) {

				component = drawingObjects.getObjectByPathId(path.getUid());

				if (component != null) {

					if (component instanceof DrawingGroup) {

						componentList
								.addAll(((DrawingGroup) component).children);

						drawingObjects.removeCompositeComponent(component);
					} else

					if (component instanceof DrawingWordLetter) {

						component = ((DrawingWordLetter) component).getParent();

						component.setGrouped(true);

						if (!componentList.contains(component)) {

							componentList.add(component);

							drawingObjects.removeCompositeComponent(component);

						}

					} else {

						if (component instanceof DrawingComposite) {

							for (DrawingComponent child : ((DrawingComposite) component)
									.getLinearChildObjectList()) {
								if (!componentList.contains(child)
										&& child.isHighlighted()) {
									child.setGrouped(true);

									{

										if (!(child instanceof DrawingGroup))
											componentList.add(child);

									}

									if (child instanceof DrawingCompositeWord) {

										drawingObjects
												.removeCompositeComponent(child);

									} else
										drawingObjects.removeComponent(child);

								} else {

									componentsToAdd.add(child);

								}
							}

						} else {

							component.setGrouped(true);

							if (!componentList.contains(component)) {

								componentList.add(component);

								drawingObjects.removeComponent(component);

							}
						}

					}

				}
			}

			for (DrawingComponent child : componentsToAdd) {
				drawingObjects.addComponent(child);
			}

			DrawingGroup group = new DrawingGroup(componentList,
					new CustomPath(), new Matrix());

			drawingObjects.addComponent(group);

			cleanView();

		}
	}

	/**
	 * Ungroup highlighted objects and reintegrate them into the existing data
	 * structure
	 */
	public void ungroupHighlightedObjects() 
	{

		List<CustomPath> highlightedPaths = drawingObjects.getHighlightedComponents();

		DrawingComponent component;
				
		removeHighlightFromPaths();
		
		for (CustomPath path : highlightedPaths) 
		{
			component = drawingObjects.getObjectByPathId(path.getUid());

			if (component instanceof DrawingGroup) 
			{
				drawingObjects.removeCompositeComponent(component);

				for (DrawingComponent child : ((DrawingGroup) component).children) 
				{
					child.setGrouped(false);
									
					child.setHighlighted(false);

					drawingObjects.addComponent(child);
				}
			}
		}

		configureButtonActivation();
		
		invalidate();

	}	
	
	/**
	 * Recalculate the screen size in order to handle orientation changes properly
	 */
	public void recalculateScreenSize() 
	{
		Display display = ma.getWindowManager().getDefaultDisplay();
		android.graphics.Point size = new android.graphics.Point();
		display.getSize(size);

		screenHeight = size.y;
		screenWidth = size.x;
		canvasHeight = size.y;
	}



	/**
	 * Remves the Array of FormalizedPropertyRelationButtons from the SketchBoard
	 * 
	 * @param formalizedRelationbuttons
	 */
	public static void removeFormalizedRelationButtons(ArrayList<FormalizedPropertyRelationButton> formalizedRelationbuttons) 
	{
		for (FormalizedPropertyRelationButton fb :formalizedRelationbuttons)
		{
			if(drawingObjects.children.contains(fb)) drawingObjects.children.remove(fb);
		}
		
	}

}
