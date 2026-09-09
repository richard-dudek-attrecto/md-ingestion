package com.rddk.mdingestion.domain;

/**
 * Classification of an event relative to the per-device watermark.
 *
 * <ul>
 *     <li>{@link #ON_TIME} - timestamp is at or ahead of the watermark.</li>
 *     <li>{@link #OUT_OF_ORDER} - timestamp is behind the watermark but still within the
 *     allowed lateness window, so it can still be used downstream.</li>
 *     <li>{@link #LATE} - timestamp is behind the watermark by more than the allowed
 *     lateness window; downstream consumers may drop or side-channel it.</li>
 * </ul>
 *
 * @author richard.dudek
 * @since 0.0.1
 */
public enum LatenessClassification {
    /**
     * Timestamp is at or ahead of the watermark.
     */
    ON_TIME,

    /**
     * Timestamp is behind the watermark but still within the allowed lateness window.
     */
    OUT_OF_ORDER,

    /**
     * Timestamp is behind the watermark by more than the allowed lateness window.
     */
    LATE
}
