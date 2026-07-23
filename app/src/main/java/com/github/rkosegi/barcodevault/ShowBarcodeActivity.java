/*
Copyright 2026 Richard Kosegi

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.github.rkosegi.barcodevault;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.rkosegi.barcodevault.databinding.ShowBarcodeBinding;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static com.github.rkosegi.barcodevault.Constants.EXTRA_ITEM;

public class ShowBarcodeActivity extends AppCompatActivity {
    private static final String TAG = ShowBarcodeActivity.class.getSimpleName();
    private ShowBarcodeBinding binding;

    private static Bitmap createBarcode(String text,
                                        BarcodeFormat format,
                                        int width,
                                        int height) throws Exception {

        BitMatrix matrix = new MultiFormatWriter().encode(
                text,
                format,
                width,
                height
        );

        Bitmap bitmap = Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bitmap.setPixel(
                        x,
                        y,
                        matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        return bitmap;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ShowBarcodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final Intent intent = getIntent();
        final Item item = (Item) intent.getSerializableExtra(EXTRA_ITEM);
        if (item != null) {
            binding.inputBarcodeDesc.setText(item.desc);
            binding.inputBarcodeText.setText(item.text);
            BarcodeFormat format = BarcodeFormat.CODE_39;
            final ImageView barcode = binding.barcode;
            try {
                format = BarcodeFormat.valueOf(item.type);
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, "Invalid barcode type: " + item.type + ", using Code39",
                        Toast.LENGTH_SHORT).show();
            }

            int width;
            int height;

            switch (format) {
                case QR_CODE:
                case DATA_MATRIX:
                case AZTEC:
                case PDF_417:
                case MAXICODE:
                    width = 600;
                    height = 600;
                    break;

                default:
                    width = 1000;
                    height = 300;
                    break;
            }

            try {
                Bitmap bmp = createBarcode(item.text, format, width, height);
                barcode.setImageBitmap(bmp);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "createBarcode", e);
                Toast.makeText(this, "Failed to render barcode: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "createBarcode", e);
                Toast.makeText(this, "Unexpected error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }
}
