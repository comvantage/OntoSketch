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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VerticalSeekBar;

import com.drawing.datastructure.CustomObjectRelation;
import com.drawing.datastructure.DrawingComponent;
import com.drawing.datastructure.DrawingConcept;
import com.drawing.datastructure.DrawingIndividual;
import com.drawing.datastructure.DrawingPropertyRelation;
import com.drawing.datastructure.FormalizedConcept;
import com.drawing.datastructure.FormalizedIndividual;
import com.drawing.datastructure.FormalizedObject;
import com.drawing.datastructure.FormalizedPropertyRelation;
import com.drawing.datastructure.GestureTypes;
import com.drawing.datastructure.InstatiationRelation;
import com.drawing.datastructure.OntologyObjectTypes;
import com.drawing.ontosketch.R;
import com.drawing.ontosketch.ui.ClassListItem;
import com.drawing.ontosketch.ui.FilterListHeaderItem;
import com.drawing.ontosketch.ui.IndividualListItem;
import com.drawing.ontosketch.ui.ListHeaderItem;
import com.drawing.ontosketch.ui.ListItem;
import com.drawing.ontosketch.ui.ListItem.ListItemState;
import com.drawing.ontosketch.ui.PropertyListItem;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hwr.android.RecoInterface.RecognizerService;


public class MainActivity extends Activity 
{

	private Boolean ontoPanelOpenState = true;

	/**
	 * Hold a reference to the current animator, so that it can be canceled
	 * mid-way.
	 */
	private Animator mCurrentAnimator;
	
	private View overlay;

	/**
	 * The system "short" animation time duration, in milliseconds. This
	 * duration is ideal for subtle animations or animations that occur very
	 * frequently.
	 */
	private int mShortAnimationDuration = 500;
	
	private DrawView sketchBoard;
	
	private ToggleButton btnDebugDraw;
	
	/**
	 *Array of the uris of all imprted Ontologies 
	 */
	private ArrayList<String> ontologyFileNames = new ArrayList<String>();
	
	private LinearLayout ontopanelConceptList;
	private LinearLayout ontopanelIndividualList;
	private LinearLayout ontopanelPropertyList;
	
	private ScrollView ontopanelConceptContainer;
	private ScrollView ontopanelIndividualContainer;
	private ScrollView ontopanelPropertyContainer;
	
	private LinearLayout ontopanelSearchConceptList;
	private LinearLayout ontopanelSearchIndividualList;
	private LinearLayout ontopanelSearchPropertyList;
	
	private LinearLayout ontopanelSearchConceptContainer;
	private LinearLayout ontopanelSearchIndividualContainer;
	private LinearLayout ontopanelSearchPropertyContainer;
	
	private FilterListHeaderItem ontopanelSearchConceptItem;
	private FilterListHeaderItem ontopanelSearchIndividualItem;
	private FilterListHeaderItem ontopanelSearchPropertyItem;
	
	private EditText searchInput;
	
	private LinkedList<Individual> individuals = new LinkedList<Individual>();
	private static LinkedList<OntProperty> properties = new LinkedList<OntProperty>();
	
	private ImageButton btnOntoPanelOpenClose;
	
	private Button btnAbstract, btnConcrete, btnCenter;
	
	public static OntModel loadedOntoModel;

	/**
	 * List of general properties which are not displayed in the ontoPanel
	 */
	private List<OntProperty> generalProperties;
	
	/**
	 * List of colors for Properties
	 */
	private static List<Integer> propertyColors = new LinkedList<Integer>();
	
	private TabHost tabHost;
	
	private RelativeLayout progressLayer;
	private ImageView progressIcon;
	private Animation progressRotation;
	
	private boolean activeSearch = false;
	
	private boolean unsaved = false;

	// begin region hwr

		/**
		 * @author Christian Brändel TU Dresden / SAP Research Dresden
		 * @author hwr Corp
		 * 
		 *         Interface that enables to use a Handler that listens to events
		 *         from the handwriting recognition class
		 * 
		 */
		public interface OnInkViewListener {

			void cleanView();

			/**
			 * Getter for the handler of handwriting recognition events
			 * 
			 * @return The handler for handwriting recognition events
			 */
			Handler getHandler();
		}

		private OnInkViewListener mListener;

		private String[] filenames;

		@SuppressWarnings("unused")
		private boolean mRecoInit;

		/**
		 * The service binding for the handwriting recognition API
		 */
		public RecognizerService mBoundService;
		
		private boolean boundToService = true;
			
		private ServiceConnection mConnection = new ServiceConnection() 
		{
			public void onServiceConnected(ComponentName className, IBinder service) 
			{
				// This is called when the connection with the service has been
				// established, giving us the service object we can use to
				// interact with the service. Because we have bound to a explicit
				// service that we know is running in our own process, we can
				// cast its IBinder to a concrete class and directly access it.

				mBoundService = ((RecognizerService.RecognizerBinder) service)
						.getService();
				mBoundService.mHandler = mListener.getHandler();
				
				Log.d("RecoService", "connected");
				
			}

			public void onServiceDisconnected(ComponentName className) 
			{
				// This is called when the connection with the service has been
				// unexpectedly disconnected -- that is, its process crashed.
				// Because it is running in our same process, we should never
				// see this happen.
				mBoundService = null;
		
				boundToService = false;
				
				Log.d("RecoService", "disconnected");
			}
		};

		// end region hwr

		/**
		 * @see android.app.Activity#onConfigurationChanged(android.content.res.Configuration)
		 */
		public void onConfigurationChanged(android.content.res.Configuration newConfig) 
		{
		
			//recalculate size of the canvas, after the orientation of the device has changed
			
			drawView.recalculateScreenSize();
			
			super.onConfigurationChanged(newConfig);
			
		};
		
		protected void onDestroy() {

			if(boundToService && mConnection != null)
			{
				unbindService(mConnection);
				boundToService = false;
			}
			
			super.onDestroy();
			
		}
		
		@Override
		protected void onPause() {
		
			super.onPause();
		}
		
		@Override
		protected void onResume() {
						
			super.onResume();
		}
		
		/**
		 * Overwrote the Android back key functionality in order to ensure that
		 * temporary changes to the canvas and created objects aren't discarded  
		 * @see android.app.Activity#onBackPressed()
		 */
		public void onBackPressed()
		{		
			   Intent setIntent = new Intent(Intent.ACTION_MAIN);
			   setIntent.addCategory(Intent.CATEGORY_HOME);
			   setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			   startActivity(setIntent);
		}
		
		DrawView drawView;

		public MenuItem undoButton, deleteButton;
		
		public VerticalSeekBar scrollBar;

		/**
		 * Called when the activity is first created.
		 * 
		 * @param savedInstanceState
		 * @see android.app.Activity#onCreate(android.os.Bundle)
		 * @see android.os.bundle.Bundle
		 */
		public void onCreate(Bundle savedInstanceState) 
		{
			super.onCreate(savedInstanceState);
			
			// begin region hwr

			// load and initialize recognizer library
			File dir = getDir("user", 0);
			String sDir = dir.getAbsolutePath();
			String lName = null;

			

			// end region hwr

			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);

			setContentView(R.layout.activity_main);
			
			drawView = (DrawView) findViewById(R.id.sketchingBoard);

			drawView.initializeAlternativeWordListView((LinearLayout) findViewById(R.id.alternativeWordsPanel));

			drawView.requestFocus();

			mListener = drawView;
			
			// startService
			
			ComponentName recognizerService = startService(new Intent(
					this, RecognizerService.class));

			bindService(new Intent(this,
					RecognizerService.class), mConnection, Context.BIND_AUTO_CREATE);

			boundToService = true;
			
			filenames = getFilesDir().list();
			
			for (int i = 0; i <= 5; i++)
				fillColorList();
		
			ontopanelConceptList = (LinearLayout) findViewById(R.id.classList);
			ontopanelIndividualList = (LinearLayout) findViewById(R.id.individualList);
			ontopanelPropertyList = (LinearLayout) findViewById(R.id.propertyList);
			
			ontopanelConceptContainer = (ScrollView) findViewById(R.id.scrollView1);
			ontopanelIndividualContainer = (ScrollView) findViewById(R.id.scrollView2);
			ontopanelPropertyContainer = (ScrollView) findViewById(R.id.scrollView3);
			
			ontopanelSearchConceptList = (LinearLayout) findViewById(R.id.searchClassList);
			ontopanelSearchIndividualList = (LinearLayout) findViewById(R.id.searchIndividualsList);
			ontopanelSearchPropertyList = (LinearLayout) findViewById(R.id.searchPropertiesList);
			
			ontopanelSearchConceptContainer = (LinearLayout) findViewById(R.id.searchClassesContainer);
			ontopanelSearchIndividualContainer = (LinearLayout) findViewById(R.id.searchIndividualsContainer);
			ontopanelSearchPropertyContainer = (LinearLayout) findViewById(R.id.searchPropertiesContainer);
			
			ontopanelSearchConceptItem = (FilterListHeaderItem) findViewById(R.id.searchClassesItem);
			ontopanelSearchIndividualItem = (FilterListHeaderItem) findViewById(R.id.searchIndividualsItem);
			ontopanelSearchPropertyItem = (FilterListHeaderItem) findViewById(R.id.searchPropertiesItem);
			
			searchInput = (EditText) findViewById(R.id.searchInput);

			searchInput.setOnTouchListener(new OnTouchListener() 
			{
				@Override
				public boolean onTouch(View v, MotionEvent event) 
				{
					searchInput.setCursorVisible(true);
					return false;
				}
				
			});
			
			searchInput.setOnEditorActionListener(new OnEditorActionListener() 
			{
				
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
				{
					if (!v.getText().toString().equalsIgnoreCase("")) 
					{
						updateSearch("",v.getText().toString());
						searchInput.setCursorVisible(false);
					}
					else
					{
						closeFilter();
					}
					
					return false;
				}
			});
			
			searchInput.addTextChangedListener(new TextWatcher() 
			{	
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count)
				{

				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after)
				{
					searchInput.setCursorVisible(true);
				}
				
				@Override
				public void afterTextChanged(Editable s)
				{
					if (!searchInput.getText().toString().equalsIgnoreCase("")) updateSearch("",searchInput.getText().toString());
					else
					{
						closeFilter();
					}
					
				}
			});
			
			searchInput.setCursorVisible(false);

			tabHost = (TabHost) findViewById(android.R.id.tabhost);

			tabHost.setup();

			TabSpec classesTabSpec = tabHost.newTabSpec("tab1");
			TabSpec individualsTabSpec = tabHost.newTabSpec("tab2");
			TabSpec propertiesTabSpec = tabHost.newTabSpec("tab3");
			

