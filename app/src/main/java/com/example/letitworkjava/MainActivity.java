package com.example.letitworkjava;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 0;
    private TextView messageTextView;
    private BroadcastReceiver updateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)) {
                // If explanation needed before requesting permission (Optional)
                // You can display a dialog here explaining why the app needs this permission.
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECEIVE_SMS},
                        MY_PERMISSIONS_REQUEST_RECEIVE_SMS);
            }
        }

        messageTextView = findViewById(R.id.messageTextView);

        // Register a BroadcastReceiver to update the TextView
        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null && intent.getAction().equals("UPDATE_TEXTVIEW")) {
                    String receivedMessage = intent.getStringExtra("message");

                    // Update the TextView with the received message
                    if (messageTextView != null) {
                        messageTextView.setText(receivedMessage);
                    }
                }
            }
        };
        registerReceiver(updateReceiver, new IntentFilter("UPDATE_TEXTVIEW"));
        messageTextView = findViewById(R.id.messageTextView);

        // Enable auto-linking for URLs in the TextView
        messageTextView.setAutoLinkMask(Linkify.WEB_URLS);
        messageTextView.setMovementMethod(new CustomLinkMovementMethod()); // Custom movement method to handle link clicks
    }

    // Custom LinkMovementMethod to handle link clicks
    private class CustomLinkMovementMethod extends android.text.method.LinkMovementMethod {
        @Override
        public boolean onTouchEvent(TextView widget, android.text.Spannable buffer, android.view.MotionEvent event) {
            boolean handled = false;
            handled = super.onTouchEvent(widget, buffer, event);

            if (!handled && event.getAction() == android.view.MotionEvent.ACTION_UP) {
                // Handle clicks on links manually if not handled by default method
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                android.text.Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                // Get the URL link if clicked
                android.text.style.ClickableSpan[] link = buffer.getSpans(off, off, android.text.style.ClickableSpan.class);
                if (link.length != 0) {
                    String url = ((android.text.style.URLSpan) link[0]).getURL();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    handled = true;
                }
            }
            return handled;
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateReceiver != null) {
            unregisterReceiver(updateReceiver);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECEIVE_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Thank you for permitting!", Toast.LENGTH_SHORT).show();
                    // Permission granted, you can perform actions that require this permission
                } else {
                    Toast.makeText(this, "Well, I can't do anything until you permit me", Toast.LENGTH_LONG).show();
                    // Permission denied, handle accordingly (disable functionality, show message, etc.)
                }
                return;
            }
        }
    }
}
