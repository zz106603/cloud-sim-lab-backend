package com.yunhwan.cloudsimlab.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.yunhwan.cloudsimlab.learningdocument.application.LearningDocumentNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(LearningDocumentNotFoundException.class)
	void handleLearningDocumentNotFound() {
	}
}
