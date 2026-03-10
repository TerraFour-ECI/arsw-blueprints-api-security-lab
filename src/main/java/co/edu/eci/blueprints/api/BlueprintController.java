package co.edu.eci.blueprints.api;

import co.edu.eci.blueprints.api.dto.ApiResponse;
import co.edu.eci.blueprints.model.Blueprint;
import co.edu.eci.blueprints.model.Point;
import co.edu.eci.blueprints.persistence.BlueprintNotFoundException;
import co.edu.eci.blueprints.persistence.BlueprintPersistenceException;
import co.edu.eci.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/blueprints")
@Tag(name = "Blueprints", description = "Secured REST API for blueprint management (requires JWT Bearer token)")
@SecurityRequirement(name = "bearer-jwt")
public class BlueprintController {

    private final BlueprintsServices services;

    public BlueprintController(BlueprintsServices services) {
        this.services = services;
    }

    // ───────── READ endpoints ─────────────────────────────────────────────────

    @Operation(summary = "List all blueprints",
               description = "Returns every blueprint in the system. Requires scope: blueprints.read")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blueprints retrieved"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient scope")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(200, "execute ok", services.getAllBlueprints()));
    }

    @Operation(summary = "Get blueprints by author",
               description = "Returns all blueprints for a given author. Requires scope: blueprints.read")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blueprints found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Author not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @GetMapping("/{author}")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    public ResponseEntity<ApiResponse<?>> byAuthor(
            @Parameter(description = "Blueprint author", example = "john") @PathVariable String author) {
        try {
            Set<Blueprint> blueprints = services.getBlueprintsByAuthor(author);
            return ResponseEntity.ok(new ApiResponse<>(200, "execute ok", blueprints));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        }
    }

    @Operation(summary = "Get a specific blueprint",
               description = "Returns a single blueprint by author + name. The configured filter is applied. Requires scope: blueprints.read")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blueprint found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @GetMapping("/{author}/{bpname}")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.read')")
    public ResponseEntity<ApiResponse<?>> byAuthorAndName(
            @Parameter(description = "Author name", example = "john") @PathVariable String author,
            @Parameter(description = "Blueprint name", example = "house") @PathVariable String bpname) {
        try {
            Blueprint bp = services.getBlueprint(author, bpname);
            return ResponseEntity.ok(new ApiResponse<>(200, "execute ok", bp));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        }
    }

    // ───────── WRITE endpoints ────────────────────────────────────────────────

    @Operation(summary = "Create a new blueprint",
               description = "Registers a new blueprint. The combination (author, name) must be unique. Requires scope: blueprints.write")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Blueprint created"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Blueprint already exists or insufficient scope"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    public ResponseEntity<ApiResponse<?>> create(@Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(201, "blueprint created", bp));
        } catch (BlueprintPersistenceException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(409, e.getMessage(), null));
        }
    }

    @Operation(summary = "Add a point to a blueprint",
               description = "Appends a new (x, y) coordinate to an existing blueprint. Requires scope: blueprints.write")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Point added"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @PutMapping("/{author}/{bpname}/points")
    @PreAuthorize("hasAuthority('SCOPE_blueprints.write')")
    public ResponseEntity<ApiResponse<?>> addPoint(
            @Parameter(description = "Author name", example = "john") @PathVariable String author,
            @Parameter(description = "Blueprint name", example = "house") @PathVariable String bpname,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Point to add",
                content = @Content(schema = @Schema(implementation = Point.class))
            )
            @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(new ApiResponse<>(202, "point added", null));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        }
    }

    // ───────── DTO ────────────────────────────────────────────────────────────

    @Schema(description = "DTO for creating a new blueprint")
    public record NewBlueprintRequest(

            @NotBlank
            @Schema(description = "Blueprint author", example = "john")
            String author,

            @NotBlank
            @Schema(description = "Blueprint name", example = "kitchen")
            String name,

            @Valid
            @Schema(description = "List of blueprint points (x, y)")
            List<Point> points

    ) {}
}
