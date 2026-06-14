package com.yunhwan.cloudsimlab.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.yunhwan.cloudsimlab.learningdocument.application.LearningDocumentNotFoundException;
import com.yunhwan.cloudsimlab.learningmodule.application.LearningModuleNotFoundException;
import com.yunhwan.cloudsimlab.learningpath.application.LearningPathNotFoundException;
import com.yunhwan.cloudsimlab.scenario.application.InvalidSimulationRequestException;
import com.yunhwan.cloudsimlab.scenario.application.ScenarioNotFoundException;
import com.yunhwan.cloudsimlab.userarchitecture.application.InvalidUserArchitectureRequestException;
import com.yunhwan.cloudsimlab.userarchitecture.application.UserArchitectureNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(LearningDocumentNotFoundException.class)
	ErrorResponse handleLearningDocumentNotFound(LearningDocumentNotFoundException ex) {
		return new ErrorResponse("LEARNING_DOCUMENT_NOT_FOUND", ex.getMessage());
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(LearningPathNotFoundException.class)
	ErrorResponse handleLearningPathNotFound(LearningPathNotFoundException ex) {
		return new ErrorResponse("LEARNING_PATH_NOT_FOUND", ex.getMessage());
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(LearningModuleNotFoundException.class)
	ErrorResponse handleLearningModuleNotFound(LearningModuleNotFoundException ex) {
		return new ErrorResponse("LEARNING_MODULE_NOT_FOUND", ex.getMessage());
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

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(UserArchitectureNotFoundException.class)
	ErrorResponse handleUserArchitectureNotFound(UserArchitectureNotFoundException ex) {
		return new ErrorResponse("USER_ARCHITECTURE_NOT_FOUND", ex.getMessage());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(InvalidUserArchitectureRequestException.class)
	ErrorResponse handleInvalidUserArchitectureRequest(InvalidUserArchitectureRequestException ex) {
		return new ErrorResponse("INVALID_USER_ARCHITECTURE_REQUEST", ex.getMessage());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		return new ErrorResponse("INVALID_REQUEST", "Invalid request value: " + ex.getName());
	}

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(HttpMessageNotReadableException.class)
	ErrorResponse handleMessageNotReadable() {
		return new ErrorResponse("INVALID_REQUEST", "Malformed or unreadable request body");
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	ErrorResponse handleUnexpectedException() {
		return new ErrorResponse("INTERNAL_SERVER_ERROR", "Unexpected server error");
	}

	public record ErrorResponse(String code, String message) {
	}
}
