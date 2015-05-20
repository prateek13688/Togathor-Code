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

package com.uf.togathor.uitems;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.uf.togathor.R;

/**
 * HookUpProgressDialog
 * 
 * Shows custom HookUp progress dialog with animation.
 */

public class HookUpProgressDialog extends Dialog {

    private ProgressBarCircularIndeterminate pbci;

	public HookUpProgressDialog(Activity activity) {
		super(activity, R.style.Theme_Transparent);

		this.setContentView(R.layout.progress_dialog);
		this.setCancelable(false);

        pbci = (ProgressBarCircularIndeterminate) findViewById(R.id.progressBarCircularIndetermininate);
        pbci.setVisibility(View.GONE);

	}

    public HookUpProgressDialog(Context context) {
        super(context, R.style.Theme_Transparent);

        this.setContentView(R.layout.progress_dialog);
        this.setCancelable(false);

        pbci = (ProgressBarCircularIndeterminate) findViewById(R.id.progressBarCircularIndetermininate);
        pbci.setVisibility(View.GONE);

    }

	@Override
	public void show() {
        pbci.setVisibility(View.VISIBLE);
		super.show();
	}

    @Override
    public void dismiss() {
        if(super.isShowing()) {
            try {
                super.dismiss();
            }
            catch (IllegalArgumentException e)  {
                Log.d("ProgressBar", "Content changed out of context");
            }
        }

        pbci.setVisibility(View.GONE);
    }

    @Override
	public void onWindowFocusChanged(boolean hasFocus) {
	}
	
}
