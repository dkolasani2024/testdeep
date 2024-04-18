package com.cainc.commoncontent.adapters.web.in;

import static com.cainc.commoncontent.shared.SecurityRoleConstants.*;

import com.cainc.commoncontent.adapters.web.in.request.prerequisitereport.PrerequisiteReportConfigurationsRequest;
import com.cainc.commoncontent.core.domain.customErrorResponses.CustomErrorResponse;
import com.cainc.commoncontent.core.domain.prerequisitereportdto.BookUnitPartDTO;
import com.cainc.commoncontent.core.domain.prerequisitereportdto.PrerequisiteReportConfigurationDTO;
import com.cainc.commoncontent.core.domain.prerequisitereportdto.PrerequisiteReportSkillDTO;
import com.cainc.commoncontent.core.ports.in.PrerequisiteReportUseCase;
import com.cainc.commoncontent.shared.ExcludeFromJacocoGeneratedReport;
import com.cainc.commoncontent.support.exception.DataNotFoundException;
import com.cainc.commoncontent.support.exception.InvalidRequestException;
import com.cainc.commons.core.enums.GradeLevelEnum;
import com.cainc.commons.session.validation.support.GlobalSessionId;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is the controller for the Prerequisite Report Content feature.
 * It handles HTTP requests related to the Prerequisite Report Content.
 */
@RestController
@Tag(name = "prerequisitereport")
@RequestMapping(value = "/educator", produces = { MediaType.APPLICATION_JSON_VALUE })
@SecuritySchemes(
    {
        @SecurityScheme(
            paramName = "sessionId",
            type = SecuritySchemeType.APIKEY,
            in = SecuritySchemeIn.COOKIE,
            name = "CA.IREADY.SECURE_TOKEN",
            scheme = "bearer",
            bearerFormat = "JWT"
        ),
    }
)
@ExcludeFromJacocoGeneratedReport(reason = "Will get tested by functional api tests")
@Slf4j
@Timed
@PreAuthorize(HAS_ROLE_CA_TEACHER + " OR  " + HAS_ROLE_CA_SCHOOLADMIN + " OR " + HAS_ROLE_CA_DISTRICTADMIN)
// The PreAuthorize annotation on the method means that it also requires a valid JWT that is authorized as an educator. See WebSecurityConfig.
public class PrerequisiteReportController {

    @Autowired
    PrerequisiteReportUseCase prerequisiteReportUseCase;

