package com.dodam.product.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SKU 형식 검증 어노테이션
 * 
 * <p>SKU는 다음 형식을 따라야 합니다:</p>
 * <ul>
 *   <li>길이: 8-20자</li>
 *   <li>구성: 영문 대문자, 숫자, 하이픈(-) 조합</li>
 *   <li>패턴: ABC-123-DEF 형태</li>
 * </ul>
 * 
 * @since 1.0.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SkuValidator.class)
public @interface ValidSku {
    
    String message() default "올바른 SKU 형식이 아닙니다. (예: ABC-123-DEF)";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}