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

    // === 템플릿 생성 ===
    public ByteArrayInputStream makeTemplateXlsx() {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sh = wb.createSheet("products");
            Row header = sh.createRow(0);
            String[] cols = {
                "proname","catename","catenum","proprice","prograd","prorent",
                "probrand","prosafe","prostat","promanuf","proagfr","proagto","promind"
            };
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

            // 안내 샘플 1행
            Row ex = sh.createRow(1);
            ex.createCell(0).setCellValue("예시상품");
            ex.createCell(1).setCellValue("로봇");    // catename 또는 catenum 중 하나만 채워도 됨
            ex.createCell(3).setCellValue(80000);
            ex.createCell(4).setCellValue("A");      // S/A/B/C
            ex.createCell(6).setCellValue("DJI");
            ex.createCell(8).setCellValue("AVAILABLE");

            for (int i = 0; i < cols.length; i++) sh.autoSizeColumn(i);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // === 업로드 처리 ===
    @Transactional
    public BulkImportResult importXlsx(MultipartFile file, Long adminId) throws Exception {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("파일이 없습니다.");
        if (!Objects.equals(file.getContentType(),
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") &&
            !file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("xlsx 파일만 업로드하세요.");
        }

        List<String> errors = new ArrayList<>();
        List<Long> created = new ArrayList<>();

        try (InputStream in = file.getInputStream(); Workbook wb = WorkbookFactory.create(in)) {
            Sheet sh = wb.getSheetAt(0);
            if (sh.getPhysicalNumberOfRows() < 2) return new BulkImportResult(0, 0, List.of("데이터가 없습니다."), List.of());

            // 헤더 인덱스 맵
            Map<String,Integer> idx = headerIndex(sh.getRow(0));

            int total = 0, success = 0;
            for (int r = 1; r <= sh.getLastRowNum(); r++) {
                Row row = sh.getRow(r);
                if (row == null) continue;
                total++;

                try {
                    ProductEntity p = new ProductEntity();

                    String proname = str(row, idx.get("proname"));
                    if (isBlank(proname)) throw new IllegalArgumentException("상품명(proname) 필수");

                    String catename = str(row, idx.get("catename"));
                    Long catenum = longOrNull(row, idx.get("catenum"));

                    CategoryEntity category = null;
                    if (catenum != null) {
                        category = categoryRepository.findById(catenum)
                                .orElseThrow(() -> new IllegalArgumentException("catenum="+catenum+" 카테고리 없음"));
                    } else if (!isBlank(catename)) {
                        category = categoryRepository.findByCatename(catename)
                                .orElseThrow(() -> new IllegalArgumentException("catename='"+catename+"' 카테고리 없음"));
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

                    // 대량일 때 성능: 100건마다 flush/clear (JPA 설정 있으면)
                    // if (success % 100 == 0) { productRepository.flush(); }

                } catch (Exception ex) {
                    errors.add(String.format("%d행: %s", r+1, ex.getMessage()));
                }
            }
            return new BulkImportResult(total, success, errors, created);
        }
    }

    private static Map<String,Integer> headerIndex(Row header) {
        Map<String,Integer> map = new HashMap<>();
        for (int i=0;i<header.getLastCellNum();i++) {
            Cell c = header.getCell(i);
            if (c == null) continue;
            map.put(c.getStringCellValue().trim().toLowerCase(), i);
        }
        return map;
    }

    // === 헬퍼들 ===
    private static String str(Row row, Integer i) {
        if (i==null) return null;
        Cell c = row.getCell(i);
        if (c==null) return null;
        c.setCellType(CellType.STRING);
        String v = c.getStringCellValue();
        return v!=null ? v.trim() : null;
    }
    private static BigDecimal big(Row row, Integer i) {
        if (i==null) return null;
        Cell c = row.getCell(i);
        if (c==null) return null;
        try {
            if (c.getCellType()==CellType.NUMERIC) return BigDecimal.valueOf(c.getNumericCellValue());
            c.setCellType(CellType.STRING);
            String s = c.getStringCellValue();
            if (isBlank(s)) return null;
            return new BigDecimal(s.replaceAll(",", ""));
        } catch (Exception e) { return null; }
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
