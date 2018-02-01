package com.liumeo.landlords;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;

public class StartGameActivity extends Activity
{
	String name;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_game);
	}

	public void newGame(View view)
	{
		name = ((EditText) findViewById(R.id.edt_name)).getText().toString();
		if (name.contains(","))
		{
			new AlertDialog.Builder(this).setTitle("提示").setMessage("名称不能带\",\"").setPositiveButton("确定", null).show();
			return;
		}
		boolean isServer = view == findViewById(R.id.btn_new);
		Intent intent = new Intent(this, isServer ? GameActivity.class : InputNumberActivity.class);
		intent.putExtra("name", name);
		if (isServer)
		{
			intent.putExtra("isServer", true);
		}
		startActivity(intent);
	}
}
