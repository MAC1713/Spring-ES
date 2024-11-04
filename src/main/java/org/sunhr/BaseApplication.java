package org.sunhr;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author mac
 */
@Configuration
@SpringBootApplication()
@EnableAspectJAutoProxy
@MapperScan("org.sunhr.sql.mapper")
public class BaseApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(BaseApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ 启动成功   ლ(´ڡ`ლ)ﾞ \n");
    }
}
