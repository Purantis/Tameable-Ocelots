package com.purantis.tameableocelots.util;

import java.util.UUID;

public interface OcelotDataAccessor {
    boolean isTameOcelotSitting();
    void setTameOcelotSitting(boolean sitting);
    UUID getOcelotOwnerUuid();
    void setOcelotOwnerUuid(UUID uuid);
    boolean isOcelotTrusting();
}