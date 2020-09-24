package com.example.facadeservice;

import java.util.LongSummaryStatistics;

public class ResultData {

  private LongSummaryStatistics statistics;
  private long spentTime;

  public ResultData(LongSummaryStatistics statistics, long spentTime) {
    this.statistics = statistics;
    this.spentTime = spentTime;
  }

  public LongSummaryStatistics getStatistics() {
    return statistics;
  }

  public long getSpentTime() {
    return spentTime;
  }
}
