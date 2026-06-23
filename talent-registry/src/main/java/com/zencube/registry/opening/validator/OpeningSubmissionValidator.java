package com.zencube.registry.opening.validator;

import com.zencube.registry.opening.domain.Opening;
import com.zencube.registry.opening.exception.IncompleteOpeningException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OpeningSubmissionValidator {

    public void validate(Opening opening) {
        List<String> missingFields = new ArrayList<>();

        if (opening.getTitle() == null || opening.getTitle().isBlank()) missingFields.add("title");
        if (opening.getDescription() == null || opening.getDescription().isBlank()) missingFields.add("description");
        if (opening.getRequirements() == null || opening.getRequirements().isBlank()) missingFields.add("requirements");
        if (opening.getJobType() == null) missingFields.add("jobType");
        if (opening.getDomain() == null || opening.getDomain().isBlank()) missingFields.add("domain");
        if (opening.getLocation() == null || opening.getLocation().isBlank()) missingFields.add("location");
        if (opening.getApplicationDeadline() == null) missingFields.add("applicationDeadline");
        if (opening.getPositions() == null || opening.getPositions() <= 0) missingFields.add("positions");
        if (opening.getRequiredSkills() == null || opening.getRequiredSkills().isBlank()) missingFields.add("requiredSkills");

        if (!missingFields.isEmpty()) {
            throw new IncompleteOpeningException("Opening cannot be submitted. Missing or invalid fields: " + String.join(", ", missingFields));
        }
    }
}
