package com.shop_service.web.global;

import com.shop_service.exception.BizException;
import com.shop_service.exception.LockAcquireException;
import com.shop_service.exception.QueryException;
import com.shop_service.model.response.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * @className: SysAdmin
 * @author: Java之父
 * @date: 2025/10/5 18:53
 * @version: 1.0.0
 * @description: 全局异常处理器
 * 统一捕获并返回规范的响应格式
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionController {

    /**
     * 处理表单参数校验异常 (@Valid用于表单对象)
     *
     * @param e BindException
     * @return  响应
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    R<String> bindExceptionHandler(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数输入有误");
        return R.fail(message);
    }

    /**
     * 处理JSON参数校验异常 (@RequestBody + @Valid)
     *
     * @param e MethodArgumentNotValidException
     * @return  响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    R<String> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数输入有误");
        return R.fail(message);
    }

    /**
     * 处理单个参数校验异常 (@Validated用于控制器方法参数)
     *
     * @param e ConstraintViolationException
     * @return  响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    R<String> constraintViolationExceptionHandler(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return R.fail(message);
    }

    /**
     * 处理文件上传大小超限
     *
     * @param e MaxUploadSizeExceededException
     * @return  响应
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    R<String> maxUploadSizeExceededExceptionHandler(MaxUploadSizeExceededException e) {
        log.warn("上传文件过大: {}", e.getMessage());
        return R.fail("上传文件大小超出限制");
    }

    /**
     * 处理资源未找到异常 (静态资源)
     *
     * @return 响应
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    R<String> noResourceFoundExceptionHandler(HttpServletRequest request) {
        log.warn("请求资源不存在 -> uri: {}", request.getRequestURI());
        return R.fail("资源不存在");
    }

    /**
     * 处理请求地址不存在异常 (路由404)
     *
     * @param request HttpServletRequest
     * @return        响应
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    R<String> noHandlerFoundExceptionHanler(HttpServletRequest request) {
        log.warn("请求地址不存在 -> uri: {}", request.getRequestURI());
        return R.fail("请求地址不存在");
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.OK)
    R<String> bizExceptionHandler(BizException e, HttpServletRequest request) {
        log.warn("业务异常 -> uri: {} message: {}", request.getRequestURI(), e.getMessage());
        return R.fail(e.getMessage());
    }

    /**
     * 处理参数异常
     */
    @ExceptionHandler(QueryException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    R<String> queryExceptionHandler(QueryException e, HttpServletRequest request) {
        log.warn("参数异常 -> uri: {} message: {}", request.getRequestURI(), e.getMessage());
        return R.fail(e.getMessage());
    }

    /**
     * 处理获取锁失败异常
     */
    @ExceptionHandler(LockAcquireException.class)
    @ResponseStatus(HttpStatus.OK)
    R<String> lockAcquireExceptionHandler(LockAcquireException e, HttpServletRequest request) {
        log.warn(
                "获取锁失败 -> uri: {} lock: {} wait: {} lease: {} multi: {}",
                request.getRequestURI(),
                e.getLockName(),
                e.getWaitSeconds(),
                e.getLeaseSeconds(),
                e.isMulti()
        );
        return R.fail("服务繁忙");
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(ArithmeticException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    R<String> arithmeticExceptionHandler(ArithmeticException e, HttpServletRequest request) {
        log.warn("认证失败 -> uri: {} message: {}", request.getRequestURI(), e.getMessage());
        return R.fail(e.getMessage());
    }

    /**
     * 处理未知异常
     *
     * @param e       异常
     * @param request HttpServletRequest
     * @return 响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    R<String> exceptionHandler(Exception e, HttpServletRequest request) {
        log.error("接口请求异常 -> uri: {} error: {}", request.getRequestURI(), e.getMessage(), e);
        return R.error("请求异常: " + e.getMessage());
    }
}
