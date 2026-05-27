package com.yunhwan.cloudsimlab.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.yunhwan.cloudsimlab.learningdocument.application.LearningDocumentNotFoundException;
import com.yunhwan.cloudsimlab.scenario.application.InvalidSimulationRequestException;
import com.yunhwan.cloudsimlab.scenario.application.ScenarioNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(LearningDocumentNotFoundException.class)
	ErrorResponse handleLearningDocumentNotFound(LearningDocumentNotFoundException ex) {
		return new ErrorResponse("LEARNING_DOCUMENT_NOT_FOUND", ex.getMessage());
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(ScenarioNotFoundException.class)
	ErrorResponse handleScenarioNotFound(ScenarioNotFoundException ex) {
		return new ErrorResponse("SCENARIO_NOT_FOUND", ex.getMessage());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(InvalidSimulationRequestException.class)
	ErrorResponse handleInvalidSimulationRequest(InvalidSimulationRequestException ex) {
		return new ErrorResponse("INVALID_SIMULATION_REQUEST", ex.getMessage());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		return new ErrorResponse("INVALID_REQUEST", "Invalid request value: " + ex.getName());
	}

	public record ErrorResponse(String code, String message) {
	}
}
