package com.kitchen.central.dto;

/** 一个批次的出餐额度：实际产量、已发（不含退回）、还能发多少，以后台算的为准。 */
public class BatchQuota {

    public Long batchId;
    public Integer actualPortions;
    public Integer sent;
    public Integer remaining;

    public BatchQuota(Long batchId, Integer actualPortions, Integer sent) {
        this.batchId = batchId;
        this.actualPortions = actualPortions;
        this.sent = sent;
        this.remaining = actualPortions - sent;
    }
}
