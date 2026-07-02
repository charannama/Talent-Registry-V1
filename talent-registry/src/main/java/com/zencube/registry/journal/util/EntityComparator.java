package com.zencube.registry.journal.util;

import com.zencube.registry.journal.exception.AuditException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
public class EntityComparator {

    // Common JPA and audit fields to ignore during business change comparison
    private static final Set<String> IGNORED_FIELDS = Set.of(
            "id", "createdAt", "updatedAt", "version", "createdBy", "updatedBy", "deletedAt", "deletedBy"
    );

    public List<FieldChange> compare(Object before, Object after) {
        if (before == null || after == null) {
            throw new AuditException("Cannot compare null entities for audit");
        }
        if (!before.getClass().equals(after.getClass())) {
            throw new AuditException("Cannot compare mismatched entity types");
        }

        List<FieldChange> changes = new ArrayList<>();
        Field[] fields = before.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (IGNORED_FIELDS.contains(field.getName())) {
                continue;
            }

            // Skip deep collection tracking to avoid N+1 and lazy init exceptions during reflection
            if (java.util.Collection.class.isAssignableFrom(field.getType())) {
                continue;
            }

            field.setAccessible(true);
            try {
                Object oldValue = field.get(before);
                Object newValue = field.get(after);

                if (!Objects.equals(oldValue, newValue)) {
                    String oldStr = oldValue != null ? oldValue.toString() : null;
                    String newStr = newValue != null ? newValue.toString() : null;
                    changes.add(new FieldChange(field.getName(), oldStr, newStr));
                }
            } catch (IllegalAccessException e) {
                throw new AuditException("Reflection failed for field: " + field.getName(), e);
            }
        }
        return changes;
    }
}
