/*
 * The MIT License (MIT)
 * 
 * Copyright � 2013 Clover Studio Ltd. All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.uf.togathor.utils;

import java.io.IOException;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.util.Log;

import com.uf.togathor.R;
import com.uf.togathor.SignInActivity;
import com.uf.togathor.Togathor;
import com.uf.togathor.db.couchdb.TogathorException;
import com.uf.togathor.db.couchdb.TogathorForbiddenException;
import com.uf.togathor.uitems.HookUpDialog;
import com.uf.togathor.utils.constants.Const;

/**
 * SpikaAsync
 * 
 * HookUp base AsyncTask checks network connection before execution.
 */

public abstract class TogathorAsync<Params, Progress, Result> extends
		AsyncTask<Params, Progress, Result> {

	protected Context mContext;
	private Exception exception;

	protected TogathorAsync(final Context context) {
		this.mContext = context;
	}

	@Override
	protected void onPreExecute() {
		if (Togathor.hasNetworkConnection()) {
			super.onPreExecute();
		} else {
			this.cancel(true);
		}
	}
	
	@Override
	protected Result doInBackground(Params... params) {
		Result result = null;
		try {
			result = backgroundWork(params);
		} catch (NullPointerException e) {
			exception = e;
			e.printStackTrace();
		}
        return result;
	}

	@Override
	protected void onPostExecute(Result result) {
		super.onPostExecute(result);
		if (exception != null)
		{
			String error = (exception.getMessage() != null) ? exception.getMessage() : mContext.getString(R.string.an_internal_error_has_occurred);
			
			Log.e(Const.ERROR, error);
			
			final HookUpDialog dialog = new HookUpDialog(mContext);
			String errorMessage = null;
			if (exception instanceof IOException){
			    errorMessage = mContext.getString(R.string.can_not_connect_to_server) + "\n" + exception.getClass().getName() + " " + error;
			}else if(exception instanceof JSONException){
			    errorMessage = mContext.getString(R.string.an_internal_error_has_occurred) + "\n" + exception.getClass().getName() + " " + error;
			}else if(exception instanceof NullPointerException){
			    errorMessage = mContext.getString(R.string.an_internal_error_has_occurred) + "\n" + exception.getClass().getName() + " " + error;
			}else if(exception instanceof TogathorException){
				errorMessage = mContext.getString(R.string.an_internal_error_has_occurred) + "\n" + error;
			}else if(exception instanceof TogathorForbiddenException){
				errorMessage = mContext.getString(R.string.an_internal_error_has_occurred) + "\n" + error;
			}else{
			    errorMessage = mContext.getString(R.string.an_internal_error_has_occurred) + "\n" + exception.getClass().getName() + " " + error;
			}	
			
			if (mContext instanceof Activity) {
				if (!((Activity)mContext).isFinishing())
				{
					if(exception instanceof TogathorForbiddenException){
						//token expired
						errorMessage=mContext.getString(R.string.token_expired_error);
						dialog.setOnDismissListener(new OnDismissListener() {
							
							@Override
							public void onDismiss(DialogInterface dialog) {
								mContext.startActivity(new Intent(mContext, SignInActivity.class)
										.putExtra(mContext.getString(R.string.token_expired_error), true));
								((Activity) mContext).finish();
							}
						});
					}
					dialog.showOnlyOK(errorMessage);
				}
			}
		}
	}

	protected Result backgroundWork(Params... params) throws NullPointerException {
		return null;
	}
	
}
