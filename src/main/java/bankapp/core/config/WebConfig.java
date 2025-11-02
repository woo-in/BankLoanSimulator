package bankapp.core.config;

import bankapp.account.interceptor.TransferRequestIdCheckInterceptor;
import bankapp.member.interceptor.LoginCheckerInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(new LoginCheckerInterceptor())
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns("/error" , "/signup/success" , "/temp-admin/**");
        // TODO : 임시 관리 페이지 허용 , 배포시 지울 것 "/temp-admin/**"

        registry.addInterceptor(new TransferRequestIdCheckInterceptor())
                .order(2)
                .addPathPatterns("/transfer/amount" , "/transfer/message" , "/transfer/auth" , "/transfer/execute");
    }

}