//			View tabIndicator1 = LayoutInflater.from(this).inflate(R.layout.tab_indicator, tabHost.getTabWidget(), false);
//			((TextView) tabIndicator1.findViewById(R.id.title)).setText(getText(R.string.tabname_classes));
//			((ImageView) tabIndicator1.findViewById(R.id.icon)).setBackgroundResource(R.drawable.ic_content_delete_active);
//
//			classesTabSpec.setIndicator(tabIndicator1);
//			classesTabSpec.setContent(R.id.ontoClassesTab);
//			
//			View tabIndicator2 = LayoutInflater.from(this).inflate(R.layout.tab_indicator, tabHost.getTabWidget(), false);
//			((TextView) tabIndicator2.findViewById(R.id.title)).setText(getText(R.string.tabname_individuals));
//			((ImageView) tabIndicator2.findViewById(R.id.icon)).setBackgroundResource(R.drawable.ic_content_delete_active);
//
//			individualsTabSpec.setIndicator(tabIndicator2);
//			individualsTabSpec.setContent(R.id.ontoIndividualsTab);
//			
//			View tabIndicator3 = LayoutInflater.from(this).inflate(R.layout.tab_indicator, tabHost.getTabWidget(), false);
//			((TextView) tabIndicator3.findViewById(R.id.title)).setText(getText(R.string.tabname_properties));
//			((ImageView) tabIndicator3.findViewById(R.id.icon)).setBackgroundResource(R.drawable.ic_content_delete_active);
//
//			propertiesTabSpec.setIndicator(tabIndicator3);
//			propertiesTabSpec.setContent(R.id.ontoPropertiesTab);
			
			classesTabSpec.setIndicator(getText(R.string.tabname_classes)).setContent(R.id.ontoClassesTab);;
			individualsTabSpec.setIndicator(getText(R.string.tabname_individuals)).setContent(R.id.ontoIndividualsTab);
			propertiesTabSpec.setIndicator(getText(R.string.tabname_properties)).setContent(R.id.ontoPropertiesTab);

			tabHost.addTab(classesTabSpec);
			tabHost.addTab(individualsTabSpec);
			tabHost.addTab(propertiesTabSpec);
			
//			for(int i=0;i<tabHost.getTabWidget().getChildCount();i++) 
//	        { 
//	            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title); //Unselected Tabs
//	            if (i == 0) tv.setTextColor(getResources().getColor(R.color.concept));
//	            else if (i == 1) tv.setTextColor(getResources().getColor(R.color.individual));
////	            else if (i == 2) tv.setTextColor(Color.parseColor("#ffffff"));
//	        } 
			
