package com.zencube.registry.enterprise.enums;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public enum DateRangeFilter {
    TODAY {
        @Override
        public Instant getStartDate() {
            return Instant.now().truncatedTo(ChronoUnit.DAYS);
        }
    },
    LAST_7_DAYS {
        @Override
        public Instant getStartDate() {
            return Instant.now().minus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        }
    },
    LAST_30_DAYS {
        @Override
        public Instant getStartDate() {
            return Instant.now().minus(30, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        }
    },
    LAST_90_DAYS {
        @Override
        public Instant getStartDate() {
            return Instant.now().minus(90, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        }
    },
    ALL_TIME {
        @Override
        public Instant getStartDate() {
            return Instant.EPOCH;
        }
    },
    CUSTOM {
        @Override
        public Instant getStartDate() {
            return null; // Must be provided externally
        }
    };

    public abstract Instant getStartDate();
}
