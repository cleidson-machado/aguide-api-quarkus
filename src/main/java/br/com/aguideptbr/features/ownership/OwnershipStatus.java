package br.com.aguideptbr.features.ownership;

/**
 * Enum representing the status of content ownership validation.
 *
 * - PENDING: Ownership claim awaiting validation
 * - VERIFIED: Ownership validated successfully (HMAC match)
 * - REJECTED: Ownership validation failed (HMAC mismatch or channel mismatch)
 */
public enum OwnershipStatus {
    /**
     * Ownership claim created but not yet validated.
     */
    PENDING,

    /**
     * Ownership validated successfully.
     * User's YouTube channel ID matches content's channel ID,
     * and HMAC signature is valid.
     */
    VERIFIED,

    /**
     * Ownership validation failed.
     * Either channel IDs don't match or HMAC signature is invalid.
     */
    REJECTED
}