//	        TextView tv = (TextView) tabHost.getCurrentTabView().findViewById(android.R.id.title); //for Selected Tab
//	        tv.setTextColor(Color.parseColor("#000000"));
			
			overlay = (View) findViewById(R.id.overlay);

			btnOntoPanelOpenClose = (ImageButton) findViewById(R.id.ontoPanelCloseBtn);
			
			btnOntoPanelOpenClose.setOnClickListener(new OnClickListener() 
			{
				@SuppressLint("NewApi")
				public void onClick(View v) 
				{
					if (mCurrentAnimator != null) 
					{
						mCurrentAnimator.cancel();
					}

					float endX;

					if (ontoPanelOpenState) 
					{
						// close
						endX = -365;
						btnOntoPanelOpenClose.setImageDrawable(getResources().getDrawable(R.drawable.ic_onto_open));
						ontoPanelOpenState = false;
					} 
					else 
					{
						// open
						endX = 0;
						btnOntoPanelOpenClose.setImageDrawable(getResources().getDrawable(R.drawable.ic_onto_close));
						ontoPanelOpenState = true;
					}

					AnimatorSet set = new AnimatorSet();
					set.play(ObjectAnimator.ofFloat(overlay, View.X, endX));
					set.setDuration(mShortAnimationDuration);
					set.setInterpolator(new DecelerateInterpolator());
					set.addListener(new AnimatorListenerAdapter() 
					{
						@Override
						public void onAnimationEnd(Animator animation) 
						{
							mCurrentAnimator = null;
						}

						@Override
						public void onAnimationCancel(Animator animation) 
						{
							mCurrentAnimator = null;
						}
					});
					
					set.start();
					mCurrentAnimator = set;

				}
			});


			sketchBoard = (DrawView) findViewById(R.id.sketchingBoard);
			
			sketchBoard.setOnDragListener(new SketchBoardDragListener());
			
			RelativeLayout scrollLayer = (RelativeLayout) findViewById(R.id.scrollLayer);
			
			scrollLayer.setOnTouchListener(new OnTouchListener()
			{
				//catch touches to avoid drawing during scrolling
				@Override
				public boolean onTouch(View v, MotionEvent event) 
				{
					return true;
				}
			});
			
			scrollBar = (VerticalSeekBar) findViewById(R.id.scrollBar);
			
			scrollBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() 
			{
				
				int oldProgress = 0;
				
				@Override
				public void onStopTrackingTouch(SeekBar seekBar)
				{
					if (oldProgress != seekBar.getProgress()) 
						seekBar.setProgress(oldProgress);
				}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar)
				{
					oldProgress = seekBar.getProgress();
				}
				
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
				{			
					if (sketchBoard.getMode() < 0)
					{
						oldProgress = progress;
						
						for (int i = 0; i < sketchBoard.drawingObjects.getPathList().getPaths().size(); i++) 
						{
							DrawingComponent dc = sketchBoard.drawingObjects.getObjectByPathId(sketchBoard.drawingObjects.getPathList().getPath(i).getUid());
							
							dc.setAlpha(calculateAlpha(dc, progress));
						}
						
						if (progress <= 40)
						{
						    btnCenter.setTextColor(getResources().getColor(R.color.black));
						}
						else if (progress <= 60)
						{
						    btnCenter.setTextColor(getResources().getColor(R.color.highlight));
						}
						else 
						{
						    btnCenter.setTextColor(getResources().getColor(R.color.black));
						}
						
						sketchBoard.invalidate();
					}
				}
			});
			
			toggleOntoPannel(false,0);
			
			btnAbstract = (Button) findViewById(R.id.buttonAbstract);
			btnConcrete = (Button) findViewById(R.id.buttonConcrete);
			btnCenter = (Button) findViewById(R.id.buttonCenter);
			
			RotateAnimation a = new RotateAnimation(0, 270,Animation.RELATIVE_TO_SELF, 0.5f,Animation.RELATIVE_TO_SELF, 0.5f);
			a.setFillAfter(true);
			a.setDuration(0);
			btnAbstract.startAnimation(a);
			btnConcrete.startAnimation(a);
			btnCenter.startAnimation(a);
			
			progressLayer = (RelativeLayout) findViewById(R.id.progressLayer);
			progressIcon = (ImageView) findViewById(R.id.progressIcon);
			
			progressLayer.setOnTouchListener(new OnTouchListener()
			{
				//catch touches to avoid drawing during progessViz
				@Override
				public boolean onTouch(View v, MotionEvent event) 
				{
					return true;
				}
			});
			
			progressRotation = AnimationUtils.loadAnimation(this, R.layout.rotate_animation);
			progressRotation.setRepeatCount(Animation.INFINITE);

			btnDebugDraw = (ToggleButton) findViewById(R.id.debugDrawRegions);
			
			scrollBar.setProgress(50);

			//to fill the generalProperties List
			loadOntology("", true);
		}

		private void showProgressLayer()
		{
//			progressIcon.startAnimation(progressRotation);
//			progressLayer.setVisibility(View.VISIBLE);
		}
		
		private void hideProgressLayer()
		{
			progressIcon.clearAnimation();
			progressLayer.setVisibility(View.GONE);
		}

		private void fillColorList()
		{
			propertyColors.add(Integer.valueOf(getResources().getColor(R.color.propertyRelation01)));
			propertyColors.add(Integer.valueOf(getResources().getColor(R.color.propertyRelation02)));
			propertyColors.add(Integer.valueOf(getResources().getColor(R.color.propertyRelation03)));
			propertyColors.add(Integer.valueOf(getResources().getColor(R.color.propertyRelation04)));
			propertyColors.add(Integer.valueOf(getResources().getColor(R.color.propertyRelation05)));
			propertyColors.add(Integer.valueOf(getResources().getColor(R.color.propertyRelation06)));
			propertyColors.add(Integer.valueOf(getResources().getColor(R.color.propertyRelation07)));
			propertyColors.add(Integer.valueOf(getResources().getColor(R.color.propertyRelation08)));
			propertyColors.add(Integer.valueOf(getResources().getColor(R.color.propertyRelation09)));
			propertyColors.add(Integer.valueOf(getResources().getColor(R.color.propertyRelation10)));
			propertyColors.add(Integer.valueOf(getResources().getColor(R.color.propertyRelation11)));
			propertyColors.add(Integer.valueOf(getResources().getColor(R.color.propertyRelation12)));
			propertyColors.add(Integer.valueOf(getResources().getColor(R.color.propertyRelation13)));
			propertyColors.add(Integer.valueOf(getResources().getColor(R.color.propertyRelation14)));
			propertyColors.add(Integer.valueOf(getResources().getColor(R.color.propertyRelation15)));
			propertyColors.add(Integer.valueOf(getResources().getColor(R.color.propertyRelation16)));
			propertyColors.add(Integer.valueOf(getResources().getColor(R.color.propertyRelation17)));
			propertyColors.add(Integer.valueOf(getResources().getColor(R.color.propertyRelation18)));
			propertyColors.add(Integer.valueOf(getResources().getColor(R.color.propertyRelation19)));
			propertyColors.add(Integer.valueOf(getResources().getColor(R.color.propertyRelation20)));
		}

		/**
		 * Calculates the alpha value of the DrawingComponent according to the progress value of the slider
		 * @param dc DrawingComponent
		 * @param progress value of the slider
		 * @return new alpha
		 */
		protected int calculateAlpha(DrawingComponent dc, int progress)
		{
			int alpha = 255;

			switch(dc.getDisplayState())
			{
				case CONCEPT:
					
					if(progress<=60) alpha = 255;
					else if(progress<=61) alpha = 230;
					else if(progress<=62) alpha = 205;
					else if(progress<=63) alpha = 180;
					else if(progress<=64) alpha = 155;
					else if(progress<=65) alpha = 130;
					else if(progress<=66) alpha = 105;
					else if(progress<=67) alpha = 80;
					else if(progress<=68) alpha = 55;
					else if(progress<=69) alpha = 30;
					else alpha = 0;
					
					break;
					
				case CONCEPTRELATION:
					
					if(progress>=40) alpha = 0;
					else if(progress>=37) alpha = 30;
					else if(progress>=34) alpha = 55;
					else if(progress>=31) alpha = 80;
					else if(progress>=28) alpha = 105;
					else if(progress>=25) alpha = 130;
					else if(progress>=22) alpha = 155;
					else if(progress>=19) alpha = 180;
					else if(progress>=15) alpha = 205;
					else if(progress>=10) alpha = 230;
					else alpha = 255;
					
					break;
					
				case CONCEPTSUBCLASSRELATION:
	
					if(progress<=60) alpha = 255;
					else if(progress<=61) alpha = 230;
					else if(progress<=62) alpha = 205;
					else if(progress<=63) alpha = 180;
					else if(progress<=64) alpha = 155;
					else if(progress<=65) alpha = 130;
					else if(progress<=66) alpha = 105;
					else if(progress<=67) alpha = 80;
					else if(progress<=68) alpha = 55;
					else if(progress<=69) alpha = 30;
					else alpha = 0;
					
					break;
					
				case INDIVIDUAL:
					
					if(progress>=40) alpha = 255;
					else if(progress>=39) alpha = 230;
					else if(progress>=38) alpha = 205;
					else if(progress>=37) alpha = 180;
					else if(progress>=36) alpha = 155;
					else if(progress>=35) alpha = 130;
					else if(progress>=34) alpha = 105;
					else if(progress>=33) alpha = 80;
					else if(progress>=32) alpha = 55;
					else if(progress>=31) alpha = 30;
					else alpha = 0;

					break;
					
				case INDIVIDUALRELATION:
					
					if(progress>=90) alpha = 255;
					else if(progress>=85) alpha = 230;
					else if(progress>=82) alpha = 205;
					else if(progress>=79) alpha = 180;
					else if(progress>=76) alpha = 155;
					else if(progress>=73) alpha = 130;
					else if(progress>=70) alpha = 105;
					else if(progress>=67) alpha = 80;
					else if(progress>=64) alpha = 55;
					else if(progress>=61) alpha = 30;
					else alpha = 0;
					
					break;
					
				case INSTANZIATION:
					
					if(progress>=70) alpha = 0;
					else if(progress>=69) alpha = 30;
					else if(progress>=68) alpha = 55;
					else if(progress>=67) alpha = 80;
					else if(progress>=66) alpha = 105;
					else if(progress>=65) alpha = 130;
					else if(progress>=64) alpha = 155;
					else if(progress>=63) alpha = 180;
					else if(progress>=62) alpha = 205;
					else if(progress>=61) alpha = 230;
					else if(progress>=60) alpha = 255;
					
					else if(progress>=40) alpha = 255;
					else if(progress>=39) alpha = 230;
					else if(progress>=38) alpha = 205;
					else if(progress>=37) alpha = 180;
					else if(progress>=36) alpha = 155;
					else if(progress>=35) alpha = 130;
					else if(progress>=34) alpha = 105;
					else if(progress>=33) alpha = 80;
					else if(progress>=32) alpha = 55;
					else if(progress>=31) alpha = 30;
					else alpha = 0;
					
					break;
					
				case NONE:
					
					alpha = 255;
					
					break;
					
				default:
					break;
			
			}

			return alpha;
		}

		@SuppressLint("NewApi")
		private void toggleOntoPannel(boolean open, long duration)
		{
			float endX = 0;
			boolean doAnimation = false;
			
			if (open && !ontoPanelOpenState)
			{
				// open
				endX = 0;
				btnOntoPanelOpenClose.setImageDrawable(getResources().getDrawable(R.drawable.ic_onto_close));
				ontoPanelOpenState = true;
				
				doAnimation = true;
			}
			else if (ontoPanelOpenState)
			{
				// close
				endX = -365;
				btnOntoPanelOpenClose.setImageDrawable(getResources().getDrawable(R.drawable.ic_onto_open));
				ontoPanelOpenState = false;

				doAnimation = true;
			}
			
			if (doAnimation)
			{
				// If there's an animation in progress, cancel it immediately
				// and proceed with this one.
				if (mCurrentAnimator != null) 
				{
					mCurrentAnimator.cancel();
				}
				
				AnimatorSet set = new AnimatorSet();
				set.play(ObjectAnimator.ofFloat(overlay, View.X, endX));
				set.setDuration(duration);
				set.setInterpolator(new DecelerateInterpolator());
				set.addListener(new AnimatorListenerAdapter() 
				{
					@Override
					public void onAnimationEnd(Animator animation) 
					{
						mCurrentAnimator = null;
					}

					@Override
					public void onAnimationCancel(Animator animation) 
					{
						mCurrentAnimator = null;
					}
				});
				
				set.start();
				mCurrentAnimator = set;
			}
		}
		
		
		/**
		 * Initialize Options menu
		 * 
		 * @param menu
		 *            The Android options menu
		 * @return true if the menu was successfully created
		 */
		public boolean onCreateOptionsMenu(Menu menu)
		{

			MenuInflater inflater = getMenuInflater();
			
			inflater.inflate(R.menu.menu_activity_main , menu);
			
			return true;
		}
		
		@Override
		public boolean onPrepareOptionsMenu(Menu menu)
		{
		  
		  undoButton = menu.findItem(R.id.menu_undo);
		  setUndoButtonActive(false);
		  
		  deleteButton = menu.findItem(R.id.menu_delete);
		  setDeleteButtonActive(false);
		  
		  super.onPrepareOptionsMenu(menu);

		  return true;
		}
		
		/**
		 * manages undoButton
		 * @param bool
		 */
		public void setUndoButtonActive(boolean bool)
		{
			if(bool)
			{
				
				if(undoButton.isEnabled()) return;
				
				undoButton.setIcon(R.drawable.ic_content_undo_active);
				undoButton.setEnabled(true);
			}
			else 
			{
				if(!undoButton.isEnabled()) return;
				
				undoButton.setIcon(R.drawable.ic_content_undo_deactive);
				undoButton.setEnabled(false);
			}

		}
		
		/**
		 * manages deleteButton
		 * @param bool
		 */
		public void setDeleteButtonActive(boolean bool)
		{
			if(bool)
			{
				if(deleteButton.isEnabled()) return;
				
				deleteButton.setIcon(R.drawable.ic_content_delete_active);
				deleteButton.setEnabled(true);
			}
			else 
			{
				if(!deleteButton.isEnabled()) return;
				
				deleteButton.setIcon(R.drawable.ic_content_delete_deactive);
				deleteButton.setEnabled(false);
			}

		}

		/**
		 * Define the behavior of the menu buttons
		 * 
		 * @param item
		 *            The <b>MenuItem</b> that was selected
		 * @return true if an item was successfully selected
		 */
		public boolean onOptionsItemSelected(MenuItem item) 
		{

		    switch (item.getItemId()) 
		    {
		        case R.id.menu_delete:
		            drawView.deleteOrClear();
		            return true;
		            
		        case R.id.menu_export:
		        	showExportDialog();
		            return true;
		            
		        case R.id.menu_import:
		        	showLoadOntologyDialog();
		            return true;
		            
		        case R.id.menu_open:
				
				if (isUnsaved()) showUnsavedSessionDialog();
		        	else showLoadDialog();

		            return true;
		            
		        case R.id.menu_save:
		        	showSaveDialog();

		            return true;
		            
		        case R.id.menu_settings:

		            return true;
		            
		        case R.id.menu_undo:
		        	drawView.undo();
		            return true;
		            
		        default:
		            return super.onOptionsItemSelected(item);
		    }

		}



		/**
		 * shows the debug regions of all DrawingComponents
		 * @param view
		 */
		public void enableDebugRegions(View view) 
		{
		    if(btnDebugDraw.isChecked())
		    {
		    	drawView.drawDebugRegions = true;
		    }
		    else
		    {
		    	drawView.drawDebugRegions = false;
		    }
		    
		    drawView.invalidate();	    	
		}
		
		public void scrollToConcrete(View view) 
		{
		     scrollBar.setProgress(100);
		     btnCenter.setTextColor(getResources().getColor(R.color.black));
		}
		
		public void scrollToAbstract(View view) 
		{
		     scrollBar.setProgress(0);
		     btnCenter.setTextColor(getResources().getColor(R.color.black));
		}
		
		public void scrollToCenter(View view) 
		{
		     scrollBar.setProgress(50);
		     btnCenter.setTextColor(getResources().getColor(R.color.highlight));
		}
		
		public void searchInOntoPanel(View view) 
		{
			if (!searchInput.getText().toString().equalsIgnoreCase(""))
			{
				updateSearch("", searchInput.getText().toString());
				searchInput.setCursorVisible(false);
			}
		}

		/**
		 * Set the pixel format of the window in order to enhance the graphical
		 * quality
		 * 
		 * @see android.app.Activity#onAttachedToWindow()
		 */
		public void onAttachedToWindow() 
		{
			super.onAttachedToWindow();

			Window window = getWindow();

			window.setFormat(PixelFormat.RGBA_8888);
		}

		/**
		 * Method that is invoked after the save button was pressed and that
		 * initiates the storage of the temporary state of the application
		 * 
		 * @param filename
		 *            The filename that will be associated with the stored state
		 */
		private void storeData(String filename) 
		{
			DataStorageHelper.storeData(this, drawView, filename);
		}

		/**
		 * Method that is invoked after the load button was pressed and that
		 * initiates loading the previously stored state of the application
		 * 
		 * @param filename
		 *            The filename that will be associated with the stored state
		 */
		private void loadData(String filename) 
		{
			loadedOntoModel.removeAll();
			generalProperties.clear();
			properties.clear();
			
			propertyColors.clear();
			
			for (int i = 0; i <= 5; i++)
				fillColorList();
			
			sketchBoard.resetMode();
			sketchBoard.resetView();
			
			int count = ontopanelConceptList.getChildCount();
			int current = 0;
			
			for (int i = 0; i < count; i++)
			{
				if (ontopanelConceptList.getChildAt(current) instanceof ClassListItem || ontopanelConceptList.getChildAt(current) instanceof ListHeaderItem)
				{
					ontopanelConceptList.removeViewAt(current);
				}
				else
				{
					current++;
				}
				
			}
			
			count = ontopanelIndividualList.getChildCount();
			current = 0;
			
			for (int i = 0; i < count; i++)
			{
				if (ontopanelIndividualList.getChildAt(current) instanceof IndividualListItem || ontopanelIndividualList.getChildAt(current) instanceof ListHeaderItem)
				{
					ontopanelIndividualList.removeViewAt(current);
				}
				else
				{
					current++;
				}
				
			}
			
			count = ontopanelPropertyList.getChildCount();
			current = 0;
			
			for (int i = 0; i < count; i++)
			{
				if (ontopanelPropertyList.getChildAt(current) instanceof PropertyListItem || ontopanelPropertyList.getChildAt(current) instanceof ListHeaderItem)
				{
					ontopanelPropertyList.removeViewAt(current);
				}
				else
				{
					current++;
				}
				
			}
			
			ontopanelSearchConceptList.removeAllViews();
			ontopanelSearchIndividualList.removeAllViews();
			ontopanelSearchPropertyList.removeAllViews();
			
			TextView t1 = (TextView) findViewById(R.id.ontoPanelHintText1);
			TextView t2 = (TextView) findViewById(R.id.ontoPanelHintText2);
			TextView t3 = (TextView) findViewById(R.id.ontoPanelHintText3);
			
			t1.setVisibility(TextView.VISIBLE);
			t2.setVisibility(TextView.VISIBLE);
			t3.setVisibility(TextView.VISIBLE);
			
			loadOntology("", true);
			
			drawView.setDrawingObjects(DataStorageHelper.loadData(this, filename));

			ArrayList<String> temp = new ArrayList<String>();
			
			temp.addAll(ontologyFileNames);
			
			for (String s : temp)
			{
				loadOntology(s, false);
			}
			
			//update ListItems in Ontopanel
			for (DrawingComponent dc : drawView.getDrawingObjects().children)
			{
				dc.setOntResource(getOntologyResource(dc.getUri()));
				
				if (dc instanceof FormalizedObject)
				{
					if (dc instanceof FormalizedIndividual)
					{
						setOntopanelIndividualListItemInactive(dc.getUri());
					}
					else if (dc instanceof FormalizedConcept)
					{
						setOntopanelConceptListItemInactive(dc.getUri());
					}
				}
				
				if (dc instanceof FormalizedPropertyRelation)
				{
					setOntopanelPropertyListItemActive(dc.getUri(), true);
					
					dc.getPath().setColor(getPropertyRelationColor(dc.getUri()));
				}
				
			}
			
			//ensure ontres
			for (DrawingComponent dc : drawView.getDrawingObjects().children)
			{
				if (dc.getOntResource() == null)
				{
					if (dc instanceof DrawingConcept)
					{
						dc.setOntResource(loadedOntoModel.createClass(dc.getUri()));
						((DrawingConcept) dc).updateDrawingComponent();
					}
					else if (dc instanceof DrawingIndividual)
					{
						OntClass i = null;
						
						if (dc.hasInstanziation())
						{
							if (((InstatiationRelation)dc.getInstanziationRelation()).getStartElement().getOntResource() == null)
							{
								DrawingComponent dc2 = ((InstatiationRelation)dc.getInstanziationRelation()).getStartElement();
								
								if (dc2 instanceof DrawingConcept)
								{
									dc2.setOntResource(loadedOntoModel.createClass(dc2.getUri()));
									((DrawingConcept) dc2).updateDrawingComponent();
								}
							}
							
							Log.d("Main", "getStartElement " + ((InstatiationRelation)dc.getInstanziationRelation()).getStartElement());
							Log.d("Main", ".getOntResource() " + ((InstatiationRelation)dc.getInstanziationRelation()).getStartElement().getOntResource());
							Log.d("Main", ".asClass() " + ((InstatiationRelation)dc.getInstanziationRelation()).getStartElement().getOntResource().asClass());
							
							i = ((InstatiationRelation)dc.getInstanziationRelation()).getStartElement().getOntResource().asClass();
						}
						else
						{
							i = loadedOntoModel.getOntClass(getResources().getString(R.string.onto_namespace) + "PlainIndividual");
						}
						
						Individual individual = loadedOntoModel.createIndividual(dc.getUri(), i);
						
						dc.setOntResource(individual);
						
						((DrawingIndividual) dc).updateDrawingComponent();
					}
				}
			}
			
			for (int i = 0; i < GeneralView.drawingObjects.getPathList().getPaths().size(); i++) 
			{
				DrawingComponent dc = GeneralView.drawingObjects.getObjectByPathId(GeneralView.drawingObjects.getPathList().getPath(i).getUid());
				
				dc.setAlpha(calculateAlpha(dc, 50));
			}
			
			sketchBoard.invalidate();
		}

		/**
		 * Display error message in case of an invalid filename
		 */
		private void showStorageError() {
			Toast toast = Toast.makeText(this,
					"Invalid filename! The content could not be stored!",
					Toast.LENGTH_LONG);
			toast.show();
		}

		/**
		 * Method that is invoked after the save button in the save_state_dialog
		 * widget was pressed and initiates the storage of the temporary state of
		 * the application
		 */
		private void showSaveDialog() {

			final EditText input = new EditText(this);
			input.setSingleLine();
			input.setText("");

			AlertDialog.Builder saveDialogBuilder = new AlertDialog.Builder(this);

			saveDialogBuilder.setTitle("Save current working");

			saveDialogBuilder.setMessage("Enter filename");

			saveDialogBuilder.setView(input);

			saveDialogBuilder.setPositiveButton("Save",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

							boolean fileAlreadyExisting = false;

							String filename = input.getText().toString();

							for (String name : filenames) {
								if (name.compareTo(filename) == 0) {
									fileAlreadyExisting = true;
									break;
								}
							}

							if ((input.getText().toString().length() > 0 && input
									.getText().toString() != null)
									&& !fileAlreadyExisting)
							{
								storeData(filename);
								setUnsaved(false);
							}
							else if (fileAlreadyExisting) {

								showOverwriteFileDialog(filename);

							} else

								showStorageError();

						}
					});

			saveDialogBuilder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

						}
					});

			AlertDialog saveDialog = saveDialogBuilder.create();

			saveDialog.show();

		}
		
		private void showExportDialog() {

			final EditText input = new EditText(this);
			input.setSingleLine();
			input.setText("");

			AlertDialog.Builder exportDialogBuilder = new AlertDialog.Builder(this);

			exportDialogBuilder.setTitle("Export current working state as ontology");

			exportDialogBuilder.setMessage("Enter filename");

			exportDialogBuilder.setView(input);

			exportDialogBuilder.setPositiveButton("Export",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

							boolean fileAlreadyExisting = false;

							String filename = input.getText().toString();

							if ((input.getText().toString().length() > 0 && input
									.getText().toString() != null)
									&& !fileAlreadyExisting)

								exportOntology(filename);
						}
					});

			exportDialogBuilder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

						}
					});

			AlertDialog exportDialog = exportDialogBuilder.create();

			exportDialog.show();

		}


		/**
		 * Method that is invoked if the selected filename for storing the current
		 * application state is already existing
		 * @param filename The name of the file that should be stored but is already existing
		 */
		private void showOverwriteFileDialog(final String filename)
		{
			
			AlertDialog.Builder overwriteDialogBuilder = new AlertDialog.Builder(this);

			overwriteDialogBuilder.setTitle("Filename already existing!");

			overwriteDialogBuilder.setMessage("Do you want to overwrite the existing file?");
			
			overwriteDialogBuilder.setPositiveButton("Overwrite", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {

					//overwrite the existing file
					storeData(filename);
					
				}
			});
			
			overwriteDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
				
					//close the dialog
					
				}
			});
			
			AlertDialog overwriteDialog = overwriteDialogBuilder.create();
			
			overwriteDialog.show();
			
		}
		
		private void showUnsavedSessionDialog()
		{			
			AlertDialog.Builder overwriteDialogBuilder = new AlertDialog.Builder(this);

			overwriteDialogBuilder.setTitle("Unsaved session!");

			overwriteDialogBuilder.setMessage("Your current work is not saved. Do you want to overwrite it?");
			
			overwriteDialogBuilder.setPositiveButton("Overwrite and load stored session", new DialogInterface.OnClickListener()
			{	
				public void onClick(DialogInterface dialog, int which)
				{
					showLoadDialog();	
				}
			});
			
			overwriteDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int which)
				{
					//close the dialog
				}
			});
			
			AlertDialog overwriteDialog = overwriteDialogBuilder.create();
			
			overwriteDialog.show();
		}
		
		public void showRemoveOntologyDialog(final String filename)
		{
			
			AlertDialog.Builder removeOntologyDialogBuilder = new AlertDialog.Builder(this);

			removeOntologyDialogBuilder.setTitle("Remove ontology");

			removeOntologyDialogBuilder.setMessage("Do you want to remove the ontology from your session?");
			
			final Toast toast = Toast.makeText(this, "Sry, no function here yet..", Toast.LENGTH_LONG);
			
			removeOntologyDialogBuilder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {

					toast.show();
				}
			});
			
			removeOntologyDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
				
					//close the dialog
					
				}
			});
			
			AlertDialog removeDialog = removeOntologyDialogBuilder.create();
			
			removeDialog.show();
		}
		
		
		/**
		 * The file name that was selected in the load dialog
		 */
		public String selectedFileName;
		
		/**
		 * The name property of the contact that was selected in the import dialog 
		 */
		public String selectedContactName;
		
		/**
		 * Method that is invoked after the load button was pressed and initiates
		 * loading the previously stored state of the application
		 */
		private void showLoadDialog() {
			
			File dir = new File(getFilesDir().toString());
			
			File[] files = dir.listFiles();
			
			for(File f:files)
			{
				Long lastModified = f.lastModified(); 
			}

			filenames = getFilesDir().list();
			
			final Spinner listBox = new Spinner(this);

			ArrayList<String> elements = new ArrayList<String>();

			String[] listItems;

			for (String name : filenames) {
				if (!name.contains("_matrices") && !name.contains("_ontolist")) {
					
					elements.add(name);
					
				}
			}

			listItems = new String[elements.size()];

			Collections.sort(elements);
			
			for (int i = 0; i < elements.size(); i++) {

				listItems[i] = elements.get(i);

			}

			if(listItems.length > 0)
			selectedFileName = listItems[0];
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item , listItems);

			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			
			listBox.setAdapter(adapter);

			listBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent, View view,
						int pos, long id) {
					
					Object item = parent.getItemAtPosition(pos);
					
					selectedFileName = item.toString();
					
				}

				public void onNothingSelected(AdapterView<?> parent) {
				}
			});

			AlertDialog.Builder saveDialogBuilder = new AlertDialog.Builder(this);

			saveDialogBuilder.setTitle("Load stored working state");

			saveDialogBuilder.setMessage("Choose a file");

			saveDialogBuilder.setView(listBox);

			saveDialogBuilder.setPositiveButton("Load",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

							loadData(selectedFileName);
							
						}
					});

			saveDialogBuilder.setNeutralButton("Delete",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

							deleteData(selectedFileName);
							
						}
					});
			
			saveDialogBuilder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

						}
					});

			AlertDialog saveDialog = saveDialogBuilder.create();

			saveDialog.show();

		}

	protected void deleteData(String fileName)
	{

		boolean deleted = false;
		
		deleted = DataStorageHelper.deleteData(this, fileName);
		
		if(deleted)
		{
			
			Toast toast = Toast.makeText(this,
					"The file has been deleted successfully!",
					Toast.LENGTH_LONG);
			toast.show();
			
		}
		else
		{
			Toast toast = Toast.makeText(this,
					"An error has occurred during deletion! Please try again!",
					Toast.LENGTH_LONG);
			toast.show();
		}
			
	}
	
	private ArrayList<String> objectProperties = new ArrayList<String>();

		private void exportOntology(String filename) 
		{

			File root = Environment.getExternalStorageDirectory();

            SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd_hh-mm-ss");
            String format = s.format(new Date());

            File file = new File(getResources().getString(R.string.export_ontology_folder) + "/" + format + "_" + filename + ".owl");  

            FileWriter fw = null;
            
			try 
			{
				fw = new FileWriter(file);
			} 
			catch (IOException e1)
			{
				e1.printStackTrace();
			}

			//loadedOntoModel.write(System.out, "RDF/XML");
			
			ArrayList<String> namespaces = new ArrayList<String>();
			
			objectProperties = new ArrayList<String>();
			
			Log.d("MainActivity","export ================================================");
			
			StringBuffer export = new StringBuffer();
			
			export.append("<rdf:RDF\n");
			
			export.append("\txmlns=\""+ getResources().getString(R.string.onto_namespace) + "\"\n");
			export.append("\txml:base=\""+ getResources().getString(R.string.onto_namespace) + "\"\n");
			export.append("\txmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n");		
			export.append("\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n");
			export.append("\txmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n");

			export.append(">\n\n");
			
		    export.append("<owl:Ontology rdf:about=\"" + getResources().getString(R.string.onto_namespace) + "\">\n");
		    	    
		    export.append("###Namespaces###");
		   
	        export.append("\n</owl:Ontology>\n\n");
			
			for (DrawingComponent dc : sketchBoard.drawingObjects.children)
			{
				if (dc instanceof DrawingConcept)
				{
					OntClass oClass = dc.getOntResource().asClass();
					
//					String localName = "";
//					if (((DrawingConcept) dc).getComponentChild() != null) localName = ((DrawingConcept) dc).getComponentChild().getResult();
					
					export.append(generateOwlXmlClass(oClass));
					
					
					if (oClass.hasSuperClass() && !oClass.equals(oClass.getSuperClass())) 
					{
						String uri = oClass.getSuperClass().getURI();
						
						Log.d("lll",uri + "  " + oClass.getSuperClass().getNameSpace());
						
						String ns = oClass.getSuperClass().getNameSpace(); //""
								
//						if (uri.contains("#")) uri.substring(0, uri.indexOf("#"));
//						else uri.substring(0, uri.indexOf());
						
						Log.d("lll",ns);
						
						if (!namespaces.contains(ns)) namespaces.add(ns);
					}

				} 
				else if (dc instanceof DrawingIndividual)
				{
					Individual individual = dc.getOntResource().asIndividual();
					
//					String localName = "";
//					if (((DrawingIndividual) dc).getComponentChild() != null) localName = ((DrawingIndividual) dc).getComponentChild().getResult();

					export.append(generateOwlXmlIndividual(individual, dc.getRelations()));
				}
			}

			
			
			for (String op : objectProperties)
			{
				export.append("\t<!-- " +  getResources().getString(R.string.onto_namespace)  + op + " --> \n\n");
				
				export.append("\t<owl:ObjectProperty rdf:about=\""  +  op + "\" /> \n\n"); //+ getResources().getString(R.string.onto_namespace)

//				    <owl:ObjectProperty rdf:about="http://www.comvantage.eu/garment/hasStripeMaterial">
//				        <rdfs:range rdf:resource="http://www.comvantage.eu/garment/Material"/>
//				        <rdfs:domain rdf:resource="http://www.comvantage.eu/garment/ShirtDesign"/>
//				        <rdfs:subPropertyOf rdf:resource="http://www.comvantage.eu/garment/hasMaterial"/>
//				        <rdfs:subPropertyOf rdf:resource="http://www.comvantage.eu/garment/hasStripeMaterial"/>
//				    </owl:ObjectProperty>
			}
			

			export.append("</rdf:RDF>\n\n");
			
			export.append("<!-- Generated by OntoSketch " + format +  " -->");
			
			String output = export.toString();
			
			StringBuffer replaceNS = new StringBuffer();
			
			for (String ns : namespaces)
			{
				replaceNS.append("\t<owl:imports rdf:resource=\"" + ns + "\"/>\n");
				
				Log.d("lllappend",ns);
				
			}
			
			Log.d("lllrepl", replaceNS.toString());
			
			output = output.replace("###Namespaces###", replaceNS.toString());
			
			int countConcept = 0;
			int countIndividual = 0;
			int countRelation = 0;
			
			for (DrawingComponent dc : sketchBoard.drawingObjects.children)
			{
				if (!dc.getItemText().equalsIgnoreCase(""))
					output = output.replace(dc.getPath().getUid().toString(), dc.getItemText());
				else
				{

					if (dc instanceof DrawingConcept)
					{
						output = output.replace(dc.getPath().getUid().toString(), "EMPTY_CONCEPT_" + countConcept);
						countConcept++;
					}
						
					else if (dc instanceof DrawingIndividual)
					{
						output = output.replace(dc.getPath().getUid().toString(), "EMPTY_INDIVIDUAL_" + countIndividual);
						countIndividual++;
					}
						
					else if (dc instanceof DrawingPropertyRelation)
					{
						output = output.replace(dc.getPath().getUid().toString(), "EMPTY_RELATION_" + countRelation);
						countRelation++;
					}	
					else if (dc instanceof FormalizedPropertyRelation)
					{
						output = output.replace(dc.getPath().getUid().toString(), ((FormalizedPropertyRelation) dc).getUri());
					}
					
					else 
						output = output.replace(dc.getPath().getUid().toString(), dc.toString());
				}
			}

			try
			{
				fw.write(output);
				fw.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}

			Toast toast = Toast.makeText(this, "Ontology was successfully exported", Toast.LENGTH_LONG);
			
			toast.show();

		}
		
		private String generateOwlXmlClass(OntClass oClass)
		{
			StringBuffer sb = new StringBuffer();
			
			sb.append("\t<!-- " + oClass.getURI() + " --> \n\n");
			
			sb.append("\t<owl:Class rdf:about=\"" + oClass.getURI() + "\">\n");

			if (oClass.hasSuperClass() 
					&& !oClass.equals(oClass.getSuperClass())) sb.append("\t\t<rdfs:subClassOf rdf:resource=\"" + oClass.getSuperClass().getURI() + "\"/>\n");

			sb.append("\t</owl:Class>\n\n");
			
			return sb.toString();
		}
		
		
		
		private String generateOwlXmlIndividual(Individual individual, List<DrawingComponent> relations)
		{
			
			StringBuffer sb = new StringBuffer();
			
			sb.append("\t<!-- " + individual.getURI() + " --> \n\n");
			
			sb.append("\t<owl:NamedIndividual rdf:about=\"" + individual.getURI() + "\">\n");
			
			StringBuffer relationSb = new StringBuffer();
			
			for (DrawingComponent dc : relations)
			{
				if (dc instanceof InstatiationRelation)
				{
					sb.append("\t\t<rdf:type rdf:resource=\"" + ((CustomObjectRelation)dc).getStartElement().getUri() + "\"/>\n");
				}
				else
				{
					if (dc instanceof DrawingPropertyRelation)
					{
						String rel = ((DrawingPropertyRelation)dc).getItemText().replace(" ", "");
						
						if (rel.equalsIgnoreCase("")) rel = dc.getPath().getUid().toString();
						
						if (((DrawingPropertyRelation)dc).getStartElement().getUri().equalsIgnoreCase(individual.getURI()))
						{
							String uri = ((DrawingPropertyRelation)dc).getEndElement().getUri();
							
							relationSb.append("\t\t<" + rel + " rdf:resource=\""+ uri + "\" />\n");
							
							if (!objectProperties.contains(rel)) objectProperties.add(rel);
						}
					}
					else
					{
						String rel = ((CustomObjectRelation)dc).getItemText().replace(" ", "");
						
						if (rel.equalsIgnoreCase("")) rel = dc.getPath().getUid().toString();
						
						if (((CustomObjectRelation)dc).getEndElement().getUri().equalsIgnoreCase(individual.getURI())) relationSb.append("\t\t<" + rel + " rdf:resource=\""+((CustomObjectRelation)dc).getEndElement().getUri() + "\" />\n");							
					}
					
				}
			}
			
			sb.append(relationSb);

			sb.append("\t</owl:NamedIndividual>\n\n");
			
			return sb.toString();
		}
		
		private void showLoadOntologyDialog() 
		{

			File dir = new File(getResources().getString(R.string.import_ontology_folder));

			File[] files = dir.listFiles();

			final Spinner listBox = new Spinner(this);

			ArrayList<String> elements = new ArrayList<String>();

			String[] listItems;
			
			for (int i= 0; i<files.length; i++)
			{
				File f = files[i];
				
				if (f.getName().contains(".owl") || f.getName().contains(".rdf")) 
					elements.add(f.getName());

			}

			//TODO: configure order of list elements here			
			listItems = new String[elements.size()];

			Collections.sort(elements);
			
			for (int i = 0; i < elements.size(); i++) {

				listItems[i] = elements.get(i);

			}

			if(listItems.length > 0)
			selectedFileName = listItems[0];
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item , listItems);

			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			
			listBox.setAdapter(adapter);

			listBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
			{
				public void onItemSelected(AdapterView<?> parent, View view,
						int pos, long id) 
				{
					
					Object item = parent.getItemAtPosition(pos);
					
					selectedFileName = item.toString();

				}

				public void onNothingSelected(AdapterView<?> parent) 
				{
				}
			});

			AlertDialog.Builder saveDialogBuilder = new AlertDialog.Builder(this);

			saveDialogBuilder.setTitle("Import ontology into ontology panel");

			saveDialogBuilder.setMessage("Choose a file");

			saveDialogBuilder.setView(listBox);

			saveDialogBuilder.setPositiveButton("Load",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

							if (selectedFileName != null) loadOntology(selectedFileName, false);
							
						}
					});

			saveDialogBuilder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

						}
					});

			AlertDialog saveDialog = saveDialogBuilder.create();

			saveDialog.show();

		}
		
		private void loadOntology(String filepath, boolean first)
		{
			showProgressLayer();
			
			InputStream is = null;

			File file = new File(getResources().getString(R.string.import_ontology_folder)+ "/"+ filepath);
			
			if (!filepath.equalsIgnoreCase("")) ontologyFileNames.add(filepath);
			
			OntModel tempModel = null;
			
			if (first)
			{
				loadedOntoModel = ModelFactory.createOntologyModel();
				generalProperties = loadedOntoModel.listOntProperties().toList();
				return;
			}
			else 
			{
				TextView t1 = (TextView) findViewById(R.id.ontoPanelHintText1);
				TextView t2 = (TextView) findViewById(R.id.ontoPanelHintText2);
				TextView t3 = (TextView) findViewById(R.id.ontoPanelHintText3);
				
				t1.setVisibility(TextView.GONE);
				t2.setVisibility(TextView.GONE);
				t3.setVisibility(TextView.GONE);
			}
			
			try 
			{   
			     tempModel = ModelFactory.createOntologyModel();
			     is = new BufferedInputStream(new FileInputStream(file));
			    
			     tempModel.read(is, getResources().getString(R.string.onto_namespace));
			}
			catch (Exception fnf) 
			{
				System.out.println("The file was not found!!\n");
			}
			
			if (tempModel != null)
			{
				loadedOntoModel.addSubModel(tempModel);

				ListHeaderItem lhiConcepts = new ListHeaderItem(this, filepath);
				
				ontopanelConceptList.addView(lhiConcepts);

				ArrayList<OntClass> oClasslist = (ArrayList<OntClass>) tempModel.listHierarchyRootClasses().toList();
				ArrayList<Individual> indList = new ArrayList<Individual>();
				
				if (oClasslist != null)
					Collections.sort(oClasslist, new Comparator<OntClass>()
							{
								@Override
						        public int compare(OntClass o1, OntClass o2)
								{
						            if (o1.getLocalName() == null || o2.getLocalName() == null) return 0;
						            else return o1.getLocalName().compareToIgnoreCase(o2.getLocalName());
								}
							}
					);

				for (OntClass o : oClasslist)
				{
					if (o.getURI() != null)
					{
						ClassListItem cll = new ClassListItem(this, o.getLocalName(), 0);
		
						cll.setUri(o.getURI());
						
						ontopanelConceptList.addView(cll);
						
						lhiConcepts.addChildren(cll);

						List<Individual> temp = sortIndividuals(o.listInstances(true).toList());
						
						if (temp.size() > 0) 
						{
							cll.setHasIndividuals(true);
							
							indList.addAll(temp);
						}
						
						if (o.hasSubClass()) indList.addAll(addSubClasses(o, ontopanelConceptList, 1, lhiConcepts, cll));
						
						cll.checkChildCount();
					}
				}

				ListHeaderItem lhiIndividuals = new ListHeaderItem(this,filepath);
				
				ontopanelIndividualList.addView(lhiIndividuals);
				
				indList = (ArrayList<Individual>) sortIndividuals(indList);

				for (Individual item : indList) 
				{
					individuals.add(item);
					
					IndividualListItem ill = new IndividualListItem(this, item.getLocalName(), item.getOntClass().getLocalName());

					ill.setUri(item.getURI());

					ontopanelIndividualList.addView(ill);
					
					lhiIndividuals.addChildren(ill);
				}
				
				ArrayList<OntProperty> pl = (ArrayList<OntProperty>) tempModel.listOntProperties().toList();
				
				Collections.sort(pl, new Comparator<OntProperty>()
						{
							@Override
					        public int compare(OntProperty o1, OntProperty o2)
							{
								 if (o1.getLocalName() == null || o2.getLocalName() == null) return 0;
					            return o1.getLocalName().compareToIgnoreCase(o2.getLocalName());
							}
						}
				);
				
				ListHeaderItem lhiRelations = new ListHeaderItem(this,filepath);
				
				ontopanelPropertyList.addView(lhiRelations);
				
				for (OntProperty item : pl) 
				{
					if (!isGeneralProperty(item))
					{
						properties.add(item);
						
						PropertyListItem pll = new PropertyListItem(this, item.getLocalName(), getNextFreeColor(item.getURI()));
						
						pll.setUri(item.getURI());
						pll.setCurrentState(ListItemState.INACTIVE);
								
						ontopanelPropertyList.addView(pll);
						
						lhiRelations.addChildren(pll);
					}
					
				}
			}
			
			tempModel = null;
			
			if (!ontoPanelOpenState) toggleOntoPannel(true, mShortAnimationDuration);

			hideProgressLayer();
			
		}
		
		private List<Individual> sortIndividuals(List<? extends OntResource> list) 
		{
			ArrayList<Individual> toSort = (ArrayList<Individual>) list;
			
			Collections.sort(toSort, new Comparator<Individual>() 
					{
						@Override
				        public int compare(Individual i1, Individual i2)
						{
				            return i1.getLocalName().compareToIgnoreCase(i2.getLocalName());
						}
					}
					);
			
			return toSort;	
		}

		public int getNextFreeColor(String uri)
		{
			int color = 0;
			
			for (int i = 0; i < ontopanelPropertyList.getChildCount(); i++)
			{
				if (ontopanelPropertyList.getChildAt(i) instanceof PropertyListItem)
				{
					PropertyListItem li = (PropertyListItem) ontopanelPropertyList.getChildAt(i);

					if (li.getUri().equalsIgnoreCase(uri)) 
					{
						return li.getColor();
					}
				}
				
			}

			if (propertyColors.isEmpty())
			{
				fillColorList();
				color = propertyColors.remove(0).intValue();
			}
			else 
			{
				color = propertyColors.remove(0).intValue();
			}
		
			return color;
		}

		
		private ArrayList<Individual> addSubClasses(OntClass oClass, LinearLayout layout, int hLevel, ListHeaderItem lhi, ClassListItem cll2)
		{
			ArrayList<OntClass> list = (ArrayList<OntClass>) oClass.listSubClasses(true).toList();
			ArrayList<Individual> newIdividuals = new ArrayList<Individual>();

			Collections.sort(list, new Comparator<OntClass>()
					{
						@Override
				        public int compare(OntClass o1, OntClass o2)
						{
				            return o1.getLocalName().compareToIgnoreCase(o2.getLocalName());
						}
					}
			);
			
			int h = hLevel+1;
			
			for (OntClass o : list) 
			{
				if (o.getURI() != null)
				{
					ClassListItem cll = new ClassListItem(this, o.getLocalName(), hLevel);
	
					cll.setUri(o.getURI());
					
					layout.addView(cll);
					
					lhi.addChildren(cll);
					
					cll2.addChildren(cll);
					
					List<Individual> temp = sortIndividuals(o.listInstances(true).toList());
					
					if (temp.size() > 0) 
					{
						cll.setHasIndividuals(true);
						
						newIdividuals.addAll(temp);
					}

					if (o.hasSubClass())
					{
						newIdividuals.addAll(addSubClasses(o, layout, h, lhi, cll));
					}
				}
			}
			
			cll2.checkChildCount();
			
			return newIdividuals;
		}
		
		private boolean isGeneralProperty(OntProperty op)
		{
			if (generalProperties != null)
			{
				for (OntProperty p : generalProperties)
				{
					if(p.getLocalName().equalsIgnoreCase(op.getLocalName()))
						
						return true;
				}
			}

			return false;
		}

		public VerticalSeekBar getScrollBar()
		{
			return scrollBar;
		}

		public void updateScrollBar(GestureTypes type)
		{
			switch (type)
			{

				case CONCEPT:
					if (scrollBar.getProgress() > 50) scrollBar.setProgress(50);
					break;

				case INDIVIDUAL:
					if (scrollBar.getProgress() < 50) scrollBar.setProgress(50);
					break;

				case NOGESTURE:
					break;

				default:
					break;
			
			}
			
		}

		public OntModel getOntoModel()
		{
			return loadedOntoModel;
		}

		public String getOntologyObjectName(String uri) 
		{
			return loadedOntoModel.getOntResource(uri).getLocalName().toString();
		}

		public static OntResource getOntologyResource(String uri)
		{
			return loadedOntoModel.getOntResource(uri);
		}

		public void setOntopanelConceptListItemActive(String uri)
		{
			for (int i = 0; i < ontopanelConceptList.getChildCount(); i++)
			{
				if (ontopanelConceptList.getChildAt(i) instanceof ListItem)
				{
					ListItem li = (ListItem) ontopanelConceptList.getChildAt(i);

					if (li.getUri().equalsIgnoreCase(uri)) li.setCurrentState(ListItemState.ACTIVE); 
				}
			}
			
			for (int i = 0; i < ontopanelSearchConceptList.getChildCount(); i++)
			{
				if (ontopanelSearchConceptList.getChildAt(i) instanceof ListItem)
				{
					ListItem li = (ListItem) ontopanelSearchConceptList.getChildAt(i);

					if (li.getUri().equalsIgnoreCase(uri)) li.setCurrentState(ListItemState.ACTIVE); 
				}
			}
		}
		
		public void setOntopanelConceptListItemInactive(String uri)
		{
			for (int i = 0; i < ontopanelConceptList.getChildCount(); i++)
			{
				if (ontopanelConceptList.getChildAt(i) instanceof ListItem)
				{
					ListItem li = (ListItem) ontopanelConceptList.getChildAt(i);

					if (li.getUri().equalsIgnoreCase(uri)) li.setCurrentState(ListItemState.INACTIVE); 
				}
			}
			
			for (int i = 0; i < ontopanelSearchConceptList.getChildCount(); i++)
			{
				if (ontopanelSearchConceptList.getChildAt(i) instanceof ListItem)
				{
					ListItem li = (ListItem) ontopanelSearchConceptList.getChildAt(i);

					if (li.getUri().equalsIgnoreCase(uri)) li.setCurrentState(ListItemState.INACTIVE); 
				}
			}
		}
		
		public void setOntopanelPropertyListItemActive(String uri, boolean active)
		{
			for (int i = 0; i < ontopanelPropertyList.getChildCount(); i++)
			{
				if (ontopanelPropertyList.getChildAt(i) instanceof PropertyListItem)
				{
					PropertyListItem li = (PropertyListItem) ontopanelPropertyList.getChildAt(i);

					if (li.getUri().equalsIgnoreCase(uri)) 
					{
						if (active) li.setCurrentState(ListItemState.ACTIVE);
						else li.setCurrentState(ListItemState.INACTIVE);
					}
				}
			}
			
			for (int i = 0; i < ontopanelSearchPropertyList.getChildCount(); i++)
			{
				if (ontopanelSearchPropertyList.getChildAt(i) instanceof PropertyListItem)
				{
					PropertyListItem li = (PropertyListItem) ontopanelSearchPropertyList.getChildAt(i);

					if (li.getUri().equalsIgnoreCase(uri)) 
					{
						if (active) li.setCurrentState(ListItemState.ACTIVE);
						else li.setCurrentState(ListItemState.INACTIVE);
					}
				}
				
			}

		}
		
		public void setOntopanelIndividualListItemActive(String uri)
		{
			for (int i = 0; i < ontopanelIndividualList.getChildCount(); i++)
			{
				if (ontopanelIndividualList.getChildAt(i) instanceof ListItem)
				{
					ListItem li = (ListItem) ontopanelIndividualList.getChildAt(i);

					if (li.getUri().equalsIgnoreCase(uri)) li.setCurrentState(ListItemState.ACTIVE); 
				}
			}
			
			for (int i = 0; i < ontopanelSearchIndividualList.getChildCount(); i++)
			{
				if (ontopanelSearchIndividualList.getChildAt(i) instanceof ListItem)
				{
					ListItem li = (ListItem) ontopanelSearchIndividualList.getChildAt(i);

					if (li.getUri().equalsIgnoreCase(uri)) li.setCurrentState(ListItemState.ACTIVE); 
				}
			}
		}
		
		public void setOntopanelIndividualListItemInactive(String uri)
		{
			for (int i = 0; i < ontopanelIndividualList.getChildCount(); i++)
			{
				if (ontopanelIndividualList.getChildAt(i) instanceof ListItem)
				{
					ListItem li = (ListItem) ontopanelIndividualList.getChildAt(i);

					if (li.getUri().equalsIgnoreCase(uri)) li.setCurrentState(ListItemState.INACTIVE); 
				}
				
			}
			
			for (int i = 0; i < ontopanelSearchIndividualList.getChildCount(); i++)
			{
				if (ontopanelSearchIndividualList.getChildAt(i) instanceof ListItem)
				{
					ListItem li = (ListItem) ontopanelSearchIndividualList.getChildAt(i);

					if (li.getUri().equalsIgnoreCase(uri)) li.setCurrentState(ListItemState.INACTIVE); 
				}
			}
		}
		
		public void findObjectsPropertyRelations(String objectUri, DrawingComponent endComponent)
		{
			Individual object = loadedOntoModel.getIndividual(objectUri);
			
			for (Individual tempIndividual : individuals)
			{

				StmtIterator si = tempIndividual.listProperties();

				while(si.hasNext())
				{
					Statement s = si.next();
					
					Triple t = s.asTriple();
					
					if (tempIndividual.hasProperty(getProperty(t.getPredicate().getURI())))
					{
						try
						{
							if (t.getObject().getURI().equalsIgnoreCase(object.getURI()) && getPropertyRelationColor(t.getPredicate().getURI()) != 0)
							{
								DrawingComponent startComponent = sketchBoard.getDrawingComponent(t.getSubject().getURI());

								if (startComponent != null)
								{
									String uri = t.getPredicate().getURI();
									
									int color = getPropertyRelationColor(uri);
									
									if (color != 0) setOntopanelPropertyListItemActive(uri, true);
	
									if (isPropertyListItemActive(uri))
									{
										sketchBoard.addPropertyRelation(startComponent, endComponent, color, uri, false, true, t.getPredicate().getLocalName());
									}
									
								}
							}
						}
						catch (Exception e)
						{
							System.out.print(e.toString());
						}
					}

				}
				
			}
		}
		
		public boolean isPropertyListItemActive(String uri)
		{
			for (int i = 0; i < ontopanelPropertyList.getChildCount(); i++)
			{
				if (ontopanelPropertyList.getChildAt(i) instanceof PropertyListItem)
				{
					PropertyListItem li = (PropertyListItem) ontopanelPropertyList.getChildAt(i);

					if (li.getUri().equalsIgnoreCase(uri)) 
					{
						switch(li.getCurrentState())
						{
							case ACTIVE:
								return true;
							case INACTIVE:
									return false;
							case NONE:
									break;
								default:
									break;
						}
					}
				}	
			}
			
			return false;
		}

		public Property getProperty(String uri)
		{
			for (Property p : properties)
			{
				if (p.getURI().equalsIgnoreCase(uri)) return p;
			}
			
			return null;
		}
		
		public LinkedList<Individual> getIndividuals() {
			return individuals;
		}

		public void setIndividuals(LinkedList<Individual> individuals) {
			this.individuals = individuals;
		}

		public void showPropertyRelation(String uri, int color) 
		{
			OntProperty op = getOntologyResource(uri).asProperty();
			
			Log.d("MainActivity", "op: " + op + "  " + uri);
			
			OntResource range = null;
			if (op.getRange() != null) range = op.getRange().asClass();
			
			OntResource domain = null;
			if (op.getDomain() != null) domain= op.getDomain().asClass();
			
			DrawingComponent drawingDomain = null;
			DrawingComponent drawingRange = null;
			
			if (range != null && domain != null)
			{
				for (DrawingComponent dc : GeneralView.drawingObjects.children)
				{
					if (dc.getUri().equalsIgnoreCase(domain.getURI()))
					{
						drawingDomain = dc;
					}
					
					if (dc.getUri().equalsIgnoreCase(range.getURI()))
					{
						drawingRange = dc;
					}
				}
				
				if (drawingDomain != null && drawingRange != null)
				{
					sketchBoard.addPropertyRelation(drawingDomain, drawingRange, color, op.getURI(), true, true, op.getLocalName());
					scrollBar.setProgress(0);
				}
			}
		}

		public void hidePropertyRelation(String uri) 
		{
			ArrayList<DrawingComponent> toDelete = new ArrayList<DrawingComponent>();
			
			for (DrawingComponent dc : sketchBoard.drawingObjects.children)
			{
				if (dc.getUri().equalsIgnoreCase(uri))
				{
					if (dc instanceof CustomObjectRelation)
					{
						if ((((CustomObjectRelation) dc).getStartElement() instanceof FormalizedObject)
								&& (((CustomObjectRelation) dc).getEndElement() instanceof FormalizedObject)
							)
						{
							((CustomObjectRelation) dc).removeReferences();
							
							toDelete.add(dc);
						}
					}
					else
					{
						toDelete.add(dc);
					}

				}
			}

			for (DrawingComponent dc : toDelete)
			{
				GeneralView.drawingObjects.removeComponent(dc);
			}

			sketchBoard.invalidate();
		}

		public int getPropertyRelationColor(String uri)
		{
			int color = 0;
			
			for (int i = 0; i < ontopanelPropertyList.getChildCount(); i++)
			{
				if (ontopanelPropertyList.getChildAt(i) instanceof PropertyListItem)
				{
					PropertyListItem li = (PropertyListItem) ontopanelPropertyList.getChildAt(i);

					if (li.getUri().equalsIgnoreCase(uri)) 
					{
						color = li.getColor();
					}
				}
				
			}
			return color;
		}

		public static LinkedList<OntProperty> getProperties()
		{
			return properties;
		}

		public static int getNextFreeColor()
		{
			if (!propertyColors.isEmpty()) return propertyColors.remove(0).intValue();
			else  return 0;
		}

		public void activateScrollBarPanel(boolean b)
		{
			scrollBar.setEnabled(b);
			
			btnAbstract.setEnabled(b);
		    btnCenter.setEnabled(b);
		    btnConcrete.setEnabled(b);
		    
		    if (b)
		    {
		    	btnAbstract.setTextColor(getResources().getColor(R.color.concept));
			    btnCenter.setTextColor(getResources().getColor(R.color.black));
			    btnConcrete.setTextColor(getResources().getColor(R.color.individual));
		    }
		    else
		    {
		    	btnAbstract.setTextColor(getResources().getColor(R.color.tabBtnBackground));
			    btnCenter.setTextColor(getResources().getColor(R.color.tabBtnBackground));
			    btnConcrete.setTextColor(getResources().getColor(R.color.tabBtnBackground));
		    }
		}

		public void selectiveUpdateOntoPanel(OntologyObjectTypes fromTab, OntologyObjectTypes gotoTab, String uri, String localName) 
		{
			ontopanelSearchConceptList.removeAllViews();
			ontopanelSearchIndividualList.removeAllViews();
			ontopanelSearchPropertyList.removeAllViews();
			
			OntResource oRes = getOntologyResource(uri);
			
			Individual individual = null;
			OntClass oClass = null;
			OntProperty property = null;
			
			switch(gotoTab)
			{
				case FORMALIZEDCONCEPT:
					tabHost.setCurrentTab(0);
					break;
				case FORMALIZEDINDIVIDUAL:
					tabHost.setCurrentTab(1);
					break;
				case FORMALIZEDPROPERTYRELATION:
					tabHost.setCurrentTab(2);
					break;
				default:
					break;			
			}
			
			switch(fromTab)
			{
				case FORMALIZEDCONCEPT:
					oClass = oRes.asClass();
					break;
				case FORMALIZEDINDIVIDUAL:
					individual = oRes.asIndividual();
					break;
				case FORMALIZEDPROPERTYRELATION:
					property = oRes.asProperty();
					break;
				default:
					break;			
			}

			if(oClass != null)
			{
				ArrayList<ClassListItem> childs = addSuperConceptsToView(oClass, true, 0);

				int lastIndex = childs.size()-1;
				if (lastIndex < 0) lastIndex= 0;
				
				ClassListItem cli2 = childs.get(lastIndex);
				
				if (cli2 != null)
				{
					childs.addAll(addSubClasses(oClass, cli2.gethLevel(), cli2));
					
					cli2.checkChildCount();
				}

				for (ClassListItem cli : childs)
				{
					ontopanelSearchConceptList.addView(cli);
				}
				
				addIndividualsToView(oClass, ontopanelSearchIndividualList);
				addPropertiesToView(oClass, ontopanelSearchPropertyList);
			}
			else if (individual != null)
			{
				ArrayList<ClassListItem> childs = addSuperConceptsToView(individual.getOntClass(), true, 0);

				int lastIndex = childs.size()-1;
				if (lastIndex < 0) lastIndex= 0;
				
				ClassListItem cli2 = childs.get(lastIndex);
				
				if (cli2 != null)
				{
					childs.addAll(addSubClasses(individual.getOntClass(), cli2.gethLevel(), cli2));
					
					cli2.checkChildCount();
				}
				
				for (ClassListItem cli : childs)
				{
					ontopanelSearchConceptList.addView(cli);
				}
				
				addIndividualsToView(individual.getOntClass(), ontopanelSearchIndividualList);
				addPropertiesToView(individual.getOntClass(), ontopanelSearchPropertyList);
			}
			else if (property !=null)
			{
				ArrayList<ClassListItem> childs = addSuperConceptsToView(property.getDomain().asClass(), true, 0);
				
				int lastIndex = childs.size()-1;
				if (lastIndex < 0) lastIndex= 0;
				
				ClassListItem cli2 = childs.get(lastIndex);
				
				if (cli2 != null)
				{
					childs.addAll(addSubClasses(property.getDomain().asClass(), cli2.gethLevel(), cli2));
					
					cli2.checkChildCount();
				}
				
				for (ClassListItem cli : childs)
				{
					ontopanelSearchConceptList.addView(cli);
				}
				
				addIndividualsToView(property.getDomain().asClass(), ontopanelSearchIndividualList);
				addPropertiesToView(property.getDomain().asClass(), ontopanelSearchPropertyList);
			}
			
			if (ontopanelSearchConceptList.getChildCount() <= 0)
			{
				ontopanelSearchConceptList.addView(getEmptyResultHint("Concepts", oRes.getLocalName()));
			}
			
			if (ontopanelSearchIndividualList.getChildCount() <= 0)
			{
				ontopanelSearchIndividualList.addView(getEmptyResultHint("Individuals", oRes.getLocalName()));
			}
			
			
			if (ontopanelSearchPropertyList.getChildCount() <= 0)
			{
				ontopanelSearchPropertyList.addView(getEmptyResultHint("Properties", oRes.getLocalName()));
			}
			
			updateSearch(uri, oRes.getLocalName());
		}
		
		private void addPropertiesToView(OntClass oClass, LinearLayout container) 
		{
			for (OntProperty p : properties)
			{
				OntResource range = null;
				if (p.getRange() != null) range = p.getRange().asClass();
				
				OntResource domain = null;
				if (p.getDomain() != null) domain= p.getDomain().asClass();
				
				String pDuri = "";
				String pRUri = "";

				if (domain != null) pDuri = domain.getURI();
				if (range != null) pRUri = range.getURI();
				
				if (pDuri.equalsIgnoreCase(oClass.getURI())
						|| pRUri.equalsIgnoreCase(oClass.getURI()))
				{
					PropertyListItem pll = new PropertyListItem(this, p.getLocalName(), getNextFreeColor(p.getURI()));
					
					pll.setUri(p.getURI());
					
					if (isPropertyListItemActive(p.getURI()))
						pll.setCurrentState(ListItemState.ACTIVE);
					else
						pll.setCurrentState(ListItemState.INACTIVE);
				
					container.addView(pll);
				}
			}
			
		}

		private void addIndividualsToView(OntClass oClass, LinearLayout cotainer)
		{
			List<Individual> temp = sortIndividuals(oClass.listInstances(true).toList());
			
			if (temp.size() > 0) 
			{
				for (Individual individual : temp)
				{
					IndividualListItem ill = new IndividualListItem(this, individual.getLocalName(), individual.getOntClass().getLocalName());

					ill.setUri(individual.getURI());

					cotainer.addView(ill);
				}
			}	
		}

		private ArrayList<ClassListItem> addSuperConceptsToView(OntClass oClass, boolean up, int hlevel)
		{
			ArrayList<ClassListItem> tempArray = new ArrayList<ClassListItem>();
			
			if (oClass != null)
			{
				if (up)
				{
					if (oClass.hasSuperClass())
					{
						OntClass sClass = oClass.getSuperClass();
						
						if (!sClass.getURI().contains("rdf-schema")) 
						{
							tempArray.addAll(addSuperConceptsToView(sClass, true, hlevel-1));
						}
					}
				}
				
				ClassListItem cll = new ClassListItem(this, oClass.getLocalName(), hlevel+1);

				cll.setUri(oClass.getURI());
				
				if (tempArray.size() >= 1)
				{
					int lastIndex = tempArray.size()-1;

					ClassListItem cli = tempArray.get(lastIndex);
					
					if (cli != null)
					{
							cli.addChildren(cll);
					}
				}
				
				tempArray.add(cll);

				List<Individual> temp = sortIndividuals(oClass.listInstances(true).toList());
				
				if (temp.size() > 0) 
				{
					cll.setHasIndividuals(true);
				}

				cll.checkChildCount();

			}
			
			return tempArray;
		}
		
		private ArrayList<ClassListItem> addSubClasses(OntClass oClass, int hLevel, ClassListItem cll2)
		{
			ArrayList<OntClass> list = (ArrayList<OntClass>) oClass.listSubClasses(true).toList();
			
			ArrayList<ClassListItem> tempArray = new ArrayList<ClassListItem>();

			Collections.sort(list, new Comparator<OntClass>()
					{
						@Override
				        public int compare(OntClass o1, OntClass o2)
						{
				            return o1.getLocalName().compareToIgnoreCase(o2.getLocalName());
						}
					}
			);
			
			int h = hLevel+1;
			
			for (OntClass o : list) 
			{
				if (o.getURI() != null)
				{
					ClassListItem cll = new ClassListItem(this, o.getLocalName(), hLevel);
	
					cll.setUri(o.getURI());
					
					tempArray.add(cll);
					
					cll2.addChildren(cll);
					
					if (o.hasSubClass())
					{
						tempArray.addAll(addSubClasses(o, h, cll));
					}
				}
			}
			
			cll2.checkChildCount();
			
			return tempArray;
		}

		private void updateSearch(String uri, String localName)
		{
			showProgressLayer();
			
			if (uri.equalsIgnoreCase(""))
			{
				ontopanelSearchConceptItem.updateText("Search", localName);
				ontopanelSearchIndividualItem.updateText("Search", localName);
				ontopanelSearchPropertyItem.updateText("Search", localName);
				
				ontopanelSearchConceptList.removeAllViews();
				ontopanelSearchIndividualList.removeAllViews();
				ontopanelSearchPropertyList.removeAllViews();
				
				//Concepts
				for (int i = 0; i < ontopanelConceptList.getChildCount(); i++)
				{
					if (ontopanelConceptList.getChildAt(i) instanceof ClassListItem)
					{
						ClassListItem li = (ClassListItem) ontopanelConceptList.getChildAt(i);

						if (li.getItemname().toLowerCase().contains(localName.toLowerCase())) ontopanelSearchConceptList.addView(new ClassListItem(li));
					}
				}
				
				if (ontopanelSearchConceptList.getChildCount() <= 0)
				{
					ontopanelSearchConceptList.addView(getEmptyResultHint("Concepts", localName));
				}
				
				//Individuals
				for (int i = 0; i < ontopanelIndividualList.getChildCount(); i++)
				{
					if (ontopanelIndividualList.getChildAt(i) instanceof IndividualListItem)
					{
						IndividualListItem li = (IndividualListItem) ontopanelIndividualList.getChildAt(i);

						if (li.getItemname().toLowerCase().contains(localName.toLowerCase()) || 
								li.getClassName().toLowerCase().contains(localName.toLowerCase())) 
							ontopanelSearchIndividualList.addView(new IndividualListItem(li));
					}
				}
				
				if (ontopanelSearchIndividualList.getChildCount() <= 0)
				{
					ontopanelSearchIndividualList.addView(getEmptyResultHint("Individuals", localName));
				}
				
				//Relations
				for (int i = 0; i < ontopanelPropertyList.getChildCount(); i++)
				{
					if (ontopanelPropertyList.getChildAt(i) instanceof PropertyListItem)
					{
						PropertyListItem li = (PropertyListItem) ontopanelPropertyList.getChildAt(i);

						if (li.getItemname().toLowerCase().contains(localName.toLowerCase())) ontopanelSearchPropertyList.addView(new PropertyListItem(li));
					}
				}
				
				if (ontopanelSearchPropertyList.getChildCount() <= 0)
				{
					ontopanelSearchPropertyList.addView(getEmptyResultHint("Properties", localName));
				}
				
			}
			else
			{
				ontopanelSearchConceptItem.updateText("Filter", localName);
				ontopanelSearchIndividualItem.updateText("Filter", localName);
				ontopanelSearchPropertyItem.updateText("Filter", localName);
			}

			showFilter();
			
			hideProgressLayer();
		}
		
		private TextView getEmptyResultHint(String type, String localName)
		{
			TextView noResults = new TextView(this);
			
			noResults.setText("No " + type + " were found for \"" + localName + "\".");
			
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.setMargins(25, 25, 25, 25);
			noResults.setLayoutParams(params);
			
			noResults.setTextColor(getResources().getColor(R.color.ontoPanelBtn));
			
			return noResults;
		}

		public void closeFilter() 
		{
			activeSearch = false;
			
			if (!searchInput.getText().toString().equalsIgnoreCase("")) searchInput.setText("");
			
			searchInput.setCursorVisible(false);
			
			ontopanelConceptContainer.setVisibility(View.VISIBLE);
			ontopanelIndividualContainer.setVisibility(View.VISIBLE);
			ontopanelPropertyContainer.setVisibility(View.VISIBLE);

			ontopanelSearchConceptContainer.setVisibility(View.GONE);
			ontopanelSearchIndividualContainer.setVisibility(View.GONE);
			ontopanelSearchPropertyContainer.setVisibility(View.GONE);
			
		}
		
		private void showFilter()
		{
			activeSearch = true;
			
			ontopanelConceptContainer.setVisibility(View.GONE);
			ontopanelIndividualContainer.setVisibility(View.GONE);
			ontopanelPropertyContainer.setVisibility(View.GONE);

			ontopanelSearchConceptContainer.setVisibility(View.VISIBLE);
			ontopanelSearchIndividualContainer.setVisibility(View.VISIBLE);
			ontopanelSearchPropertyContainer.setVisibility(View.VISIBLE);
		}

		public EditText getSearchInput() {
			return searchInput;
		}

		public boolean isUnsaved() {
			return unsaved;
		}

		public void setUnsaved(boolean unsaved) {
			this.unsaved = unsaved;
		}

		public ArrayList<String> getOntoList()
		{
			return ontologyFileNames;
		}
		
		public void setOntoList(ArrayList<String> ontFileNames)
		{
			ontologyFileNames = ontFileNames;
		}

		public static String getNamespace()
		{
			return "http://ontosketch.com/";
		}

}
