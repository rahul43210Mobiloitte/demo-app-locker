package com.gueei.applocker;


import com.gueei.applocker.R;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegistrationActivity extends Activity {
	private static final String DATABASE_TABLE = "loginTable";
	private static final String NAME = "r_name";
	private static final String ID = "_Id";
	private static final String USERNAME = "u_name";
	private static final String PASSWORD = "p_word";
	static int j=1;
	private EditText name_ed, user_ed, pword_ed;
	private DBAdapter mydb = new DBAdapter(this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.regis);
		mydb.open();
		mydb.createTables();
		final Button reg= (Button) findViewById(R.id.register);
		Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		name_ed =(EditText) findViewById(R.id.name_ed);
		user_ed =(EditText) findViewById(R.id.user_ed);
		pword_ed =(EditText) findViewById(R.id.pword_ed);

		{ reg.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String ch = name_ed.getText().toString();
				int p=ch.length();
				switch (p) {
				case 0:Toast.makeText(getBaseContext(), "  Enter Name ", Toast.LENGTH_SHORT).show();
				break;
				default:
				{

					String ch1 = user_ed.getText().toString();
					int p1=ch1.length();

					switch (p1) {
					case 0:Toast.makeText(getBaseContext(), "  Enter Username", Toast.LENGTH_SHORT).show();
					break;

					default:					
					{   
						String ch2 = pword_ed.getText().toString();
						int p2 = ch2.length();

						switch (p2) {
						case 0:Toast.makeText(getBaseContext(), "  Enter Password", Toast.LENGTH_SHORT).show();
						break;

						default:					
						{try{
							insertEntry();
							Intent intent=new Intent(getApplicationContext(), LoginPageActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
						}
						catch(SQLiteException ex){
							Toast.makeText(getApplicationContext(), "couldn't be updated",5).show(); 
						}
						break;  }

						}



						break;}

					}



					break;}
				} 
			}
		});

		
		}
	mydb.close();
	}

	private void insertEntry(){	
		ContentValues newValues = new ContentValues();		
		newValues.put(ID, j);
		newValues.put(NAME, name_ed.getText().toString());
		newValues.put(USERNAME, user_ed.getText().toString());
		newValues.put(PASSWORD, pword_ed.getText().toString());
		mydb.insertRow(DATABASE_TABLE, newValues);
		mydb.close();

	}
}