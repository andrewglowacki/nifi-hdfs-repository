package org.apache.nifi.hdfs.repository;

import org.apache.nifi.controller.repository.claim.ResourceClaim;

public class ClaimAndLength {

    private final ResourceClaim claim;
    private final long length;

    public ClaimAndLength(ResourceClaim claim, long length) {
        this.claim = claim;
        this.length = length;
    }

    public ResourceClaim getClaim() {
        return claim;
    }
    public long getLength() {
        return length;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (claim == null ? 0 : claim.hashCode());
        return result;
    }

    /**
     * Equality is determined purely by the ResourceClaim's equality
     *
     * @param obj the object to compare against
     * @return -1, 0, or +1 according to the contract of Object.equals
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final ClaimAndLength other = (ClaimAndLength) obj;
        return claim.equals(other.getClaim());
    }

}
