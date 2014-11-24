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
 
package com.hwr.android.RecoInterface;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class RecognizerService extends Service 
{
    private ConditionVariable mCondition;
    private boolean mRunRecognizerThread;
    private int mStrokeCnt;
    private boolean mReady;
    
    public Handler mHandler;
         
    @Override
    public void onCreate()  
    { 
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        mRunRecognizerThread = true;
        mStrokeCnt = 0; 
        mReady = false;
        mHandler = null;
        Thread recognizeThread = new Thread(null, mTask, "RecognizerService");
        mCondition = new ConditionVariable(false);
        recognizeThread.start();
        
        Log.d("Recognizer", "recognizeThread started");
    }

    @Override
    public void onDestroy() 
    {
        synchronized( mCondition )
        {
            // Stop the thread from generating further notifications
            // stopRecognizer(); -- causes problems when recognizing
            mRunRecognizerThread = false;
            mCondition.notify();
        }
    }

    private Runnable mTask = new Runnable() 
    {
        public void run() 
        {
            while( mRunRecognizerThread ) 
            { 
                int strokes = 0;
                synchronized( mCondition )
                {
                    while ( ! mReady )
                    {
                        try 
                        {
                            mCondition.wait();
                        } 
                        catch (InterruptedException e) 
                        {
                            
                            e.printStackTrace();
                            continue;
                        }
                    }
                    mReady = false;
                }
                if ( ! mRunRecognizerThread )
                    break;
                                
                synchronized( this )
                {
                    strokes = mStrokeCnt;
                }
                
                if ( strokes > 0 && mHandler != null )
                {
                       // call recognizer
                    
                }
            }
            // Done with our work...  stop the service!
            RecognizerService.this.stopSelf();
        } 
    };
        
      public void dataNotify( int nStrokeCnt )
    {
        synchronized( this )
        {
            mStrokeCnt = nStrokeCnt;
        }
        synchronized( mCondition )
        {
            mReady = true;
            mCondition.notify();
        }
    }   

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    // Local Binder for Recognizer service 
    public class RecognizerBinder extends Binder 
    {
        public RecognizerService getService() 
        {
            return RecognizerService.this;
        }
        
    }
   
    // Instantiate local binder
    private final IBinder mBinder = new RecognizerBinder();
 
}

