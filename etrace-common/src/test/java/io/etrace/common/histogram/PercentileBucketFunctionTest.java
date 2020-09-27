/*
 * Copyright 2019 etrace.io
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

package io.etrace.common.histogram;

import org.junit.Test;

public class PercentileBucketFunctionTest {

    @Test
    public void testGetAmount() {
        BucketFunction bucketFunction = PercentileBucketFunction.getFunctions(0);
        //System.out.println(bucketFunction.getCategories());
        assert 1 == bucketFunction.getAmount(0);
        assert 6 == bucketFunction.getAmount(5);
        assert 78643 == bucketFunction.getAmount(98);
        assert 91750 == bucketFunction.getAmount(99);
        assert 91750 == bucketFunction.getAmount(1000);
        bucketFunction = PercentileBucketFunction.getFunctions(30);
        assert 30 == bucketFunction.getAmount(0);
        assert 59 == bucketFunction.getAmount(10);
        assert 256 == bucketFunction.getAmount(31);
        assert 1677721 == bucketFunction.getAmount(98);
        assert 1887436 == bucketFunction.getAmount(99);
        assert 1887436 == bucketFunction.getAmount(100);
    }
}
