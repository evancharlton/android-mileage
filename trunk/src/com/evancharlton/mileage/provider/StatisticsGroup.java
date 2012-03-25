
package com.evancharlton.mileage.provider;

import java.util.ArrayList;

public class StatisticsGroup {
    private final ArrayList<Statistic> mStatistics = new ArrayList<Statistic>();

    private final int mLabel;

    public StatisticsGroup(int label, Statistic... statistics) {
        Statistics.GROUPS.add(this);
        final int length = statistics.length;
        for (int i = 0; i < length; i++) {
            mStatistics.add(statistics[i]);
        }
        mLabel = label;
    }

    public int getLabel() {
        return mLabel;
    }

    public ArrayList<Statistic> getStatistics() {
        return mStatistics;
    }
}
