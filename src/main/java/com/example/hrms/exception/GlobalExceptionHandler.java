package com.example.hrms.exception;

import com.example.hrms.common.http.model.Result;
import com.example.hrms.common.http.model.ResultData;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  private final MessageSource messageSource;

  public GlobalExceptionHandler(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResultData<?> handleResourceNotFoundException(ResourceNotFoundException ex) {
    return new ResultData<>(Result.PARAM_ERROR, ex.getMessage(), null);
  }

  @ExceptionHandler(BadRequestException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResultData<?> handleBadRequestException(BadRequestException ex) {
    return new ResultData<>(Result.PARAM_ERROR, ex.getMessage(), null);
  }

  @ExceptionHandler(UnauthorizedException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ResultData<?> handleUnauthorizedException(UnauthorizedException ex) {
    return new ResultData<>(Result.UNAUTHORIZED, ex.getMessage(), null);
  }

  @ExceptionHandler(InvalidArgumentException.class)
  @ResponseStatus(HttpStatus.OK)
  public ResultData<?> handleInvalidArgument(InvalidArgumentException ex) {
    return new ResultData<>(Result.PARAM_ERROR, ex.getMessage(), null);
  }

  /**
   * Spring MVC
   *
   * @param req HttpServletRequest 객체
   * @param e   Exception 객체
   * @return ResultData 객체
   */
  @ExceptionHandler({HttpRequestMethodNotSupportedException.class,
      HttpMediaTypeNotSupportedException.class,
      HttpMediaTypeNotAcceptableException.class, MissingServletRequestParameterException.class,
      ServletRequestBindingException.class, ConversionNotSupportedException.class,
      HttpMessageNotReadableException.class, MissingServletRequestPartException.class,
      NoHandlerFoundException.class, MethodArgumentTypeMismatchException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResultData<?> handleMvcException(HttpServletRequest req, Exception e) {
    e.printStackTrace();
    log.warn("MVC_EXCEPTION : {} | {}", req.getRequestURI(), e.getMessage());
    return new ResultData<>(Result.PARAM_ERROR, e.getMessage(), null);
  }

  /**
   * hand for case data not found.
   * @param req
   * @param e
   * @return
   */
  @ExceptionHandler(DataNotFoundException.class)
  @ResponseStatus(HttpStatus.OK)
  public ResultData<?> handleNotFoundException(HttpServletRequest req,
                                                   DataNotFoundException e) {
    log.warn("NOT_FOUND_EXCEPTION : {} | {}", req.getRequestURI(), e.getMessage());
    return new ResultData<>(Result.NO_DATA, e.getMessage(), null);
  }
  @ExceptionHandler(InvalidPasswordException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResultData<?> handleInvalidPasswordException(InvalidPasswordException ex) {
    return new ResultData<>(Result.PARAM_ERROR, ex.getMessage(), null);
  }

  }
