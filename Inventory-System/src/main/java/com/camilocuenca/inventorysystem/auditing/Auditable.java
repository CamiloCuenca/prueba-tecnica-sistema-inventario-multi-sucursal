package com.camilocuenca.inventorysystem.auditing;

import java.lang.annotation.*;

/**
 * Marca métodos o clases cuyos llamados deben registrarse en el sistema de auditoría.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
    String accion() default "";
}

