package de.numcodex.feasibility_gui_backend.query.broker.dsf;

import de.numcodex.feasibility_gui_backend.query.QueryMediaType;
import de.numcodex.feasibility_gui_backend.query.broker.UnsupportedMediaTypeException;

import java.util.List;

import static de.numcodex.feasibility_gui_backend.query.QueryMediaType.*;

/**
 * Utility for translating backend defined query media types into their corresponding DSF middleware counterpart.
 */
class DSFMediaTypeTranslator {
    /**
     * Given a string representation of a backend defined {@link QueryMediaType}
     * translates it into a media type compatible with DSF middleware instances.
     *
     * @param mediaType The media type that shall be translated.
     * @return The translated media type.
     * @throws UnsupportedMediaTypeException If the given media type is not supported.
     */
    public String translate(String mediaType) throws UnsupportedMediaTypeException {
        try {
            return switch(QueryMediaType.fromRepresentation(mediaType)) {
                case STRUCTURED_QUERY -> "application/json";
                case CQL -> CQL.getRepresentation();
                case FHIR -> "application/x-fhir-query";
            };
        } catch (IllegalArgumentException e) {
            throw new UnsupportedMediaTypeException(mediaType, List.of(
                    STRUCTURED_QUERY.getRepresentation(),
                    CQL.getRepresentation(),
                    FHIR.getRepresentation()));
        }
    }
}
