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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.drawing.datastructure.CustomPath;
import com.drawing.datastructure.DrawingComposite;


/**
 * Helper class for saving and loading stored information
 * @author Florian Schneider TU Dresden / SAP NEXT Dresden
 * @author Christian Brändel TU Dresden / SAP Research Dresden
 *
 */
public class DataStorageHelper {
	
	static String FILENAME;
	
	static String MATRIXFILENAME;
	
	static String ONTOFILENAME;
	
	static String FILENAMESLIST = "filenames";
	
	/**
	 * Store the <b>DrawingComponent</b> object and all its children to internal storage
	 * @param context The current <b>Context</b> of the application
	 * @param drawView The instance of the current <b>DrawView</b> object that should be stored
	 * @param filename The name of the file that should be stored
	 * @param components The <b>DrawingComponent</b> that should be stored
	 * @return True if everything was stored successfully
	 */
	@SuppressWarnings("resource")
	public static boolean storeData(Context context, DrawView drawView, String filename)
	{
		FILENAME = filename;
		
		MATRIXFILENAME = FILENAME + "_matrices";
		
		ONTOFILENAME = FILENAME + "_ontolist";

		FileOutputStream fos = null;
		FileOutputStream fosMatrix = null;
		FileOutputStream fosOnto = null;
		ObjectOutputStream oos = null;
		ObjectOutputStream oosMatrix = null;
		ObjectOutputStream oosOnto = null;
		
		DrawingComposite components = drawView.getDrawingObjects();
		
		ArrayList<String> ontoList = drawView.ma.getOntoList();
		
		List<CustomPath> highlightedPathList = components.getHighlightedComponents();

		for (int i = 0; i < highlightedPathList.size(); i++)
		{
			if (highlightedPathList.get(i).isHighlighted())
				drawView.updatePath(highlightedPathList.get(i));
		}
		
		float[][] matrices = drawView.getMatrices();
		
		try
		{
			fos = context.openFileOutput(FILENAME, Context.MODE_WORLD_READABLE);
			fosMatrix = context.openFileOutput(MATRIXFILENAME, Context.MODE_WORLD_READABLE);
			fosOnto = context.openFileOutput(ONTOFILENAME, Context.MODE_WORLD_READABLE);
			
			try
			{
				oos = new ObjectOutputStream(fos);
	
				oosMatrix = new ObjectOutputStream(fosMatrix);
				
				oosOnto = new ObjectOutputStream(fosOnto);
				
				oos.writeObject(components);
				
				oosMatrix.writeObject(matrices);
				
				oosOnto.writeObject(ontoList);
			
			} 
			catch (IOException e)
			{
				e.printStackTrace();
				Log.d("STORAGEEXCEPTION", e.toString());
				return false;
			}
			
			
		} 
		catch (FileNotFoundException e)
		{	
			e.printStackTrace();
//			Log.d("STORAGEEXCEPTION", e.toString());
			return false;
		}
		

		if(oos != null)
		{
			try
			{
				oos.close();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
//				Log.d("STORAGEEXCEPTION", e.toString());
				return false;
			}
		}
		
		if(oosMatrix != null)
		{
			try
			{
				oosMatrix.close();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
//				Log.d("STORAGEEXCEPTION", e.toString());
				return false;
			}
		}
		
		if(oosOnto != null)
		{
			try
			{
				oosOnto.close();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
//				Log.d("STORAGEEXCEPTION", e.toString());
				return false;
			}
		}
		
		if(fos != null)
		{
			try
			{
				fos.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
//				Log.d("STORAGEEXCEPTION", e.toString());
				return false;
			}
		}
		
		if(fosMatrix != null)
		{
			try 
			{
				fosMatrix.close();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
//				Log.d("STORAGEEXCEPTION", e.toString());
				return false;
			}
		}
		
		if(fosOnto != null)
		{
			try 
			{
				fosOnto.close();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
//				Log.d("STORAGEEXCEPTION", e.toString());
				return false;
			}
		}
		
		Toast toast = Toast.makeText(context, "Session was successfully stored.", Toast.LENGTH_SHORT);
		
		toast.show();
		
		return true;
	}
	
	/**
	 * Delete the files associated with this filename from the file system 
	 * @param fileName file to delete
	 */
	public static boolean deleteData(Context context, String fileName) {

	FILENAME = fileName;
	
	MATRIXFILENAME = FILENAME + "_matrices";
	
	ONTOFILENAME = FILENAME + "_ontolist";
	
	boolean deleted = false;
	
	try
	{
	
	File file = new File(context.getFilesDir().getPath() + "/" + FILENAME);
	
	deleted = file.delete();
	
	file = new File(context.getFilesDir().getPath() + "/" + MATRIXFILENAME);
	
	deleted = file.delete() && deleted;
	
	file = new File(context.getFilesDir().getPath() + "/" + ONTOFILENAME);
	
	deleted = file.delete() && deleted;
	
	}catch(Exception e)
	{
		e.printStackTrace();
		return false;
	}
	
	return deleted;
	
	}
	
	/**
	 * Load the <b>DrawingComponent</b> object and all its children to internal storage
	 * @param context The current <b>Context</b> of the application
	 * @param filename The name of the file that should be restored
	 * @return The <b>DrawingComponent</b> that was stored before
	 */
	public static DrawingComposite loadData(Context context, String filename)
	{
		FILENAME = filename;
		
		MATRIXFILENAME = FILENAME + "_matrices";
		
		ONTOFILENAME = FILENAME + "_ontolist";
		
		FileInputStream fis = null;
		
		ObjectInputStream ois = null;
		
		FileInputStream fisMatrix = null;
		
		ObjectInputStream oisMatrix = null;
		
		FileInputStream fisOnto = null;
		
		ObjectInputStream oisOnto = null;
		
		ArrayList<String> ontFileNames = null;
		
		DrawingComposite component = null;
		
		float[][] matrices = new float[3][9];
		
		
		try
		{
			fis = new FileInputStream(context.getFilesDir().getPath() + "/" + FILENAME);
			
			fisMatrix = new FileInputStream(context.getFilesDir().getPath() + "/" + MATRIXFILENAME);
			
			fisOnto = new FileInputStream(context.getFilesDir().getPath() + "/" + ONTOFILENAME);
			
			try
			{
				ois = new ObjectInputStream(fis);
				
				oisMatrix = new ObjectInputStream(fisMatrix);
				
				oisOnto = new ObjectInputStream(fisOnto);
				
				try
				{
					component = (DrawingComposite) ois.readObject();
					
					matrices = (float[][]) oisMatrix.readObject();
					
					((MainActivity)context).drawView.restoreMatrices(matrices);
					
					ontFileNames = (ArrayList<String>) oisOnto.readObject();
					
				}
				catch (ClassNotFoundException e) 
				{

					e.printStackTrace();
//					Log.d("STORAGEEXCEPTION", e.toString());
				}
				
				ois.close();
				oisMatrix.close();
				oisOnto.close();
				
			} 
			catch (StreamCorruptedException e)
			{

				e.printStackTrace();
//				Log.d("STORAGEEXCEPTION", e.toString());
				
				
				
			} 
			catch (IOException e)
			{

				e.printStackTrace();
//				Log.d("STORAGEEXCEPTION", e.toString());
			}

		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
//			Log.d("STORAGEEXCEPTION", e.toString());
		}
		
		if (ontFileNames != null) ((MainActivity)context).setOntoList(ontFileNames);
		
		return component;
	}
}