    /**
     * This method handles the GET request to fetch book unit parts based on the grade and prereqConfigId
     * It uses the PrerequisiteReportUseCase to fetch the data.
     *
     * @param grade The grade for which the book unit parts are to be fetched
     * @param prereqConfigId The prereqConfigId used to get BookSeriesId which is used to fetch book unit parts
     * @param sessionId The session ID of the user making the request
     * @return A list of BookUnitPartDTO object.
     */
    @GetMapping("/book-unit-parts")
    @Operation(
        summary = "Returns the list of Book Unit Parts.",
        description = "Returns the list of Book Unit Parts DTOs."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Returned the list of Book Unit Parts successfully.",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = BookUnitPartDTO.class)))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Bad Request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                responseCode = "503",
                description = "Service Unavailable",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
        }
    )
    public ResponseEntity<List<BookUnitPartDTO>> getBookUnitsParts(
        @RequestParam String grade,
        @RequestParam String prereqConfigId,
        @Parameter(hidden = true) @GlobalSessionId String sessionId
    ) {
        //Validate if the given Strings are empty or if the given grade is valid
        if (grade.isBlank() || prereqConfigId.isBlank() || !isGivenGradeValid(grade)) {
            throw new InvalidRequestException("Empty/Invalid Input Parameters");
        }

        // Logging the request details
        log.info("Getting book unit parts for grade: {}, prereqConfigId: {}", grade, prereqConfigId);

        // Get Book Unit Parts based on grade and prereqConfigId
        List<BookUnitPartDTO> bookUnitPartDTOs = prerequisiteReportUseCase.getBookUnitPartsByGradeAndPrereqConfigId(
            grade,
            prereqConfigId
        );

        // Logging the size of the fetched book unit parts
        log.info("Returning book unit parts list of size: " + bookUnitPartDTOs.size());
        // Returning the fetched bookUnitPartsDTO which includes Book Unit Parts
        return ResponseEntity.ok(bookUnitPartDTOs);
    }

    /**
     * This method handles the GET request to fetch prerequisite report skills based on the bookUnitPartId and prereqConfigId
     * It uses the PrerequisiteReportUseCase to fetch the data.
     *
     * @param bookUnitPartId The bookUnitPartId for which the prerequisite skills are to be fetched
     * @param prereqConfigId The prereqConfigId used to get prerequisite skills are to be fetched
     * @param sessionId The session ID of the user making the request
     * @return A list of PrerequisiteReportSkillDTO object.
     */
    @GetMapping("/prerequisite-report-skills")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Returns the list of Prerequisite Report Skills successfully.",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = PrerequisiteReportSkillDTO.class))
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Bad Request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Not Found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                responseCode = "503",
                description = "Service Unavailable",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
        }
    )
    public List<PrerequisiteReportSkillDTO> getPrerequisiteReportSkills(
        @RequestParam String bookUnitPartId,
        @RequestParam String prereqConfigId,
        @Parameter(hidden = true) @GlobalSessionId String sessionId
    ) {
        if (bookUnitPartId.isBlank() || prereqConfigId.isBlank()) {
            throw new InvalidRequestException("Request arguments cannot be empty");
        }
        // Logging the request details
        log.info(
            "Getting Prerequisite Report Skills for bookUnitPartId: {}, prereqConfigId: {}",
            bookUnitPartId,
            prereqConfigId
        );
        List<PrerequisiteReportSkillDTO> prerequisiteReportSkillDTOS =
            prerequisiteReportUseCase.getPrereqReportSkillByUnitPartandPrereqConfig(bookUnitPartId, prereqConfigId);
        log.info("Returning prerequisite report skills list of size: " + prerequisiteReportSkillDTOS.size());
        return prerequisiteReportSkillDTOS;
    }

    /**
     * Handles the GET request to fetch Prerequisite Report Configurations based on specified criteria.
     *
     * It leverages the PrerequisiteReportUseCase to retrieve the data and returns a PrerequisiteReportConfigurationDTO
     * object that encapsulates the configurations.
     *
     * @param prerequisiteReportConfigurationsRequest A request object containing the criteria for fetching report configurations
     *        - productLine: The product line for which the report configurations are to be fetched
     *        - bookSeriesId: The ID of the book series for which the report configurations are to be fetched
     *        - showStandards: A boolean flag indicating whether to show standards in the report configurations
     * @return ResponseEntity<PrerequisiteReportConfigurationDTO> A response entity containing the PrerequisiteReportConfigurationDTO or an error status
     */
    @GetMapping("/prerequisite-report-configurations")
    @Operation(
        summary = "Fetch Prerequisite Report Configurations from Common Content Service.",
        description = "Retrieves Prerequisite Report Configurations based on product line, book series ID, and show standards criteria."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Returned the Prerequisite Report Configurations successfully.",
                content = @Content(
                    array = @ArraySchema(schema = @Schema(implementation = PrerequisiteReportConfigurationDTO.class))
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Bad Request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Not Found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
        }
    )
    public ResponseEntity<PrerequisiteReportConfigurationDTO> getPrerequisiteReportConfigurations(
        @Valid @ModelAttribute PrerequisiteReportConfigurationsRequest prerequisiteReportConfigurationsRequest,
        BindingResult bindingResult
    ) {
        // Logging the request details
        log.info(
            "Getting Prerequisite Report Configurations for productLine: {}, bookSeriesId: {}, showStandards: {}",
            prerequisiteReportConfigurationsRequest.getProductLine(),
            prerequisiteReportConfigurationsRequest.getBookSeriesId(),
            prerequisiteReportConfigurationsRequest.isShowStandards()
        );

        try {
            // Verify if any error in API input param
            if (bindingResult.hasErrors()) {
                // Construct an error response based on bindingResult
                String errorResponse = constructErrorResponse(bindingResult);
                throw new InvalidRequestException(errorResponse);
            }

            // Get Prerequisite Report Configurations based on productLine, bookSeriesId and showStandards
            PrerequisiteReportConfigurationDTO prerequisiteReportConfigurationDTO =
                prerequisiteReportUseCase.getPrerequisiteReportConfigurationsByProductLineBookSeriesIdAndShowStandards(
                    prerequisiteReportConfigurationsRequest.getProductLine(),
                    prerequisiteReportConfigurationsRequest.getBookSeriesId(),
                    prerequisiteReportConfigurationsRequest.isShowStandards()
                );

            //log.info("Returning PrerequisiteReportConfigurationDTO : " + prerequisiteReportConfigurationDTO.toString());
            HttpHeaders headers = new HttpHeaders();
            // Returning the fetched PrerequisiteReportConfigurationDTO which includes Prerequisite Report Configurations and PrerequisiteReportDetailedConfiguration
            return new ResponseEntity<>(prerequisiteReportConfigurationDTO, headers, HttpStatus.OK);
        } catch (InvalidRequestException e) {
            log.error("Invalid input/s to Prerequisite Report Configurations API: ", e);
            return new ResponseEntity<>(null, null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Service call error on get Prerequisite Report Configurations: ", e);
            return new ResponseEntity<>(null, null, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private boolean isGivenGradeValid(String grade) {
        return Arrays
            .stream(GradeLevelEnum.values())
            .anyMatch(gradeValue -> gradeValue.getAuthoringAbbreviation().equals(grade));
    }

    public static String constructErrorResponse(BindingResult bindingResult) {
        // Stream through errors, log them, and collect into a concatenated string
        String concatenatedErrorMessages = bindingResult
            .getAllErrors()
            .stream()
            .map(ObjectError::getDefaultMessage)
            //                .peek(message -> log.error("Validation error: {}", message)) // Log each error message
            .collect(Collectors.joining("; "));

        return concatenatedErrorMessages;
    }

    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<CustomErrorResponse> handleInvalidRequestException(
        InvalidRequestException ex,
        HttpServletRequest request
    ) {
        log.error("InvalidRequestException occurred: {}", ex.getMessage());
        CustomErrorResponse errorResponse = CustomErrorResponse
            .builder()
            .errorMessage("Bad request")
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .path(request.getRequestURI())
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(DataNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<CustomErrorResponse> handleDataNotFoundException(
        DataNotFoundException ex,
        HttpServletRequest request
    ) {
        log.error("DataNotFoundException occurred: {}", ex.getMessage());
        CustomErrorResponse errorResponse = CustomErrorResponse
            .builder()
            .errorMessage("Not Found")
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .path(request.getRequestURI())
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
}
