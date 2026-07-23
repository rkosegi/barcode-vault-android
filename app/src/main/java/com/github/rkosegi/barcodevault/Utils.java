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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Utils {
    private static final Gson GSON = new Gson().newBuilder().setPrettyPrinting().create();
    private Utils() {
        // utility class
    }

    public static String getTextSafely(Object obj) {
        if (obj != null) {
            return obj.toString();
        }
        return "";
    }

    public static <T> String toJson(T input) {
        return GSON.toJson(input);
    }

    public static <T> T consumeStreamAs(InputStream is, TypeToken<T> tt) {
        return GSON.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), tt);
    }
}
