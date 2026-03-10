package co.edu.eci.blueprints.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "2D coordinate point", example = "{\"x\": 10, \"y\": 20}")
public record Point(
    @Schema(description = "X coordinate", example = "10") int x,
    @Schema(description = "Y coordinate", example = "20") int y
) {}
