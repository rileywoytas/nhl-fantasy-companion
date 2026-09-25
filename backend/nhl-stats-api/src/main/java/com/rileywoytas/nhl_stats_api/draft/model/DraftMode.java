package com.rileywoytas.nhl_stats_api.draft.model;

public enum DraftMode {
    /** CPU drafters make every pick that isn't the user's. */
    MOCK,
    /** Real draft night: the user records every pick, including other managers' (mark-as-taken). */
    LIVE
}
