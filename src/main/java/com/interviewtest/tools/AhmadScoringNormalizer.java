package com.interviewtest.tools;

import java.math.BigDecimal;

public class AhmadScoringNormalizer implements ScoringSummary{
    BigDecimal mean;
    BigDecimal standardDeviation;
    BigDecimal variance;
    BigDecimal median;
    BigDecimal min;
    BigDecimal max;

    public AhmadScoringNormalizer(BigDecimal mean, BigDecimal standardDeviation, BigDecimal variance, BigDecimal median, BigDecimal min, BigDecimal max) {
        this.mean = mean;
        this.standardDeviation = standardDeviation;
        this.variance = variance;
        this.median = median;
        this.min = min;
        this.max = max;
    }

    @Override
    public BigDecimal mean() {
        return mean;
    }

    @Override
    public BigDecimal standardDeviation() {
        return standardDeviation;
    }

    @Override
    public BigDecimal variance() {
        return variance;
    }

    @Override
    public BigDecimal median() {
        return median;
    }

    @Override
    public BigDecimal min() {
        return min;
    }

    @Override
    public BigDecimal max() {
        return max;
    }
}
