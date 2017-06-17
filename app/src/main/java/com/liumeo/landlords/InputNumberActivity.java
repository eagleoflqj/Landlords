package com.liumeo.landlords;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class InputNumberActivity extends Activity
{
	String code;
	String name;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_input_number);
		Intent intent=getIntent();
		name=intent.getStringExtra("name");
	}

	public void confirm(View view)
	{
		code=((EditText)findViewById(R.id.edt_num)).getText().toString();
		Intent intent = new Intent(this, GameActivity.class);
		intent.putExtra("name",name);
		intent.putExtra("code",code);
		intent.putExtra("isServer",false);
		startActivity(intent);
	}

	public void back(View view)
	{
		finish();
	}
}
