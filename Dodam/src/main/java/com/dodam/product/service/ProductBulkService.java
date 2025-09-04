package com.dodam.product.service;

import com.dodam.product.entity.CategoryEntity;
import com.dodam.product.entity.ProductEntity;
import com.dodam.product.repository.CategoryRepository;
import com.dodam.product.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductBulkService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // === 결과 DTO ===
    @Data @AllArgsConstructor
    public static class BulkImportResult {
        private int total;
        private int success;
        private List<String> errors;
        private List<Long> createdIds;
    }

    // ✅ 헤더를 내부표준키로 매핑하는 alias 맵 (한/영/동의어 지원)
    private static final Map<String, String> HEADER_ALIAS = Map.ofEntries(
        Map.entry("proname",  "상품명,product name,product,goods,name,품명,proname"),
        Map.entry("catename", "카테고리명,category name,category,cat name,catename"),
        Map.entry("catenum",  "카테고리번호,category id,category no,cat id,catenum"),
        Map.entry("proprice", "판매가격,가격,price,sale price,amount,proprice"),
        Map.entry("prograd",  "등급,grade,level,prograd"),
        Map.entry("prorent",  "대여가격,rent price,rental price,prorent"),
        Map.entry("probrand", "브랜드,brand,probrand"),
        Map.entry("prosafe",  "안전인증,kc,안전,안전인증번호,prosafety,prosafe"),
        Map.entry("prostat",  "상태,status,재고상태,판매상태,prostat"),
        Map.entry("promanuf", "제조사,manufacturer,make,promanuf"),
        Map.entry("proagfr",  "사용연령최소,연령from,age from,min age,proagfr"),
        Map.entry("proagto",  "사용연령최대,연령to,age to,max age,proagto"),
        Map.entry("promind",  "최소대여일,minimum days,min days,promind")
    );

    // 문자열 정규화
    private static String norm(String s){
        if (s == null) return null;
        return s.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    // 별칭 테이블을 빠르게 조회하기 위한 역인덱스
    private static final Map<String,String> HEADER_LOOKUP;
    static {
        Map<String,String> tmp = new HashMap<>();
        for (var e : HEADER_ALIAS.entrySet()) {
            String key = e.getKey();
            for (String alt : e.getValue().split(",")) {
                tmp.put(norm(alt), key); // "상품명" -> "proname" 등
            }
            tmp.put(norm(key), key);    // 자기 자신도 매핑
        }
        HEADER_LOOKUP = Collections.unmodifiableMap(tmp);
    }

    // ✅ 내부 표준키 -> 컬럼인덱스 맵 생성 (한/영/동의어 모두 허용)
    private static Map<String,Integer> headerIndex(Row header) {
        Map<String,Integer> map = new HashMap<>();
        if (header == null) return map;
        short last = header.getLastCellNum();
        for (int i=0;i<last;i++) {
            Cell c = header.getCell(i);
            if (c == null) continue;
            c.setCellType(CellType.STRING); // DEV 편의 (필요 시 DataFormatter 사용)
            String raw = c.getStringCellValue();
            String k = HEADER_LOOKUP.get(norm(raw));
            if (k != null && !map.containsKey(k)) {
                map.put(k, i);
            }
        }
        return map;
    }

    // === 템플릿 생성: 다운로드 시 한글 컬럼명으로 생성됨 ===
    public ByteArrayInputStream makeTemplateXlsx() {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet("products");
            Row header = sh.createRow(0);

            // ✅ 한국어 컬럼 헤더
            String[] cols = {
                "상품명","카테고리명","카테고리번호","판매가격","등급","대여가격",
                "브랜드","안전인증","상태","제조사","사용연령최소","사용연령최대","최소대여일"
            };
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

            // 예시 1행
            Row ex = sh.createRow(1);
            ex.createCell(0).setCellValue("예시상품");
            ex.createCell(1).setCellValue("로봇");     // 카테고리명 또는 카테고리번호 둘 중 하나만
            ex.createCell(3).setCellValue(80000);
            ex.createCell(4).setCellValue("A");       // S/A/B/C
            ex.createCell(6).setCellValue("DJI");
            ex.createCell(8).setCellValue("AVAILABLE");

            for (int i = 0; i < cols.length; i++) sh.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("템플릿 생성 실패", e);
        }
    }

    // === 업로드 처리 ===
    @Transactional
    public BulkImportResult importXlsx(MultipartFile file, Long adminId) throws Exception {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("파일이 없습니다.");
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        String ctype = file.getContentType();
        if (!(name.endsWith(".xlsx") || (ctype != null && ctype.contains("spreadsheetml")))) {
            throw new IllegalArgumentException("xlsx 파일만 업로드하세요.");
        }

        List<String> errors = new ArrayList<>();
        List<Long> created = new ArrayList<>();

        try (InputStream in = file.getInputStream(); Workbook wb = WorkbookFactory.create(in)) {
            Sheet sh = wb.getSheetAt(0);
            if (sh == null || sh.getPhysicalNumberOfRows() < 2) {
                return new BulkImportResult(0, 0, List.of("데이터가 없습니다."), List.of());
            }

            // 헤더 인덱스 맵
            Map<String,Integer> idx = headerIndex(sh.getRow(0));

            // 최소 필수 헤더 검증 (상품명, 가격, 카테고리(명/번호 중 하나))
            List<String> missing = new ArrayList<>();
            if (!idx.containsKey("proname")) missing.add("상품명");
            if (!idx.containsKey("proprice")) missing.add("판매가격");
            if (!idx.containsKey("catename") && !idx.containsKey("catenum")) missing.add("카테고리명 또는 카테고리번호");
            if (!missing.isEmpty()) {
                return new BulkImportResult(0, 0,
                    List.of("필수 컬럼 누락: " + String.join(", ", missing) + " (템플릿을 다시 받아 사용하세요)"),
                    List.of());
            }

            int total = 0, success = 0;
            for (int r = 1; r <= sh.getLastRowNum(); r++) {
                Row row = sh.getRow(r);
                if (row == null) continue;
                // 완전 공백 행 스킵
                if (rowIsBlank(row)) continue;

                total++;

                try {
                    ProductEntity p = new ProductEntity();

                    String proname = str(row, idx.get("proname"));
                    if (isBlank(proname)) throw new IllegalArgumentException("상품명(proname) 필수");

                    String catename = str(row, idx.get("catename"));
                    Long catenum = longOrNull(row, idx.get("catenum"));

                    CategoryEntity category;
                    if (catenum != null) {
                        category = categoryRepository.findById(catenum)
                                .orElseThrow(() -> new IllegalArgumentException("catenum=" + catenum + " 카테고리 없음"));
                    } else if (!isBlank(catename)) {
                        category = categoryRepository.findByCatename(catename)
                                .orElseThrow(() -> new IllegalArgumentException("catename='" + catename + "' 카테고리 없음"));
                    } else {
                        throw new IllegalArgumentException("catename 또는 catenum 중 하나는 필수");
                    }

                    BigDecimal price = big(row, idx.get("proprice"));
                    if (price == null) throw new IllegalArgumentException("proprice 필수");

                    String grad = upper(str(row, idx.get("prograd"))); // S/A/B/C
                    BigDecimal rent = big(row, idx.get("prorent"));
                    if (rent == null) rent = autoRent(price, grad); // 등급 기반 자동계산

                    p.setProname(proname);
                    p.setCategory(category);
                    p.setProprice(price);
                    p.setPrograd(grad);
                    p.setProrent(rent);
                    p.setProbrand(str(row, idx.get("probrand")));
                    p.setProsafe(str(row, idx.get("prosafe")));
                    p.setProstat(upper(defaultIfBlank(str(row, idx.get("prostat")), "AVAILABLE")));
                    p.setPromanuf(str(row, idx.get("promanuf")));
                    p.setProagfr(intOrNull(row, idx.get("proagfr")));
                    p.setProagto(intOrNull(row, idx.get("proagto")));
                    p.setPromind(intOrNull(row, idx.get("promind")));
                    p.setProdate(LocalDate.now());
                    if (adminId != null) p.setProcreat(adminId.intValue());

                    ProductEntity saved = productRepository.save(p);
                    created.add(saved.getPronum());
                    success++;

                    // 성능 튜닝 포인트 (필요 시)
                    // if (success % 100 == 0 && productRepository instanceof JpaRepository) { ((JpaRepository<?,?>)productRepository).flush(); }

                } catch (Exception ex) {
                    errors.add(String.format("%d행: %s", r + 1, ex.getMessage()));
                }
            }
            return new BulkImportResult(total, success, errors, created);
        }
    }

    // === 헬퍼들 ===
    private static final DataFormatter DATA_FMT = new DataFormatter(Locale.KOREA);

    private static boolean rowIsBlank(Row row) {
        if (row == null) return true;
        short last = row.getLastCellNum();
        for (int i=0;i<last;i++){
            Cell c = row.getCell(i);
            if (c != null && !isBlank(DATA_FMT.formatCellValue(c))) return false;
        }
        return true;
    }

    private static String str(Row row, Integer i) {
        if (i==null || row==null) return null;
        Cell c = row.getCell(i);
        if (c==null) return null;
        // DataFormatter는 숫자/날짜/문자 모두 문자열로 안전 변환
        String v = DATA_FMT.formatCellValue(c);
        return v!=null ? v.trim() : null;
    }

    private static BigDecimal big(Row row, Integer i) {
        if (i==null || row==null) return null;
        Cell c = row.getCell(i);
        if (c==null) return null;
        try {
            switch (c.getCellType()) {
                case NUMERIC:
                    return BigDecimal.valueOf(c.getNumericCellValue());
                case FORMULA:
                    // 수식 결과도 안전하게 포맷 → 파싱
                    String fv = DATA_FMT.formatCellValue(c);
                    if (isBlank(fv)) return null;
                    return new BigDecimal(fv.replaceAll(",", ""));
                default:
                    String s = DATA_FMT.formatCellValue(c);
                    if (isBlank(s)) return null;
                    return new BigDecimal(s.replaceAll(",", ""));
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer intOrNull(Row row, Integer i) {
        BigDecimal b = big(row, i);
        return b==null ? null : b.intValue();
    }
    private static Long longOrNull(Row row, Integer i) {
        BigDecimal b = big(row, i);
        return b==null ? null : b.longValue();
    }
    private static boolean isBlank(String s){ return s==null || s.isBlank(); }
    private static String upper(String s){ return s==null?null:s.toUpperCase(Locale.ROOT); }
    private static String defaultIfBlank(String s, String def){ return isBlank(s)?def:s; }

    private static BigDecimal autoRent(BigDecimal price, String grad) {
        double ratio = switch (grad==null?"":grad) {
            case "S" -> 1.00; case "A" -> 0.90; case "B" -> 0.80; case "C" -> 0.70;
            default -> 0.0;
        };
        return BigDecimal.valueOf(Math.floor(price.doubleValue() * ratio));
    }
}
