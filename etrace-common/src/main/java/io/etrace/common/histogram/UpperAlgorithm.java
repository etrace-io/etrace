package io.etrace.common.histogram;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpperAlgorithm {
    protected List<Integer> ratios;

    public UpperAlgorithm(List<Integer> upperRatios) {
        this.ratios = upperRatios;
    }

    public List<String> getMeta() {
        List<String> meta = new ArrayList<>();
        if (ratios == null || ratios.isEmpty()) {
            meta.add("upper_" + DistributionConstant.RATIO_95);
        } else {
            for (Integer upperRatio : ratios) {
                meta.add("upper_" + upperRatio);
            }
        }
        return meta;
    }

    public Map<String, Long> getUpper(Distribution distribution) throws IOException {
        long count = distribution.getCount();
        if (ratios == null) {
            ratios = new ArrayList<>();
        }
        if (ratios.isEmpty()) {
            ratios.add(DistributionConstant.RATIO_95);
        } else if (ratios.size() > 1) {
            ratios.sort((o1, o2) -> o2 - o1);
        }

        double[] ratioNumber = new double[ratios.size()];
        for (int i = 0; i < ratioNumber.length; i++) {
            ratioNumber[i] = ratios.get(i) / 100.0 * count;
        }
        BucketFunction bucketFunction = distribution.getBucketFunction();
        Map<String, Long> result = new HashMap<>();
        if (count <= 0) {
            String categorie = bucketFunction.getDistribution(0);
            for (Integer ratio : ratios) {
                writeUpper(result, ratio, 0);
            }
            return result;
        }

        long surplus = count;
        int ratioIndex = 0;
        long[] values = distribution.getValues();
        for (int i = values.length - 1; i >= 0; i--) {
            surplus -= values[i];
            while (ratioIndex < ratioNumber.length && surplus <= ratioNumber[ratioIndex]) {
                writeUpper(result, ratios.get(ratioIndex), bucketFunction.getAmount(i));
                ratioIndex++;
            }
            if (ratioIndex >= ratioNumber.length) {
                break;
            }
        }
        return result;
    }

    private void writeUpper(Map<String, Long> result, int ratio, long amount) throws IOException {
        result.put("upper_" + ratio, amount);
    }
}
