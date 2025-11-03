package com.dksys.biz;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JodConverterConfig {

    // LibreOffice 프로세스 관리
    @Bean(initMethod = "start", destroyMethod = "stop")
    public OfficeManager officeManager() {
        return LocalOfficeManager.builder()
                .officeHome("C:/Program Files/LibreOffice") // 설치 경로에 맞게
                .portNumbers(2002)
                .build();
    }

    // 여기서 DocumentConverter 빈을 직접 만들어 준다
    @Bean
    public DocumentConverter documentConverter(OfficeManager officeManager) {
        // ✅ JodConverter.builder() 가 아니라 LocalConverter 사용
        return LocalConverter.builder()
                .officeManager(officeManager)
                .build();
        // 또는 더 간단히
        // return LocalConverter.make(officeManager);
    }
}
