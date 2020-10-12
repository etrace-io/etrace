/*
 * Copyright 2020 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etrace.api.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

// todo:
@Deprecated
public class AesUtils {

    private static final String ALG = "AES";

    private static final String PADDING = "SHA1PRNG";

    private static SecretKeySpec generateAESKeyWithSeed(String password) throws Exception {

        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALG);
        SecureRandom random = SecureRandom.getInstance(PADDING);
        random.setSeed(password.getBytes());
        keyGenerator.init(128, random);
        SecretKey key = keyGenerator.generateKey();
        return new SecretKeySpec(key.getEncoded(), ALG);
    }

    public static byte[] encryptWithAES(byte[] content, String password) throws Exception {

        SecretKeySpec keySpec = generateAESKeyWithSeed(password);
        Cipher cipher = Cipher.getInstance(ALG);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(content);
    }

    public static byte[] decryptWithAES(byte[] content, String password) throws Exception {
        SecretKeySpec keySpec = generateAESKeyWithSeed(password);
        Cipher cipher = Cipher.getInstance(ALG);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher.doFinal(content);
    }
}
