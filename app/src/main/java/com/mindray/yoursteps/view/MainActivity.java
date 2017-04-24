package com.mindray.yoursteps.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.mindray.yoursteps.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    // Start of Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* switch (item.getItemId()) {
            case R.id.action_sign_in:
                Intent accountIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(accountIntent);
                break;
            case R.id.action_update:
                new UpdateTask(MainActivity.this).update();
                break;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.action_wave:
                startActivity(new Intent(MainActivity.this, WaveActivity.class));
        }

        return super.onOptionsItemSelected(item); */
        return true;
    }
    // End of Menu
}
