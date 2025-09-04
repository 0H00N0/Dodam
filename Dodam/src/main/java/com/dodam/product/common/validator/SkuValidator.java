package com.dodam.product.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * SKU 형식 검증 구현체
 * 
 * <p>SKU 형식 검증 로직을 구현합니다.</p>
 * 
 * @since 1.0.0
 */
public class SkuValidator implements ConstraintValidator<ValidSku, String> {
    
    /**
     * SKU 정규식 패턴
     * - 영문 대문자와 숫자, 하이픈으로 구성
     * - 길이 8-20자
     * - 하이픈으로 구분되는 구조
     */
    private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Z0-9]+(-[A-Z0-9]+)*$");
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 20;
    
    @Override
    public void initialize(ValidSku constraintAnnotation) {
        // 초기화 로직 (필요시 구현)
    }
    
    @Override
    public boolean isValid(String sku, ConstraintValidatorContext context) {
        // null이나 빈 문자열인 경우 다른 검증 어노테이션에 위임
        if (sku == null || sku.trim().isEmpty()) {
            return true;
        }
        
        // 길이 체크
        if (sku.length() < MIN_LENGTH || sku.length() > MAX_LENGTH) {
            addConstraintViolation(context, 
                String.format("SKU 길이는 %d-%d자 사이여야 합니다.", MIN_LENGTH, MAX_LENGTH));
            return false;
        }
        
        // 패턴 체크
        if (!SKU_PATTERN.matcher(sku).matches()) {
            addConstraintViolation(context, 
                "SKU는 영문 대문자, 숫자, 하이픈(-)만 사용할 수 있습니다.");
            return false;
        }
        
        // 하이픈으로 시작하거나 끝나는 경우 체크
        if (sku.startsWith("-") || sku.endsWith("-")) {
            addConstraintViolation(context, 
                "SKU는 하이픈(-)으로 시작하거나 끝날 수 없습니다.");
            return false;
        }
        
        // 연속된 하이픈 체크
        if (sku.contains("--")) {
            addConstraintViolation(context, 
                "SKU에 연속된 하이픈(--)은 사용할 수 없습니다.");
            return false;
        }
        
        return true;
    }
    
    /**
     * 커스텀 에러 메시지를 설정합니다.
     * 
     * @param context 검증 컨텍스트
     * @param message 에러 메시지
     */
    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
               .addConstraintViolation();
    }
}