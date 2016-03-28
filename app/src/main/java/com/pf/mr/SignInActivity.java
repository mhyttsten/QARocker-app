package com.pf.mr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.pf.mr.R;

public class SignInActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        startActivity(new Intent(this, MainActivity.class));
    }
}
