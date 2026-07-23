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
import android.net.Uri;
import android.os.Bundle;

import com.github.rkosegi.barcodevault.databinding.ActivityAddItemBinding;
import com.google.common.base.Strings;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import static androidx.activity.result.contract.ActivityResultContracts.OpenDocument;
import static com.github.rkosegi.barcodevault.Constants.BARCODE_TYPES;
import static com.github.rkosegi.barcodevault.Constants.EXTRA_ITEM;
import static com.github.rkosegi.barcodevault.Utils.getTextSafely;

public class AddItemActivity extends AppCompatActivity {
    private Item item;
    private ActivityAddItemBinding binding;
    private final ActivityResultLauncher<ScanOptions> qrLauncher = registerForActivityResult(new ScanContract(),
            this::onScanResult);
    private final ActivityResultLauncher<String[]> pickImageLauncher = registerForActivityResult(new OpenDocument(),
                    this::onImagePickResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddItemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final Intent intent = getIntent();
        item = (Item) intent.getSerializableExtra(EXTRA_ITEM);
        if (item == null) {
            item = new Item(0,
                    getString(R.string.empty_barcode),
                    getString(R.string.describe_me),
                    ScanOptions.QR_CODE, "", null
            );
        } else {
            binding.btnAddNow.setText(R.string.update_item);
        }

        binding.inputBarcodeType.setAdapter(new BarcodeTypeSpinnerAdapter(this));
        if (!Strings.isNullOrEmpty(item.type) && BARCODE_TYPES.contains(item.type)) {
            binding.inputBarcodeType.setSelection(BARCODE_TYPES.indexOf(item.type));
        }

        binding.inputBarcodeText.setText(item.text);
        binding.inputBarcodeDesc.setText(item.desc);
        binding.inputIconUrl.setText(item.iconUrl);
        binding.inputBarcodeLayout.setEndIconOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES);
            options.setBarcodeImageEnabled(true);
            options.setPrompt("Scan code");

            qrLauncher.launch(options);
        });
        binding.inputUrlLayout.setEndIconOnClickListener(v -> pickImageLauncher.launch(new String[]{"image/*"}));

        binding.btnAddNow.setOnClickListener(v -> {
            item.text = getTextSafely(binding.inputBarcodeText.getText());
            item.desc = getTextSafely(binding.inputBarcodeDesc.getText());
            item.type = getTextSafely(binding.inputBarcodeType.getSelectedItem());
            item.iconUrl = getTextSafely(binding.inputIconUrl.getText());
            setResult(RESULT_OK, new Intent().putExtra(EXTRA_ITEM, item));
            finish();
        });
    }

    private void onScanResult(ScanIntentResult result) {
        if (result.getContents() != null) {
            binding.inputBarcodeText.setText(result.getContents());
        }
    }

    private void onImagePickResult(Uri uri) {
        if (uri != null) {
            getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            binding.inputIconUrl.setText(uri.toString());
        }
    }
}
