/**
 * @program: lmmvideos
 * @description: 启动类
 * @author: minmin.liu
 * @create: 2018-09-26 16:17
 **/
package com.lmm;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.lmm.utils.SpringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(exclude = {DruidDataSourceAutoConfigure.class})
//扫描mybatis mapper包路径
@MapperScan(basePackages = "com.lmm.mapper")
@ComponentScan(basePackages = {"com.lmm", "org.n3r.idworker"})
public class Application {
    /**
     * 注册springutil
     *
     * @return
     */
    @Bean
    public SpringUtil getSpringUtil() {
        return new SpringUtil();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
