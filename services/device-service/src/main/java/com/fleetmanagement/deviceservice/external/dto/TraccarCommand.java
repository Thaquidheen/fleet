
package com.fleetmanagement.deviceservice.external.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TraccarCommand {

    private Long id;
    private String description;
    private String type;

    @JsonProperty("deviceId")
    private Long deviceId;

    @JsonProperty("textChannel")
    private Boolean textChannel;

    @JsonProperty("attributes")
    private Map<String, Object> attributes;
}
