package com.joaorihan.courierprime.courier;

import lombok.Getter;
import org.bukkit.entity.EntityType;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Represents a courier type that can be either a vanilla EntityType or a custom entity
 * from MythicMobs (mm:) or ModelEngine (meg:)
 */
@Getter
public class CourierType {

    private static final Pattern CUSTOM_IDENTIFIER =
            Pattern.compile("[A-Za-z0-9][A-Za-z0-9_.-]*");

    public enum Provider {
        VANILLA,
        MYTHICMOBS,
        MODELENGINE
    }

    private final Provider provider;
    private final String identifier;
    private final EntityType vanillaType;

    private CourierType(Provider provider, String identifier, EntityType vanillaType) {
        this.provider = provider;
        this.identifier = identifier;
        this.vanillaType = vanillaType;
    }

    /**
     * Parses a courier type string into a CourierType object
     * Formats:
     * - "mm:MOB_ID" for MythicMobs
     * - "meg:MODEL_ID" for ModelEngine
     * - "ENTITY_TYPE" for vanilla entities
     *
     * @param typeString The string to parse
     * @return The parsed CourierType, or null if invalid
     */
    public static CourierType parse(String typeString) {
        if (typeString == null) {
            return null;
        }

        String normalized = typeString.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        String lower = normalized.toLowerCase(Locale.ROOT);

        // MythicMobs format: mm:MOB_ID
        if (lower.startsWith("mm:")) {
            String mobId = normalized.substring(3).trim();
            if (!isValidCustomIdentifier(mobId)) return null;
            return new CourierType(Provider.MYTHICMOBS, mobId, null);
        }

        // ModelEngine format: meg:MODEL_ID
        if (lower.startsWith("meg:")) {
            String modelId = normalized.substring(4).trim();
            if (!isValidCustomIdentifier(modelId)) return null;
            return new CourierType(Provider.MODELENGINE, modelId, null);
        }

        // Vanilla EntityType
        try {
            String entityName = normalized.toUpperCase(Locale.ROOT);
            EntityType entityType = EntityType.valueOf(entityName);
            if (!entityType.isSpawnable()) {
                return null;
            }
            return new CourierType(Provider.VANILLA, entityName, entityType);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Checks whether an identifier is safe to pass to an optional entity
     * provider. Provider identifiers are deliberately kept narrower than a
     * free-form command argument so malformed configuration cannot become an
     * API lookup or spawn request.
     */
    public static boolean isValidCustomIdentifier(String identifier) {
        return identifier != null && CUSTOM_IDENTIFIER.matcher(identifier.trim()).matches();
    }

    /**
     * Checks if this is a vanilla entity type
     */
    public boolean isVanilla() {
        return provider == Provider.VANILLA;
    }

    /**
     * Checks if this is a MythicMobs entity
     */
    public boolean isMythicMobs() {
        return provider == Provider.MYTHICMOBS;
    }

    /**
     * Checks if this is a ModelEngine entity
     */
    public boolean isModelEngine() {
        return provider == Provider.MODELENGINE;
    }

    /**
     * Gets the string representation for storage/config
     */
    @Override
    public String toString() {
        return switch (provider) {
            case MYTHICMOBS -> "mm:" + identifier;
            case MODELENGINE -> "meg:" + identifier;
            case VANILLA -> identifier;
        };
    }

    /**
     * Gets a display-friendly name
     */
    public String getDisplayName() {
        return switch (provider) {
            case MYTHICMOBS -> "§6[MM] §e" + identifier;
            case MODELENGINE -> "§b[MEG] §3" + identifier;
            case VANILLA -> "§a" + identifier;
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CourierType that = (CourierType) obj;
        return provider == that.provider && identifier.equals(that.identifier);
    }

    @Override
    public int hashCode() {
        return 31 * provider.hashCode() + identifier.hashCode();
    }
}
