package com.george.reminderpro;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class HelpActivity extends BaseActivity {

	private ImageButton okButton;

    @Override protected int getLayoutResource() {
        return R.layout.activity_help;
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_help);

        //add back button on the top
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		okButton = (ImageButton)findViewById(R.id.buttonOk);
		
		OnClickListener okButtonClickListener = new OnClickListener() {
					
			@Override
			public void onClick(View v) {
				finish();
			}
		};
		
		okButton.setOnClickListener(okButtonClickListener);
	}
}
