package ar.edu.utn.frc.mycar.web.exception;

import com.openai.errors.OpenAIException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;
import java.util.stream.Collectors;

/** Translates domain exceptions into RFC 9457 {@link ProblemDetail} HTTP responses. */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** Returns 503 when the OpenAI API call fails (quota/billing, outage, invalid key, etc.). */
    @ExceptionHandler(OpenAIException.class)
    public ProblemDetail handleOpenAiFailure(OpenAIException ex) {
        log.error("OpenAI API call failed", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "El asistente de IA no está disponible en este momento. Intentá de nuevo más tarde.");
    }

    /** Returns 400 with a map of field-level validation errors. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (a, b) -> a));
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setProperty("errors", errors);
        return pd;
    }

    /** Returns 409 when the requested email is already registered. */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleDuplicateEmail(EmailAlreadyExistsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    /** Returns 409 when the plate is already registered in the system. */
    @ExceptionHandler(DuplicatePlateException.class)
    public ProblemDetail handleDuplicatePlate(DuplicatePlateException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    /** Returns 404 when a vehicle is not found or does not belong to the requesting user. */
    @ExceptionHandler(VehicleNotFoundException.class)
    public ProblemDetail handleVehicleNotFound(VehicleNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** Returns 404 when an expense is not found or does not belong to the requesting user's vehicle. */
    @ExceptionHandler(ExpenseNotFoundException.class)
    public ProblemDetail handleExpenseNotFound(ExpenseNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** Returns 404 when a maintenance record is not found or does not belong to the requesting user's vehicle. */
    @ExceptionHandler(MaintenanceLogNotFoundException.class)
    public ProblemDetail handleMaintenanceLogNotFound(MaintenanceLogNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** Returns 401 when the email/password combination is invalid. */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /** Returns 403 when the account exists but is disabled. */
    @ExceptionHandler(UserInactiveException.class)
    public ProblemDetail handleUserInactive(UserInactiveException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    /** Returns 400 when the supplied current password does not match the stored hash. */
    @ExceptionHandler(PasswordMismatchException.class)
    public ProblemDetail handlePasswordMismatch(PasswordMismatchException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** Returns 401 for 2FA verification failures (wrong code, expired, max attempts). */
    @ExceptionHandler(TwoFactorVerificationException.class)
    public ProblemDetail handleTwoFactorVerification(TwoFactorVerificationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /** Returns 400 when a password reset token does not exist or has already been used. */
    @ExceptionHandler(InvalidResetTokenException.class)
    public ProblemDetail handleInvalidResetToken(InvalidResetTokenException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** Returns 400 when a password reset token has expired. */
    @ExceptionHandler(ResetTokenExpiredException.class)
    public ProblemDetail handleResetTokenExpired(ResetTokenExpiredException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** Returns 404 when a document is not found or does not belong to the requesting user's vehicle. */
    @ExceptionHandler(DocumentNotFoundException.class)
    public ProblemDetail handleDocumentNotFound(DocumentNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** Returns 404 when a document exists but has no file attached. */
    @ExceptionHandler(DocumentFileNotFoundException.class)
    public ProblemDetail handleDocumentFileNotFound(DocumentFileNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** Returns 400 when an uploaded file fails type/size validation. */
    @ExceptionHandler(InvalidFileException.class)
    public ProblemDetail handleInvalidFile(InvalidFileException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** Returns 400 when the uploaded file exceeds the configured max size. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "El archivo supera el tamaño máximo permitido (10MB).");
    }

    /** Returns 404 when a transfer token does not exist. */
    @ExceptionHandler(TransferTokenNotFoundException.class)
    public ProblemDetail handleTransferTokenNotFound(TransferTokenNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** Returns 410 when a transfer token has expired or was already used. */
    @ExceptionHandler(TransferTokenInvalidException.class)
    public ProblemDetail handleTransferTokenInvalid(TransferTokenInvalidException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.GONE, ex.getMessage());
    }

    /** Returns 400 when the confirming user is already the current owner of the vehicle. */
    @ExceptionHandler(CannotTransferToSelfException.class)
    public ProblemDetail handleCannotTransferToSelf(CannotTransferToSelfException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
