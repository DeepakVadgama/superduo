package it.jaschke.alexandria.scanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.Collections;

import it.jaschke.alexandria.AddBook;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class SimpleScannerActivity extends Activity implements ZXingScannerView.ResultHandler {

    private final String TAG = getClass().getSimpleName();
    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        mScannerView.setFormats(Collections.singletonList(BarcodeFormat.EAN_13));
        mScannerView.setAutoFocus(true);
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        mScannerView.stopCamera();
        Intent output = new Intent();

        if (rawResult.getText() == null) {
            setResult(RESULT_CANCELED, output);
        } else {
            output.putExtra(AddBook.EAN_CONTENT, rawResult.getText());
            setResult(RESULT_OK, output);
        }
        finish();
    }
}
