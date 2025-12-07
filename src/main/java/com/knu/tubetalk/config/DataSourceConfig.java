package com.knu.tubetalk.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 트랜잭션 관리를 위한 DataSource 설정
 * 
 * Spring Boot가 자동으로 생성한 DataSource를 TransactionAwareDataSourceProxy로
 * 래핑하여 @Transactional 어노테이션이 제대로 작동하도록 합니다.
 * 
 * 이렇게 하면 DAO에서 dataSource.getConnection()을 호출할 때
 * 트랜잭션 컨텍스트에 있는 Connection을 재사용하게 됩니다.
 */
@Configuration
@EnableTransactionManagement
public class DataSourceConfig {

    /**
     * DataSource를 TransactionAwareDataSourceProxy로 래핑합니다.
     * @Primary로 설정하여 DAO들이 자동으로 이 DataSource를 주입받도록 합니다.
     * 
     * ObjectProvider를 사용하여 순환 참조를 방지합니다.
     */
    @Bean
    @Primary
    @ConditionalOnBean(DataSource.class)
    public DataSource dataSource(ObjectProvider<DataSource> dataSourceProvider) {
        // ObjectProvider에서 원본 DataSource 가져오기 (우리 Bean 제외)
        DataSource originalDataSource = dataSourceProvider.stream()
            .filter(ds -> !(ds instanceof TransactionAwareDataSourceProxy))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("원본 DataSource를 찾을 수 없습니다."));
        
        return new TransactionAwareDataSourceProxy(originalDataSource);
    }
}

