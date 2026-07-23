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

import com.google.zxing.BarcodeFormat;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Constants {
    private Constants() {
        // utility class
    }

    static final String EXTRA_ITEM = "EXTRA_ITEM";

    static final List<String> BARCODE_TYPES = Stream.of(BarcodeFormat.values()).map(BarcodeFormat::name)
            .collect(Collectors.toList());


}
