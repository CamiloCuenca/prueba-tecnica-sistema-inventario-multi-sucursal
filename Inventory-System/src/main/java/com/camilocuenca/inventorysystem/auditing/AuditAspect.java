package com.camilocuenca.inventorysystem.auditing;

import com.camilocuenca.inventorysystem.model.AuditLog;
import com.camilocuenca.inventorysystem.repository.AuditLogRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Aspecto que intercepta métodos anotados con @Auditable y guarda registros de auditoría.
 */
@Aspect
@Component
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    public AuditAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    private String resolveUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return "anonymous";
        return auth.getName();
    }

    private String buildDescription(JoinPoint jp) {
        MethodSignature sig = (MethodSignature) jp.getSignature();
        String method = sig.getDeclaringTypeName() + "." + sig.getMethod().getName();
        String args = Arrays.stream(jp.getArgs()).map(a -> a == null ? "null" : a.toString()).collect(Collectors.joining(", "));
        return method + "(" + args + ")";
    }

    private String resolveAction(JoinPoint jp) {
        MethodSignature sig = (MethodSignature) jp.getSignature();
        Auditable aud = sig.getMethod().getAnnotation(Auditable.class);
        if (aud == null) aud = jp.getTarget().getClass().getAnnotation(Auditable.class);
        if (aud != null && !aud.accion().isEmpty()) return aud.accion();
        return sig.getMethod().getName();
    }

    @AfterReturning(pointcut = "@annotation(com.camilocuenca.inventorysystem.auditing.Auditable) || @within(com.camilocuenca.inventorysystem.auditing.Auditable)", returning = "ret")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void afterSuccess(JoinPoint jp, Object ret) {
        try {
            String usuario = resolveUsername();
            String descripcion = buildDescription(jp);
            String accion = resolveAction(jp);
            AuditLog log = new AuditLog(accion, usuario, descripcion, "SUCCESS", null);
            auditLogRepository.save(log);
        } catch (Exception ex) {
            // no interrumpir la ejecución del método interceptado
        }
    }

    @AfterThrowing(pointcut = "@annotation(com.camilocuenca.inventorysystem.auditing.Auditable) || @within(com.camilocuenca.inventorysystem.auditing.Auditable)", throwing = "ex")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void afterError(JoinPoint jp, Throwable ex) {
        try {
            String usuario = resolveUsername();
            String descripcion = buildDescription(jp);
            String accion = resolveAction(jp);
            String msg = ex.getMessage() == null ? ex.getClass().getName() : ex.getMessage();
            AuditLog log = new AuditLog(accion, usuario, descripcion, "ERROR", msg);
            auditLogRepository.save(log);
        } catch (Exception inner) {
            // no interrumpir
        }
    }
}

