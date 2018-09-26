package com.chinamobile.cmss.pinpoint.hbase;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

public class HBaseConfig {
    private final boolean profile;

    public HBaseConfig(ProfilerConfig config) {
        this.profile = config.readBoolean("profiler.hbase.client", true);
    }

    public boolean isProfile() {
        return profile;
    }

    @Override
    public String toString() {
        return "HBaseConfig{" +"profile=" + profile + '}';
    }
}
